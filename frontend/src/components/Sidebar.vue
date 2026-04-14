<template>
	<div
		class="buttons"
		:class="{
			'isOpen': isOpen,
		}"
	>
		<button
			@click="isOpen=!isOpen"
		>
			<i class="fa-solid fa-list"></i>
		</button>
		<button @click="$emit('zoomIn')"><i class="fa-solid fa-plus"></i></button>
		<button @click="$emit('zoomOut')"><i class="fa-solid fa-minus"></i></button>
		<button @click="$emit('resetZoom')"><i class="fa-solid fa-crosshairs"></i></button>
		<button @click="$emit('reportOpen')"><i class="fa-solid fa-file-lines"></i></button>
	</div>
	<div
		class="sidebar"
		:class="{
			'isOpen': isOpen,
		}"
	>
		<!-- type can be found in contracts folder -->
		<SidebarSection
			title="Vegetation Health"
			icon="fa-solid fa-tree"
			color="#00ed9e"
		>
			<CategoricalData :value="analysis.vegetation_health.status">status</CategoricalData>
			<PercentData :value="analysis.vegetation_health.canopy_density_percent">canopy density</PercentData>
			<PercentData :value="analysis.vegetation_health.greenness_index">greenness index</PercentData>
			<p>{{ analysis.vegetation_health.explanation }}</p>
		</SidebarSection>
		<SidebarSection
			title="Crop Health"
			icon="fa-solid fa-seedling"
			color="#ffd700"
		>
			<CategoricalData :value="analysis.crop_health.status">status</CategoricalData>
			<CategoricalData v-if="analysis.crop_health.main_issue != 'none'" :value="analysis.crop_health.main_issue">main issue</CategoricalData>
			<PercentData negative :value="analysis.crop_health.stressed_area_percent">stressed area</PercentData>
			<p>{{ analysis.crop_health.explanation }}</p>
		</SidebarSection>
		<SidebarSection
			title="Deforestation"
			icon="fa-solid fa-helmet-safety"
			color="#009967"
		>
			<PercentData :value="analysis.deforestation.tree_cover_percent">tree cover</PercentData>
			<PercentData :value="analysis.deforestation.recent_loss_percent">recent loss</PercentData>
			<p>{{ analysis.deforestation.explanation }}</p>
		</SidebarSection>
		<SidebarSection
			title="Water Quality"
			icon="fa-solid fa-water"
			color="#6180bf"
		>
			<CategoricalData :value="analysis.water_quality.status">status</CategoricalData>
			<BooleanData :value="analysis.water_quality.visible_pollution">visible pollution</BooleanData>
			<p>{{ analysis.water_quality.explanation }}</p>
		</SidebarSection>
		<SidebarSection
			title="Coastal Erosion"
			icon="fa-solid fa-ship"
			color="#FF9467"
		>
			<BooleanData :value="analysis.coastal_erosion.erosion_detected">status</BooleanData>
			<CategoricalData :value="analysis.coastal_erosion.severity">erosion rate</CategoricalData>
			<p>{{ analysis.coastal_erosion.explanation }}</p>
		</SidebarSection>
		<SidebarSection
			title="Disaster Indicators"
			icon="fa-solid fa-triangle-exclamation"
			color="#ff4c71"
		>
			<BooleanData :value="analysis.disaster_indicators.flooding">flood risk</BooleanData>
			<BooleanData :value="analysis.disaster_indicators.fire_scar">fire risk</BooleanData>
			<BooleanData :value="analysis.disaster_indicators.landslide">landslide risk</BooleanData>
			<CategoricalData v-if="analysis.disaster_indicators.other" :value="analysis.disaster_indicators.other">other risks</CategoricalData>
			<p>{{ analysis.disaster_indicators.explanation }}</p>
		</SidebarSection>
		<SidebarSection
			title="Maritime Features"
			icon="fa-solid fa-anchor"
			color="#7EE0FF"
		>
			<CategoricalData :value="analysis.maritime_features.ships_detected.toString()">ship traffic</CategoricalData>
			<BooleanData :value="analysis.maritime_features.oil_spill">oil spill</BooleanData>
			<p>{{ analysis.maritime_features.explanation }}</p>
		</SidebarSection>
	</div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import type { AnalysisOutput } from '@stratvis/contracts';
import CategoricalData from './CategoricalData.vue';
import PercentData from './PercentData.vue';
import BooleanData from './BooleanData.vue';
import SidebarSection from './SidebarSection.vue';

defineEmits<{
	zoomIn: [];
	zoomOut: [];
	resetZoom: [];
	reportOpen: [];
}>();

const isOpen = ref(true);

defineProps<{
	analysis: AnalysisOutput;
}>();

</script>
<style scoped>
.sidebar {
	width: 400px;
	height: calc(100vh - 20px);
	box-sizing: border-box;

	transition: right 0.2s ease;
	right: -400px;
	top: 10px;
	position: fixed;
	background-color: color-mix(in srgb, var(--sec) 80%, transparent);
	backdrop-filter: blur(10px);
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.9);

	border: 3px solid rgba(255, 255, 255, 0.1);
	border-radius: 8px;
	padding: 16px;
	padding-bottom: 80px;

	overflow-y: auto;
	z-index: 100;
}

.sidebar.isOpen {
	right: 10px;
}

.buttons {
	position: absolute;
	display: flex;
	flex-direction: column;
	transition: right 0.2s ease;
	right: 10px;
	top: 10px;
	background-color: color-mix(in srgb, var(--sec) 80%, transparent);
	border: 3px solid rgba(255, 255, 255, 0.1);
	border-radius: 8px;
	z-index: 100;
}

.buttons.isOpen {
	right: 420px;
}

.buttons button {
	background: none;
	border: none;
	padding: 10px;
	margin: 0;
	font-size: 20px;

	border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.buttons button:last-child {
	border-bottom: none;
}

.buttons button:hover {
	background-color: var(--sec);
}

</style>
