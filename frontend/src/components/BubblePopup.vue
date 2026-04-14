<template>
	<div
		class="bubble-popup"
		:class="{
			closing,
		}"
	>
		<slot />
	</div>
	<button
		class="close-button"
		:class="{
			closing,
		}"
		@click="close"
	>
		<i class="fa-solid fa-xmark"></i>
	</button>
	<div
		class="shadow"
		:class="{
			closing,
		}"
	></div>
</template>
<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue';
const emit = defineEmits<{
	(e: 'close'): void;
}>();

const closing = ref(true);

onMounted(() => {
	nextTick(() => {
		requestAnimationFrame(() => {
			closing.value = false;
		});
	});
})


async function close(): Promise<void> {
	closing.value = true;
	await new Promise((resolve) => setTimeout(resolve, 200));
	emit('close');
}
</script>
<style scoped>
.bubble-popup {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	background: var(--sec);
	border: 1px solid var(--border);
	border-radius: 12px;
	padding: 25px;
	z-index: 200;

	height: 90vh;

	overflow-y: auto;
}

.shadow {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.45);
	backdrop-filter: blur(10px);
	z-index: 150;
}

.close-button {
	position: absolute;
	top: 10px;
	right: 10px;
	background: transparent;
	border: none;
	font-size: 20px;
	color: var(--text);
	cursor: pointer;
	z-index: 250;
}

.bubble-popup, shadow {
	transition: opacity 0.2s ease;
}

.closing {
	opacity: 0;
}

.shadow.closing {
	backdrop-filter: blur(0);
}
</style>
