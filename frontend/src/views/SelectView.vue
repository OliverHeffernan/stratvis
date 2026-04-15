<template>
	<h1 class="desciptor">Choose an area to analyse using the <i class="fa-solid fa-crop"></i> button in the top right corner</h1>
	<AreaSelect v-if="selecting" />
	<div class="buttons">
		<button @click="map?.zoomIn()"><i class="fa-solid fa-plus"></i></button>
		<button @click="map?.zoomOut()"><i class="fa-solid fa-minus"></i></button>
		<button
			@click="selecting = !selecting"
			:class="{ active: selecting }"
		><i class="fa-solid fa-crop"></i></button>
	</div>
	<div id="map"></div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { Map } from 'maplibre-gl';
import AreaSelect from '@/components/AreaSelect.vue';
import { onMounted } from 'vue';
let map: Map | null = null;

const selecting = ref(false);
onMounted(() => {
	map = new Map({
		container: 'map',
		center: [-122.420679, 37.772537],
		zoom: 13,
		//style: 'https://demotiles.maplibre.org/style.json',
		hash: true,
		transformRequest: (url, resourceType)=> {
			if(resourceType === 'Source' && url.startsWith('http://myHost')) {
				return {
					url: url.replace('http', 'https'),
					headers: { 'my-custom-header': true},
					credentials: 'include'  // Include cookies for cross-origin requests
				}
			}
		},
	});

	map.addSource('esri-satellite', {
		type: 'raster',
		tiles: [
			'https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
		],
		tileSize: 256,
		attribution: 'Tiles © Esri',
	})
	map.addLayer({
		id: 'esri-satellite',
		type: 'raster',
		source: 'esri-satellite',
	})
});
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
</style>
