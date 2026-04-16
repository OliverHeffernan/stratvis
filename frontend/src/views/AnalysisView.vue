<template>
	<LoadingView v-if="isLoading" />
	<div v-else-if="error" class="errorScreen">
		<h2>Analysis failed</h2>
		<p>{{ error }}</p>
		<button @click="router.push({ name: 'select' })">Back to map</button>
	</div>
	<template v-else>
		<div id="analysis-map"></div>
		<Transition name="hoverInfoTransition">
			<div v-if="hoveredPoi" class="hoverInfo">
				<h4>{{ hoveredPoi.message }}</h4>
				<p>{{ hoveredPoi.longMessage }}</p>
			</div>
		</Transition>
		<Sidebar
			v-if="analysis"
			:analysis="analysis"
			@zoomIn="zoomIn"
			@zoomOut="zoomOut"
			@resetZoom="fitToSnapshot()"
			@reportOpen="reportOpen = true"
		/>
		<BubblePopup v-if="reportOpen && analysis" @close="reportOpen = false">
			<RenderedMarkdown :markdown="analysis.report" />
		</BubblePopup>
	</template>
</template>

<script setup lang="ts">
import maplibregl, { type Map } from 'maplibre-gl';
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import LoadingView from './LoadingView.vue';
import Sidebar from '@/components/Sidebar.vue';
import BubblePopup from '@/components/BubblePopup.vue';
import RenderedMarkdown from '@/components/RenderedMarkdown.vue';
import { authFetch, clearAuthToken, getApiBase } from '@/lib/auth';
import type Session from '@/types/Session';
import type { AnalysisWithMap, Snapshot } from '@/types/Session';

const route = useRoute();
const router = useRouter();

const apiBase = getApiBase();
const ANALYZE_SESSION_URL = `${apiBase}/api/v1/analyze-session`;

const isLoading = ref(true);
const error = ref('');
const analysis = ref<AnalysisWithMap | null>(null);
const snapshot = ref<Snapshot | null>(null);
const reportOpen = ref(false);
const hoveredPoi = ref<{ message: string; longMessage: string } | null>(null);

let map: Map | null = null;
const poiMarkers: maplibregl.Marker[] = [];
let hoveredRangeFeatureId: string | number | null = null;

onMounted(async () => {
	const sessionId = String(route.query.sessionId ?? '');
	if (!sessionId) {
		error.value = 'Missing sessionId in URL.';
		isLoading.value = false;
		return;
	}

	try {
		const response = await authFetch(`${ANALYZE_SESSION_URL}?sessionId=${encodeURIComponent(sessionId)}`, {
			method: 'POST',
		});

		if (response.status === 401) {
			clearAuthToken();
			router.push({ name: 'login' });
			return;
		}

		if (!response.ok) {
			throw new Error(`Analyze session failed with status ${response.status}`);
		}

		const payload = (await response.json()) as Session;
		const latestSnapshot = payload.snapshots.at(-1);
		if (!latestSnapshot) {
			throw new Error('Session has no snapshots to display.');
		}
		if (!latestSnapshot.analysis) {
			throw new Error('Analysis is not available for the latest snapshot.');
		}

		snapshot.value = latestSnapshot;
		analysis.value = latestSnapshot.analysis;
		isLoading.value = false;
		await nextTick();
		initMap();
	} catch (err) {
		error.value = err instanceof Error ? err.message : 'Unknown error occurred during analysis.';
		isLoading.value = false;
	}
});

onBeforeUnmount(() => {
	clearMarkers();
	if (map) {
		map.remove();
		map = null;
	}
});

