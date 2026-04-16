<template>
	<button
		v-if="stitchedImageUrl"
		class="analysis-button"
		@click="submitForAnalysis"
	>Analyze</button>
	<h1 class="desciptor">Choose an area to analyse using the <i class="fa-solid fa-crop"></i> button in the top right corner</h1>
	<AreaSelect
		v-if="selecting"
		@complete="handleSelectionComplete"
		@cancelled="selecting = false"
	/>
	<div class="buttons">
		<button @click="map?.zoomIn()"><i class="fa-solid fa-plus"></i></button>
		<button @click="map?.zoomOut()"><i class="fa-solid fa-minus"></i></button>
		<button
			@click="selecting = !selecting"
			:class="{ active: selecting }"
		><i class="fa-solid fa-crop"></i></button>
	</div>
	<div v-if="isFetching || fetchError || stitchedImageUrl" class="resultCard">
		<p v-if="isFetching">Fetching stitched image...</p>
		<p v-if="fetchError" class="error">{{ fetchError }}</p>
		<template v-if="stitchedImageUrl">
			<p>Stitched image preview</p>
			<p class="meta">{{ stitchedImageInfo }}</p>
			<img :src="stitchedImageUrl" alt="Selected area stitched from map tiles" />
			<a :href="stitchedImageUrl" target="_blank" rel="noopener">Open full image</a>
		</template>
	</div>
	<div id="map"></div>
</template>
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { Map } from 'maplibre-gl';
import { useRouter, type Router } from 'vue-router';
const router: Router = useRouter();
import AreaSelect from '@/components/AreaSelect.vue';

type SelectionRect = {
	x0: number;
	y0: number;
	x1: number;
	y1: number;
	width: number;
	height: number;
};

const apiBase = (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, '') ?? 'http://localhost:8080';
const API_URL = `${apiBase}/api/v1/tiles/stitch`;
const map = ref<any>(null);

const selecting = ref(false);
const isFetching = ref(false);
const fetchError = ref('');
const stitchedImageUrl = ref<string | null>(null);
const stitchedImageInfo = ref('');
const sessionId = ref<string | null>(null);

onMounted(() => {
	map.value = new Map({
		container: 'map',
		center: [-122.420679, 37.772537],
		zoom: 13,
		hash: true,
		style: {
			version: 8,
			sources: {
				'esri-satellite': {
					type: 'raster',
					tiles: [
						'https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
					],
					tileSize: 256,
					attribution: 'Tiles © Esri',
				},
			},
			layers: [
				{
					id: 'esri-satellite',
					type: 'raster',
					source: 'esri-satellite',
				},
			],
		},
	});
});

onBeforeUnmount(() => {
	if (stitchedImageUrl.value) {
		URL.revokeObjectURL(stitchedImageUrl.value);
	}
	if (map.value) {
		map.value.remove();
		map.value = null;
	}
});

async function handleSelectionComplete(rect: SelectionRect): Promise<void> {
	if (!map.value) {
		return;
	}

	selecting.value = false;
	fetchError.value = '';
	stitchedImageInfo.value = '';
	isFetching.value = true;

	try {
		const northWest = map.value.unproject([rect.x0, rect.y0]);
		const southEast = map.value.unproject([rect.x1, rect.y1]);

		const minLng = Math.min(northWest.lng, southEast.lng);
		const maxLng = Math.max(northWest.lng, southEast.lng);
		const minLat = Math.min(northWest.lat, southEast.lat);
		const maxLat = Math.max(northWest.lat, southEast.lat);
		const baseZoom = Math.round(map.value.getZoom());
		const preferredZoom = Math.min(19, baseZoom + 1);
		const fallbackZoom = Math.max(1, baseZoom);

		const stitchedResult = await fetchStitchedImageWithFallback(
			minLng,
			minLat,
			maxLng,
			maxLat,
			preferredZoom,
			fallbackZoom,
		);

		const { blob, usedZoom, sessionIdHeader } = stitchedResult;
		const bitmap = await createImageBitmap(blob);
		stitchedImageInfo.value = `${bitmap.width} x ${bitmap.height} px @ z${usedZoom}`;
		bitmap.close();

		if (stitchedImageUrl.value) {
			URL.revokeObjectURL(stitchedImageUrl.value);
		}
		stitchedImageUrl.value = URL.createObjectURL(blob);
		sessionId.value = sessionIdHeader;
		if (!sessionId.value) {
			throw new Error('Missing session id in stitch response header.');
		}
	} catch (error) {
		fetchError.value = error instanceof Error ? error.message : 'Failed to stitch selected area image.';
		sessionId.value = null;
	} finally {
		isFetching.value = false;
	}
}

