<template>
	<div class="section-header">
		<div
			class="section-icon"
			:style="{
				borderColor: color ? color : 'var(--primary)',
				color: color ? color : 'var(--primary)',
			}"
		>
			<i :class="icon"></i>
		</div>
		<h2 class="section-title"><slot /></h2>
	</div>

</template>
<script setup lang="ts">
defineProps<{
	icon: string;
	color?: string;
}>();
</script>

<style scoped>
.section-header {
	display: flex;
	align-items: center;
	margin-bottom: 16px;
	gap: 12px;
	width: 100%;
}

.section-icon {
	padding: 8px;
	font-size: 20px;
	height: 25px;
	width: 25px;
	border-radius: 50%;
	border-width: 3px;
	border-style: solid;

	display: flex;
	align-items: center;
	justify-content: center;
}

.section-title {
	display: flex;
	justify-content: space-between;
	width: 100%;
}
</style>
