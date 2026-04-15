<template>
	<div
		class="background"
		@mousedown="start"
		@mouseup="end"
		@mouseleave="cancel"
		@mousemove="move"
	></div>
	<div v-if="selectionRect" class="selection" :style="selectionStyle">
		<div class="endHandle handle"></div>
	</div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
const startPoint = ref<{ x: number; y: number } | null>(null);
const currentPoint = ref<{ x: number; y: number } | null>(null);
let dragging = false;

const selectionRect = computed(() => {
	if (!startPoint.value || !currentPoint.value) {
		return null;
	}

	const viewportWidth = window.innerWidth;
	const viewportHeight = window.innerHeight;

	const minX = Math.min(startPoint.value.x, currentPoint.value.x);
	const maxX = Math.max(startPoint.value.x, currentPoint.value.x);
	const minY = Math.min(startPoint.value.y, currentPoint.value.y);
	const maxY = Math.max(startPoint.value.y, currentPoint.value.y);

	const x0 = Math.floor(Math.max(0, Math.min(viewportWidth, minX)));
	const x1 = Math.ceil(Math.max(0, Math.min(viewportWidth, maxX)));
	const y0 = Math.floor(Math.max(0, Math.min(viewportHeight, minY)));
	const y1 = Math.ceil(Math.max(0, Math.min(viewportHeight, maxY)));

	return {
		x0,
		x1,
		y0,
		y1,
		viewportWidth,
		viewportHeight,
		width: Math.max(0, x1 - x0),
		height: Math.max(0, y1 - y0),
	};
});

const selectionStyle = computed(() => {
	if (!selectionRect.value) {
		return {};
	}
	return {
		left: `${selectionRect.value.x0}px`,
		top: `${selectionRect.value.y0}px`,
		width: `${selectionRect.value.width}px`,
		height: `${selectionRect.value.height}px`,
	};
});

const start = (e: MouseEvent) => {
	
	startPoint.value = { x: e.clientX, y: e.clientY };
	currentPoint.value = null;
	dragging = true;
};

const end = (e: MouseEvent) => {
	
	if (startPoint.value) {
		currentPoint.value = { x: e.clientX, y: e.clientY };
		// Here you can emit an event with the selected rectangle coordinates
	}
	dragging = false;
};

const cancel = () => {
	
	if (!dragging) return;
	startPoint.value = null;
	currentPoint.value = null;
	dragging = false;
};

const move = (e: MouseEvent) => {
	if (!dragging) return;
	
	if (startPoint.value) {
		currentPoint.value = { x: e.clientX, y: e.clientY };
	}
};

</script>
<style scoped>
.background {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	cursor: crosshair;
	background-color: rgba(0, 0, 0, 0.1);
	z-index: 100;
}

.selection {
	position: absolute;
	border: 2px dashed #007bff;
	background-color: rgba(0, 123, 255, 0.08);
	box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
	isolation: isolate;
	mix-blend-mode: normal;
	z-index: 101;
	pointer-events: none;
}

.handle {
	width: 10px;
	height: 10px;
	background-color: #007bff;
	border-radius: 50%;
	z-index: 102;
	cursor: nwse-resize;
}
.handle:hover {
	background-color: pink;
}
.endHandle {
	position: absolute;
	right: -5px;
	bottom: -5px;
}

</style>