async function fetchStitchedImageWithFallback(
	minLng: number,
	minLat: number,
	maxLng: number,
	maxLat: number,
	preferredZoom: number,
	fallbackZoom: number,
): Promise<{ blob: Blob; usedZoom: number; sessionIdHeader: string }> {
	const zoomCandidates = preferredZoom === fallbackZoom ? [preferredZoom] : [preferredZoom, fallbackZoom];
	let lastError = 'Failed to stitch selected area image.';

	for (const zoom of zoomCandidates) {
		const params = new URLSearchParams({
			minLng: minLng.toString(),
			minLat: minLat.toString(),
			maxLng: maxLng.toString(),
			maxLat: maxLat.toString(),
			zoom: zoom.toString(),
		});

		const response = await fetch(`${API_URL}?${params.toString()}`);
		if (!response.ok) {
			const errorText = await response.text();
			lastError = errorText || `Tile stitch failed with status ${response.status}`;
			continue;
		}

		const sessionIdHeader = response.headers.get('x-session-id') ?? response.headers.get('X-Session-Id');
		if (!sessionIdHeader) {
			lastError = 'Missing session id in stitch response header.';
			continue;
		}

		const contentType = response.headers.get('content-type') ?? '';
		if (!contentType.startsWith('image/')) {
			const text = await response.text();
			lastError = text || `Expected image response but got ${contentType || 'unknown content-type'}`;
			continue;
		}

		const blob = await response.blob();
		if (blob.size === 0) {
			lastError = 'Tile stitch returned an empty image.';
			continue;
		}

		return { blob, usedZoom: zoom, sessionIdHeader };
	}

	throw new Error(lastError);
}

function submitForAnalysis() {
	if (!sessionId.value) return;
	
	router.push({ name: 'analysis', query: { sessionId: sessionId.value } });
}

</script>
<style scoped>
#map {
	position: fixed;
	top: 0;
	bottom: 0;
	width: 100vw;
	height: 100vh;

}
.buttons {
	position: absolute;
	display: flex;
	flex-direction: column;
	right: 10px;
	top: 10px;
	background-color: color-mix(in srgb, var(--sec) 80%, transparent);
	border: 3px solid rgba(255, 255, 255, 0.1);
	border-radius: 8px;
	z-index: 105;
}

.buttons button {
	background: none;
	border: none;
	padding: 10px;
	margin: 0;
	font-size: 20px;

	border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.buttons button:first-child:hover {
	border-radius: 8px 8px 0 0;
}
.buttons button:last-child:hover {
	border-radius: 0 0 8px 8px;
}


.buttons button:last-child {
	border-bottom: none;
}

.buttons button:hover {
	background-color: var(--sec);
}
.buttons .active {
	background-color: var(--accent) !important;
}
.buttons button:first-child.active {
	border-radius: 8px 8px 0 0;
}
.buttons button:last-child.active {
	border-radius: 0 0 8px 8px;
}

.desciptor {
	position: absolute;
	bottom: 20px;
	left: 50%;
	transform: translateX(-50%);
	background-color: color-mix(in srgb, var(--sec) 80%, transparent);
	border: 3px solid rgba(255, 255, 255, 0.1);
	border-radius: 8px;
	padding: 10px;
	z-index: 115;
	text-align: center;
}

.resultCard {
	position: absolute;
	left: 10px;
	bottom: 20px;
	width: min(420px, calc(100vw - 20px));
	background-color: color-mix(in srgb, var(--sec) 88%, transparent);
	border: 3px solid rgba(255, 255, 255, 0.1);
	border-radius: 8px;
	padding: 10px;
	z-index: 115;
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.resultCard img {
	width: 100%;
	max-height: 220px;
	object-fit: contain;
	border-radius: 6px;
	border: 1px solid rgba(255, 255, 255, 0.2);
}

.resultCard a {
	color: var(--accent);
	text-decoration: underline;
	font-size: 14px;
}

.meta {
	font-size: 13px;
	opacity: 0.8;
}

.error {
	color: var(--errorBorder);
}

.analysis-button {
	position: fixed;
	top: 10px;
	left: 10px;
	z-index: 120;
	padding: 10px 16px;
	border-radius: 8px;
	border: 2px solid rgba(255, 255, 255, 0.2);
	background: color-mix(in srgb, var(--goodBorder) 35%, var(--sec));
	font-weight: 600;
	cursor: pointer;
}

.analysis-button:hover {
	filter: brightness(1.1);
}
</style>
