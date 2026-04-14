<template>
	<div class="container">
		<div class="progress-bar" :class="{ negative }">
			<div class="progress" :style="{ width: value + '%' }">
				{{ value }} %
			</div>
		</div>
	</div>
</template>
<script setup lang="ts">
defineProps<{
	value: number;
	negative?: boolean;
}>();
</script>
<style scoped>
.container {
	display: flex;
	flex-direction: row-reverse;
}
.progress-bar {
	width: 100%;
	height: 20px;
	background-color: var(--prim);
	border-radius: 10px;
	overflow: hidden;
}

.progress {
	height: 100%;
	background-color: var(--accent);
	transition: width 0.3s ease;
	box-sizing: border-box;
	padding: 0 15px;
}
</style>
