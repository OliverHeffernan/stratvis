<template>
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
	<div v-if="isFetching || isAnalyzing" class="loadingCard">
		<i class="fa-solid fa-spinner spin"></i>
		<p>{{ isAnalyzing ? 'Analysis loading...' : 'Preparing snapshot...' }}</p>
	</div>
	<div v-if="fetchError" class="errorCard">
		<p class="error">{{ fetchError }}</p>
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

const apiBase = ((import.meta.env.VITE_API_URL as string | undefined) ?? (import.meta.env.VITE_API_ROOT as string | undefined) ?? 'http://localhost:8080').replace(/\/$/, '');
const TILE_STITCH_URL = `${apiBase}/api/v1/tiles/stitch`;
const ANALYZE_SESSION_URL = `${apiBase}/api/v1/analyze-session`;
const map = ref<any>(null);

const selecting = ref(false);
const isFetching = ref(false);
const isAnalyzing = ref(false);
const fetchError = ref('');

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
	isFetching.value = true;
	isAnalyzing.value = false;

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

		const { sessionIdHeader } = stitchedResult;
		if (!sessionIdHeader) {
			throw new Error('Missing session id in stitch response header.');
		}

		isAnalyzing.value = true;
		const analyzeResponse = await fetch(`${ANALYZE_SESSION_URL}?sessionId=${encodeURIComponent(sessionIdHeader)}`, {
			method: 'POST',
		});
		if (!analyzeResponse.ok) {
			const bodyText = await analyzeResponse.text();
			throw new Error(bodyText || `Analyze session failed with status ${analyzeResponse.status}`);
		}

		router.push({ name: 'analysis', query: { sessionId: sessionIdHeader } });
	} catch (error) {
		fetchError.value = error instanceof Error ? error.message : 'Failed to stitch selected area image.';
	} finally {
		isFetching.value = false;
		isAnalyzing.value = false;
	}
}

async function fetchStitchedImageWithFallback(
	minLng: number,
	minLat: number,
	maxLng: number,
	maxLat: number,
	preferredZoom: number,
	fallbackZoom: number,
): Promise<{ usedZoom: number; sessionIdHeader: string }> {
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

		const response = await fetch(`${TILE_STITCH_URL}?${params.toString()}`);
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

		return { usedZoom: zoom, sessionIdHeader };
	}

	throw new Error(lastError);
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


.loadingCard {
	position: absolute;
	left: 10px;
	top: 10px;
	width: min(320px, calc(100vw - 20px));
	background-color: color-mix(in srgb, var(--sec) 88%, transparent);
	border: 3px solid rgba(255, 255, 255, 0.1);
	border-radius: 8px;
	padding: 10px;
	z-index: 115;
	display: flex;
	align-items: center;
	gap: 8px;
}

.error {
	color: var(--errorBorder);
}

.errorCard {
	position: absolute;
	left: 10px;
	bottom: 20px;
	width: min(360px, calc(100vw - 20px));
	background-color: color-mix(in srgb, var(--errorBG) 88%, transparent);
	border: 3px solid color-mix(in srgb, var(--errorBorder) 55%, transparent);
	border-radius: 8px;
	padding: 10px;
	z-index: 115;
}

.spin {
	animation: spin 1s linear infinite;
}

@keyframes spin {
	0% {
		transform: rotate(0deg);
	}
	100% {
		transform: rotate(360deg);
	}
}
</style>