function initMap(): void {
	if (!snapshot.value) {
		return;
	}

	console.log('init');
	map = new maplibregl.Map({
		container: 'analysis-map',
		center: [
			(snapshot.value.minLng + snapshot.value.maxLng) / 2,
			(snapshot.value.minLat + snapshot.value.maxLat) / 2,
		],
		zoom: Math.max(snapshot.value.usedZoom, 1),
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

	map.on('load', () => {
		renderSnapshotBounds();
		renderPoiRangeCircles();
		fitToSnapshot();
	});
}

function fitToSnapshot(): void {
	if (!map || !snapshot.value) {
		return;
	}
	const height = snapshot.value.maxLat - snapshot.value.minLat;
	const width = snapshot.value.maxLng - snapshot.value.minLng;
	const paddingFactorLng = 0.4;
	const paddingFactorLat = 0.02;

	map.fitBounds(
		[
			[snapshot.value.minLng, snapshot.value.minLat],
			[snapshot.value.maxLng + width * paddingFactorLng, snapshot.value.maxLat + height * paddingFactorLat],
		],
		{ padding: 40, duration: 0 },
	);
}

function zoomIn(): void {
	map?.zoomIn();
}

function zoomOut(): void {
	map?.zoomOut();
}

function renderSnapshotBounds(): void {
	if (!map || !snapshot.value) {
		return;
	}

	const boundsGeoJson: GeoJSON.FeatureCollection<GeoJSON.Polygon> = {
		type: 'FeatureCollection',
		features: [
			{
				type: 'Feature',
				geometry: {
					type: 'Polygon',
					coordinates: [[
						[snapshot.value.minLng, snapshot.value.minLat],
						[snapshot.value.maxLng, snapshot.value.minLat],
						[snapshot.value.maxLng, snapshot.value.maxLat],
						[snapshot.value.minLng, snapshot.value.maxLat],
						[snapshot.value.minLng, snapshot.value.minLat],
					]],
				},
				properties: {},
			},
		],
	};

	map.addSource('snapshot-bounds', {
		type: 'geojson',
		data: boundsGeoJson,
	});

	map.addLayer({
		id: 'snapshot-bounds-fill',
		type: 'fill',
		source: 'snapshot-bounds',
		paint: {
			'fill-color': '#3db2ff',
			'fill-opacity': 0.08,
		},
	});

	map.addLayer({
		id: 'snapshot-bounds-line',
		type: 'line',
		source: 'snapshot-bounds',
		paint: {
			'line-color': '#3db2ff',
			'line-width': 2,
		},
	});
}

function renderPoiRangeCircles(): void {
	if (!map || !analysis.value) {
		return;
	}

	const circleFeatures: GeoJSON.Feature<GeoJSON.Polygon>[] = [];
	let featureIndex = 0;
	for (const poi of analysis.value.points_of_interest) {
		if (typeof poi.lng !== 'number' || typeof poi.lat !== 'number') {
			continue;
		}
		const rangeMeters = typeof poi.range_m === 'number' ? poi.range_m : 0;
		if (rangeMeters <= 0) {
			continue;
		}

		circleFeatures.push({
			id: featureIndex++,
			type: 'Feature',
			geometry: makeCirclePolygon(poi.lng, poi.lat, rangeMeters),
			properties: {
				type: poi.type,
				message: poi.message,
				long_message: poi.long_message,
				lng: poi.lng,
				lat: poi.lat,
			},
		});
	}

	const circlesGeoJson: GeoJSON.FeatureCollection<GeoJSON.Polygon> = {
		type: 'FeatureCollection',
		features: circleFeatures,
	};

	map.addSource('poi-ranges', {
		type: 'geojson',
		data: circlesGeoJson,
	});

	map.addLayer({
		id: 'poi-ranges-fill',
		type: 'fill',
		source: 'poi-ranges',
		paint: {
			'fill-color': [
				'match',
				['get', 'type'],
				0, '#ff4c71',
				1, '#d98a00',
				2, '#00a86b',
				'#ff4c71',
			],
			'fill-opacity': [
				'case',
				['boolean', ['feature-state', 'hover'], false],
				0.28,
				0.18,
			],
		},
	});

	map.addLayer({
		id: 'poi-ranges-black-outline',
		type: 'line',
		source: 'poi-ranges',
		paint: {
			'line-color': '#000000',
			'line-opacity': 0.9,
			'line-width': [
				'case',
				['boolean', ['feature-state', 'hover'], false],
				10,
				8,
			],
		},
	});

	map.addLayer({
		id: 'poi-ranges-outline',
		type: 'line',
		source: 'poi-ranges',
		paint: {
			'line-color': '#ffffff',
			'line-opacity': 0.95,
			'line-width': [
				'case',
				['boolean', ['feature-state', 'hover'], false],
				6.5,
				5,
			],
		},
	});

	map.addLayer({
		id: 'poi-ranges-danger-line',
		type: 'line',
		source: 'poi-ranges',
		filter: ['==', ['get', 'type'], 0],
		paint: {
			'line-color': '#ff4c71',
			'line-width': [
				'case',
				['boolean', ['feature-state', 'hover'], false],
				2.6,
				1.8,
			],
		},
	});

	map.addLayer({
		id: 'poi-ranges-threat-line',
		type: 'line',
		source: 'poi-ranges',
		filter: ['==', ['get', 'type'], 1],
		paint: {
			'line-color': '#d98a00',
			'line-width': [
				'case',
				['boolean', ['feature-state', 'hover'], false],
				2.6,
				1.8,
			],
		},
	});

	map.addLayer({
		id: 'poi-ranges-opportunity-line',
		type: 'line',
		source: 'poi-ranges',
		filter: ['==', ['get', 'type'], 2],
		paint: {
			'line-color': '#00a86b',
			'line-width': [
				'case',
				['boolean', ['feature-state', 'hover'], false],
				2.6,
				1.8,
			],
		},
	});

	map.addLayer({
		id: 'poi-ranges-glow',
		type: 'line',
		source: 'poi-ranges',
		paint: {
			'line-color': '#ffffff',
			'line-opacity': [
				'case',
				['boolean', ['feature-state', 'hover'], false],
				0.35,
				0,
			],
			'line-width': 8,
			'line-blur': 1,
		},
	});

	map.on('mouseenter', 'poi-ranges-fill', () => {
		if (!map) {
			return;
		}
		map.getCanvas().style.cursor = 'pointer';
	});

	map.on('mouseleave', 'poi-ranges-fill', () => {
		if (!map) {
			return;
		}
		map.getCanvas().style.cursor = '';
		hoveredPoi.value = null;
		clearRangeHoverState();
	});

	map.on('mousemove', (event) => {
		if (!map) {
			return;
		}

		const features = map.queryRenderedFeatures(event.point, { layers: ['poi-ranges-fill'] });
		if (features.length === 0) {
			hoveredPoi.value = null;
			map.getCanvas().style.cursor = '';
			clearRangeHoverState();
			return;
		}
		map.getCanvas().style.cursor = 'pointer';

		const feature = features[0];
		if (!feature) {
			hoveredPoi.value = null;
			clearRangeHoverState();
			return;
		}

		const featureId = feature.id;
		if (featureId !== undefined && hoveredRangeFeatureId !== featureId) {
			clearRangeHoverState();
			map.setFeatureState({ source: 'poi-ranges', id: featureId }, { hover: true });
			hoveredRangeFeatureId = featureId;
		}

		const properties = feature.properties ?? {};
		const message = String(properties.message ?? 'Point of interest');
		const longMessage = String(properties.long_message ?? '');
		hoveredPoi.value = { message, longMessage };
	});

	map.on('mouseleave', 'poi-ranges-danger-line', clearRangeHoverState);
	map.on('mouseleave', 'poi-ranges-threat-line', clearRangeHoverState);
	map.on('mouseleave', 'poi-ranges-opportunity-line', clearRangeHoverState);
}

function clearRangeHoverState(): void {
	if (!map || hoveredRangeFeatureId == null) {
		return;
	}
	map.setFeatureState({ source: 'poi-ranges', id: hoveredRangeFeatureId }, { hover: false });
	hoveredRangeFeatureId = null;
}

function clearMarkers(): void {
	while (poiMarkers.length > 0) {
		poiMarkers.pop()?.remove();
	}
	clearRangeHoverState();
	hoveredPoi.value = null;
}

function makeCirclePolygon(lng: number, lat: number, radiusMeters: number, steps = 48): GeoJSON.Polygon {
	const earthRadius = 6378137;
	const latRad = (lat * Math.PI) / 180;
	const points: [number, number][] = [];

	for (let i = 0; i <= steps; i++) {
		const bearing = (2 * Math.PI * i) / steps;
		const dLat = (radiusMeters / earthRadius) * Math.cos(bearing);
		const dLng = (radiusMeters / (earthRadius * Math.cos(latRad))) * Math.sin(bearing);
		points.push([
			lng + (dLng * 180) / Math.PI,
			lat + (dLat * 180) / Math.PI,
		]);
	}

	return {
		type: 'Polygon',
		coordinates: [points],
	};
}

function escapeHtml(value: string): string {
	return value
		.replaceAll('&', '&amp;')
		.replaceAll('<', '&lt;')
		.replaceAll('>', '&gt;')
		.replaceAll('"', '&quot;')
		.replaceAll("'", '&#39;');
}
</script>

<style scoped>
#analysis-map {
	position: fixed;
	inset: 0;
	width: 100vw;
	height: 100vh;
}

.errorScreen {
	position: fixed;
	inset: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12px;
	background: var(--prim);
	padding: 24px;
	text-align: center;
}

.errorScreen button {
	padding: 10px 16px;
	border-radius: 8px;
	border: 1px solid var(--btnBorder);
	background: var(--btnBG);
	cursor: pointer;
}

.hoverInfo {
	position: fixed;
	left: 12px;
	bottom: 12px;
	z-index: 140;
	width: min(440px, calc(100vw - 24px));
	max-height: 42vh;
	overflow: auto;
	background-color: color-mix(in srgb, var(--sec) 92%, transparent);
	border: 2px solid rgba(255, 255, 255, 0.16);
	border-radius: 10px;
	padding: 10px 12px;
	box-shadow: 0 10px 30px rgba(0, 0, 0, 0.45);
	backdrop-filter: blur(8px);
}

.hoverInfo h4 {
	margin: 0 0 6px;
	font-size: 15px;
	font-weight: 700;
}

.hoverInfo p {
	margin: 0;
	font-size: 13px;
	line-height: 1.4;
}

.hoverInfoTransition-enter-active,
.hoverInfoTransition-leave-active {
	transition: opacity 0.2s ease, transform 0.2s ease;
}

.hoverInfoTransition-enter-from,
.hoverInfoTransition-leave-to {
	opacity: 0;
	transform: translateY(8px);
}

.hoverInfoTransition-enter-to,
.hoverInfoTransition-leave-from {
	opacity: 1;
	transform: translateY(0);
}

:global(.poi-marker) {
	width: 14px;
	height: 14px;
	border-radius: 50%;
	border: 2px solid #ffffff;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
	cursor: pointer;
}

:global(.poi-popup h4) {
	margin: 0 0 6px;
	font-size: 14px;
	font-weight: 700;
	color: #111;
}

:global(.poi-popup p) {
	margin: 0;
	font-size: 13px;
	color: #222;
	max-width: 300px;
}
</style>
