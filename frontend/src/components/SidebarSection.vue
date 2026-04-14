<template>
	<div class="sidebar-section">
		<SectionHeader :color="color" :icon="icon">
			{{ title }}
			<button @click="isOpen = !isOpen" class="toggle-button">
				<i v-if="isOpen" class="fa-solid fa-chevron-up"></i>
				<i v-else class="fa-solid fa-chevron-down"></i>
			</button>
		</SectionHeader>
		<div v-if="isOpen" class="section-content">
			<slot />
		</div>
	</div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import SectionHeader from './SectionHeader.vue';
const isOpen = ref(false);
defineProps<{
	title: string;
	icon: string;
	color?: string;
}>();
</script>
<style scoped>
.toggle-button {
	background: none;
	border: none;
	cursor: pointer;
	padding: 0;
	margin-left: auto;
	color: var(--primary);
	font-size: inherit;
	float: right;
}

.section-content {
	display: flex;
	flex-direction: column;
	gap: 12px;
	border-radius: 8px;
	background-color: var(--sec);
	padding: 12px;
	border: 1px solid rgba(255, 255, 255, 0.1);
}
</style>
