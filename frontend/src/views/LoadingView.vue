<!-- A simple loading spinner component -->
<template>
	<div
		class="loading"
		:class="{ transparent, showing }"
	>
		<i class="fa-solid fa-spinner"></i>
		<p><slot/></p>
	</div>
</template>
<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue';
const props = defineProps<{
	transparent?: boolean;
	transIn?: boolean;
	transOut?: boolean;
}>();

const showing = ref(false);
onMounted(() => {
	if (!props.transIn) {
		showing.value = true;
		return;
	}
	if (props.transOut) {
		showing.value = true;
		nextTick(() => {
			requestAnimationFrame(() => {
				showing.value = false;
			});
		})
	}
	nextTick(() => {
		requestAnimationFrame(() => {
			showing.value = true;
		});
	})
});
</script>
<style scoped>
.loading {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: var(--prim);
	z-index: 10000;

	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	opacity: 0;
	transition: opacity 0.5s ease;
}

.showing {
	opacity: 1;
}

.loading i {
	animation: spin 1s linear infinite;
}

.transparent {
	background-color: color-mix(in srgb, var(--sec) 80%, transparent);
	backdrop-filter: blur(20px);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
