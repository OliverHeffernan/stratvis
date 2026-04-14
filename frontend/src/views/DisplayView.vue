<template>
	<div
		class="container"
		@mousedown="startDrag"
		@mouseup="endDrag"
		@mousemove="handleMove"
		@mouseleave="endDrag"
	>
		<canvas
			class="imgCanvas"
			ref="canvasRef"
			:style="{
				transform: `translate(${pan.x}px, ${pan.y}px)`,
				scale: zoom,
			}"
		></canvas>
	</div>
	<Sidebar
		:analysis="analysis"
		@zoomIn="zoom *= 1.2"
		@zoomOut="zoom /= 1.2"
		@resetZoom="zoom = 1; pan = { x: 0, y: 0 }"
	/>
</template>
<style scoped>
.container {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	cursor: grab;

	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
}

.imgCanvas {
	transition: scale 0.1s ease;
}
</style>

<script setup lang="ts">
import type { AnalysisOutput } from '@stratvis/contracts';
import Sidebar from '../components/Sidebar.vue';
import { onMounted, ref, watch, type Ref } from 'vue';
const props = defineProps<{
	image: ImageBitmap | null;
	analysis: AnalysisOutput;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
const pan: Ref<{ x: number; y: number }> = ref({ x: 0, y: 0 });
const zoom: Ref<number> = ref(1);

onMounted(() => {
	if (props.image) {
		drawBitmap(props.image);
	}
});

watch(
	() => props.image,
	(newImage) => {
		if (newImage) {
			drawBitmap(newImage);
		}
	},
	{ flush: 'post' },
);

function drawBitmap(bitmap: ImageBitmap): void {
	const canvas: HTMLCanvasElement | null = canvasRef.value;
	if (!canvas) {
		console.error('Canvas element not found');
		return;
	}
	canvas.width = bitmap.width;
	canvas.height = bitmap.height;
	const ctx: CanvasRenderingContext2D | null = canvas.getContext('2d');
	if (!ctx) {
		console.error('Failed to get 2D context');
		return;
	}
	ctx.drawImage(bitmap, 0, 0);
}

const isDragging = ref(false);

const startDrag = (e: MouseEvent) => {
	isDragging.value = true;
};

const endDrag = (e: MouseEvent) => {
	isDragging.value = false;
};

const handleMove = (e: MouseEvent) => {
	if (!isDragging.value) return;
	pan.value.x += e.movementX / zoom.value;
	pan.value.y += e.movementY / zoom.value;
}
</script>
