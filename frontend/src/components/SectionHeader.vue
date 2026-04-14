<template>
	<div class="section-header">
		<div class="section-icon">
			<i :class="icon"></i>
		</div>
		<h2 class="section-title"><slot /></h2>
	</div>

</template>
<script setup lang="ts">
defineProps<{
	icon: string;
}>();
</script>

<style scoped>
.section-header {
	display: flex;
	align-items: center;
	margin-bottom: 16px;
	gap: 12px;
}

.section-icon {
	padding: 8px;
	font-size: 20px;
	height: 25px;
	width: 25px;
	border-radius: 50%;
	border-width: 3px;
	border-style: solid;
	border-color: var(--primary);

	display: flex;
	align-items: center;
	justify-content: center;
}
</style>
