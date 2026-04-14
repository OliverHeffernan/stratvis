<template>

	<BubblePopup>
		<RenderedMarkdown :markdown="analysis.report" />
	</BubblePopup>
	
	<div
		class="container"
		:class="{ dragging: isDragging }"
		@mousedown="startDrag"
		@mouseup="endDrag"
		@mousemove="handleMove"
		@mouseleave="endDrag"
	>
		<div class="stage" :style="stageStyle">
			<canvas class="imgCanvas" ref="canvasRef"></canvas>
			<div
				class="pinsContainer"
				:style="{
					width: `${canvasRef?.width ?? 0}px`,
					height: `${canvasRef?.height ?? 0}px`,
					'--zoom': `${zoom}`,
				}"
			>
				<Pinpoint
					v-for="(point, index) in analysis.points_of_interest"
					:key="index"
					:pinpoint="point"
				/>
			</div>
		</div>
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
.dragging {
	cursor: grabbing;
}

.imgCanvas {
	display: block;
}

.stage {
	position: relative;
	transform-origin: center center;
}

.pinsContainer {
	position: absolute;
	inset: 0;
	z-index: 10;
	pointer-events: none;
}
</style>

<script setup lang="ts">
import type { AnalysisOutput } from '@stratvis/contracts';
import Sidebar from '@/components/Sidebar.vue';
import Pinpoint from '@/components/Pinpoint.vue';
import BubblePopup from '@/components/BubblePopup.vue';
import RenderedMarkdown from '@/components/RenderedMarkdown.vue';
import { computed, onMounted, ref, watch, type Ref } from 'vue';
const props = defineProps<{
	image: ImageBitmap | null;
	analysis: AnalysisOutput;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
const pan: Ref<{ x: number; y: number }> = ref({ x: 0, y: 0 });
const zoom: Ref<number> = ref(1);
const lastPointer: Ref<{ x: number; y: number } | null> = ref(null);
const stageStyle = computed(() => ({
	transform: `translate(${pan.value.x}px, ${pan.value.y}px) scale(${zoom.value})`,
}));

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
	lastPointer.value = { x: e.clientX, y: e.clientY };
};

const endDrag = () => {
	isDragging.value = false;
	lastPointer.value = null;
};

const handleMove = (e: MouseEvent) => {
	if (!isDragging.value || !lastPointer.value) return;
	const dx = e.clientX - lastPointer.value.x;
	const dy = e.clientY - lastPointer.value.y;
	pan.value.x += dx;
	pan.value.y += dy;
	lastPointer.value = { x: e.clientX, y: e.clientY };
};

</script>
