<script setup lang="ts">
defineProps<{value: string}>();
</script>
<template>
	<div class="categorical-data">
		<p><b><slot/>:</b> {{ value }}</p>
	</div>

</template>
<style scoped>
.categorical-data {
	display: flex;
	gap: 8px;
	font-size: 16px;
}

.categorical-data p {
	margin: 0;
}
</style>
