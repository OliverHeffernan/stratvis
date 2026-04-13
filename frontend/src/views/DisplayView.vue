<template>
	<canvas ref="canvasRef"></canvas>
</template>

<script setup lang="ts">
import type { AnalysisOutput } from '@stratvis/contracts';
import { onMounted, ref, watch } from 'vue';
const props = defineProps<{
	image: ImageBitmap | null;
	analysis: AnalysisOutput;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);

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
</script>
