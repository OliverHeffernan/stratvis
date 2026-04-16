<template>
	<h1 v-if="!clicked" class="descriptor">Select an area to analyze by right-clicking and dragging from one corner to the opposite corner.</h1>
	<AreaSelect
		v-if="selecting"
		@complete="handleSelectionComplete"
		@cancelled="selecting = false"
		@allClick="clickedOff"
	/>
	<div class="buttons">
		<button @click="map?.zoomIn()"><i class="fa-solid fa-plus"></i></button>
		<button @click="map?.zoomOut()"><i class="fa-solid fa-minus"></i></button>
		<button
			@click="selecting = !selecting"
			:class="{ active: selecting }"
		><i class="fa-solid fa-crop"></i></button>
	</div>
	<LoadingView v-if="isFetching || isAnalyzing" transIn transparent>{{ isAnalyzing ? 'Analysis loading...' : 'Preparing snapshot...' }}</LoadingView>
	<div v-if="fetchError" class="errorCard">
		<p class="error">{{ fetchError }}</p>
	</div>
	<div v-if="quickSelectionStyle" class="quickSelection" :style="quickSelectionStyle"></div>
	<div id="map"></div>
</template>
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { Map } from 'maplibre-gl';
import { useRouter, type Router } from 'vue-router';
import { authFetch, clearAuthToken, getApiBase } from '@/lib/auth';
const router: Router = useRouter();
import AreaSelect from '@/components/AreaSelect.vue';
import LoadingView from './LoadingView.vue';

const clicked = ref(false);

function clickedOff() {
	console.log('clickedOff');
	clicked.value = true;
}

type SelectionRect = {
	x0: number;
	y0: number;
	x1: number;
	y1: number;
	width: number;
	height: number;
};

const apiBase = getApiBase();
const TILE_STITCH_URL = `${apiBase}/api/v1/tiles/stitch`;
const ANALYZE_SESSION_URL = `${apiBase}/api/v1/analyze-session`;
const map = ref<any>(null);

const selecting = ref(false);
const isFetching = ref(false);
const isAnalyzing = ref(false);
const fetchError = ref('');
const quickSelectionStart = ref<{ x: number; y: number } | null>(null);
const quickSelectionCurrent = ref<{ x: number; y: number } | null>(null);
let quickSelectionDragging = false;

const quickSelectionRect = computed<SelectionRect | null>(() => {
	if (!quickSelectionStart.value || !quickSelectionCurrent.value) {
		return null;
	}

	const viewportWidth = window.innerWidth;
	const viewportHeight = window.innerHeight;
	const minX = Math.min(quickSelectionStart.value.x, quickSelectionCurrent.value.x);
	const maxX = Math.max(quickSelectionStart.value.x, quickSelectionCurrent.value.x);
	const minY = Math.min(quickSelectionStart.value.y, quickSelectionCurrent.value.y);
	const maxY = Math.max(quickSelectionStart.value.y, quickSelectionCurrent.value.y);

	const x0 = Math.floor(Math.max(0, Math.min(viewportWidth, minX)));
	const x1 = Math.ceil(Math.max(0, Math.min(viewportWidth, maxX)));
	const y0 = Math.floor(Math.max(0, Math.min(viewportHeight, minY)));
	const y1 = Math.ceil(Math.max(0, Math.min(viewportHeight, maxY)));

	return {
		x0,
		x1,
		y0,
		y1,
		width: Math.max(0, x1 - x0),
		height: Math.max(0, y1 - y0),
	};
});

const quickSelectionStyle = computed<Record<string, string> | null>(() => {
	if (!quickSelectionRect.value || selecting.value) {
		return null;
	}
	return {
		left: `${quickSelectionRect.value.x0}px`,
		top: `${quickSelectionRect.value.y0}px`,
		width: `${quickSelectionRect.value.width}px`,
		height: `${quickSelectionRect.value.height}px`,
	};
});

