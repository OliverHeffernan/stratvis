<template>
	<div
		class="pinContainer"
		:style="{
			left: pinpoint.x + '%',
			top: pinpoint.y + '%',
		}"
	>
		<i
			class="fa-solid fa-location-pin"
			:style="{
				color: colorFromType(pinpoint.type),
				borderLeftColor: colorFromType(pinpoint.type),
			}"
		></i>
		<div class="bubble">
			<h4>{{pinpoint.message}}</h4>
			<p>{{pinpoint.long_message}}</p>
		</div>
	</div>
</template>
<script setup lang="ts">
import type { AnalysisOutput } from '@stratvis/contracts';
type PointOfInterest = AnalysisOutput['points_of_interest'][number];
type PinType = PointOfInterest['type'];

function colorFromType(type: PinType): string {
	switch (type) {
		case 2:
			return 'green';
		case 1:
			return 'orange';
		default:
			return 'red';
	}
}

const props = defineProps<{
	pinpoint: PointOfInterest;
}>();
</script>
<style scoped>
.pinContainer {
	position: absolute;
	transform: translate(-50%, -100%) scale(calc(1 / var(--zoom, 1)));
	transform-origin: bottom center;
	color: var(--primary);
	font-size: 24px;
	cursor: pointer;
	pointer-events: auto;
}

.pinContainer i {
	-webkit-text-stroke: 2px var(--text);
}

.pinContainer:hover .bubble {
	display: block;
}

.bubble {
	position: absolute;
	top: 28px;
	left: 15px;
	max-width: 300px;
	background-color: var(--sec);
	color: var(--text);
	padding: 8px 12px;
	border-radius: 0 8px 8px 0;
	white-space: nowrap;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.9);
	display: none;
	border-left-width: 2px;
	border-left-style: solid;
}

.bubble h4 {
	margin: 0;
	font-weight: bold;
}

.bubble p {
	margin: 4px 0 0;
	font-size: 14px;
	width: 100%;
	white-space: normal;
}
</style>
