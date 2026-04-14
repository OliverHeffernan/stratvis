<template>
	<div class="percent-data">
		<p><b><slot/>:</b></p>
		<ProgressBar :value="value" :negative="value < 50" />
	</div>
</template>
<script setup lang="ts">
import ProgressBar from './ProgressBar.vue';
defineProps<{
	value: number;
	negative?: boolean;
}>();
</script>
<style scoped>
.percent-data p {
	margin: 0;
}
</style>
