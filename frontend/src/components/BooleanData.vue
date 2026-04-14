<template>
	<CategoricalData :value="value ? 'True' : 'False'">
		<slot />
	</CategoricalData>
</template>
<script setup lang="ts">
import CategoricalData from './CategoricalData.vue';
defineProps<{
	value: boolean;
}>();
</script>