let mapCanvas: HTMLCanvasElement | null = null;
let onCanvasContextMenu: ((event: MouseEvent) => void) | null = null;
let onCanvasMouseDown: ((event: MouseEvent) => void) | null = null;
let onWindowMouseMove: ((event: MouseEvent) => void) | null = null;
let onWindowMouseUp: ((event: MouseEvent) => void) | null = null;
let onWindowKeyDown: ((event: KeyboardEvent) => void) | null = null;

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

	map.value.dragRotate.disable();
	map.value.touchZoomRotate.disableRotation();
	const canvas = map.value.getCanvas();
	mapCanvas = canvas;

	onCanvasContextMenu = (event: MouseEvent) => {
		event.preventDefault();
	};

	onCanvasMouseDown = (event: MouseEvent) => {
		if (event.button !== 2 || selecting.value) {
			return;
		}
		event.preventDefault();
		quickSelectionDragging = true;
		clickedOff();
		quickSelectionStart.value = { x: event.clientX, y: event.clientY };
		quickSelectionCurrent.value = { x: event.clientX, y: event.clientY };
	};

	onWindowMouseMove = (event: MouseEvent) => {
		if (!quickSelectionDragging || !quickSelectionStart.value || selecting.value) {
			return;
		}
		quickSelectionCurrent.value = { x: event.clientX, y: event.clientY };
	};

	onWindowMouseUp = (event: MouseEvent) => {
		if (event.button !== 2 || !quickSelectionDragging || selecting.value) {
			return;
		}
		event.preventDefault();
		quickSelectionCurrent.value = { x: event.clientX, y: event.clientY };
		const rect = quickSelectionRect.value;
		quickSelectionDragging = false;
		quickSelectionStart.value = null;
		quickSelectionCurrent.value = null;
		if (rect && rect.width > 0 && rect.height > 0) {
			void handleSelectionComplete(rect);
		}
	};

	onWindowKeyDown = (event: KeyboardEvent) => {
		if (event.key !== 'Escape') {
			return;
		}
		quickSelectionDragging = false;
		quickSelectionStart.value = null;
		quickSelectionCurrent.value = null;
	};

	canvas.addEventListener('contextmenu', onCanvasContextMenu);
	canvas.addEventListener('mousedown', onCanvasMouseDown);
	window.addEventListener('mousemove', onWindowMouseMove);
	window.addEventListener('mouseup', onWindowMouseUp);
	window.addEventListener('keydown', onWindowKeyDown);
});

onBeforeUnmount(() => {
	if (mapCanvas && onCanvasContextMenu) {
		mapCanvas.removeEventListener('contextmenu', onCanvasContextMenu);
	}
	if (mapCanvas && onCanvasMouseDown) {
		mapCanvas.removeEventListener('mousedown', onCanvasMouseDown);
	}
	if (onWindowMouseMove) {
		window.removeEventListener('mousemove', onWindowMouseMove);
	}
	if (onWindowMouseUp) {
		window.removeEventListener('mouseup', onWindowMouseUp);
	}
	if (onWindowKeyDown) {
		window.removeEventListener('keydown', onWindowKeyDown);
	}
	mapCanvas = null;
	onCanvasContextMenu = null;
	onCanvasMouseDown = null;
	onWindowMouseMove = null;
	onWindowMouseUp = null;
	onWindowKeyDown = null;
	quickSelectionDragging = false;
	quickSelectionStart.value = null;
	quickSelectionCurrent.value = null;

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
		const analyzeResponse = await authFetch(`${ANALYZE_SESSION_URL}?sessionId=${encodeURIComponent(sessionIdHeader)}`, {
			method: 'POST',
		});
		if (analyzeResponse.status === 401) {
			clearAuthToken();
			router.push({ name: 'login' });
			return;
		}
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

		const response = await authFetch(`${TILE_STITCH_URL}?${params.toString()}`);
		if (response.status === 401) {
			clearAuthToken();
			router.push({ name: 'login' });
			throw new Error('Authentication required. Please sign in again.');
		}
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

.descriptor {
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
	pointer-events: none;
}


.loadingCard {
	position: fixed;
	left: 0;
	top: 0;
	width: 100vw;
	height: 100vh;
	background-color: color-mix(in srgb, var(--sec) 88%, transparent);
	border: 3px solid rgba(255, 255, 255, 0.1);
	border-radius: 8px;
	padding: 10px;
	z-index: 115;
	display: flex;
	align-items: center;
	text-align: center;
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

.quickSelection {
	position: fixed;
	border: 2px dashed #007bff;
	background-color: rgba(0, 123, 255, 0.08);
	box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
	isolation: isolate;
	mix-blend-mode: normal;
	z-index: 112;
	pointer-events: none;
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
