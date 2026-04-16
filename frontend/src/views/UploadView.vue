<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

function startMapAnalysis(): void {
	router.push({ name: 'select' });
}
</script>

<template>
	<div class="container">
		<h1>Map-based analysis</h1>
		<p>Select an area on the map, run analysis, and view map pinpoints with uncertainty ranges.</p>
		<button type="button" @click="startMapAnalysis">Open map</button>
	</div>
</template>

<style scoped>
.container {
	position: fixed;
	inset: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 14px;
	text-align: center;
	padding: 24px;
}

button {
	padding: 10px 16px;
	border: 1px solid var(--btnBorder);
	border-radius: 8px;
	background: var(--btnBG);
	cursor: pointer;
}
</style>
