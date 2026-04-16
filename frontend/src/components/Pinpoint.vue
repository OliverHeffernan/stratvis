<template>
	<div
		class="area"
		:style="{
			left: position.x * 100 + '%',
			top: position.y * 100 + '%',
			width: pinpoint.range + 'px',
			height: pinpoint.range + 'px',
		}"
	></div>
	<div
		class="pinContainer"
		:style="{
			left: position.x * 100 + '%',
			top: position.y * 100 + '%',
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
import { computed } from 'vue';
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

const position = computed(() => {
	const guesses = props.pinpoint.points;
	const guessCount = guesses.length;

	const avgX = guesses.reduce((sum, p) => sum + p.x, 0) / guessCount;
	const avgY = guesses.reduce((sum, p) => sum + p.y, 0) / guessCount;

	return {
		x: clamp01(avgX),
		y: clamp01(avgY),
	};
});

function clamp01(value: number): number {
	return Math.max(0, Math.min(1, value));
}

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

.area {
	position: absolute;
	border: 2px solid red;
	border-radius: 50%;
	transform: translate(-50%, -50%);
	opacity: 0.3;
	pointer-events: none;
}
</style>
