<template>
	<LoadingView v-if="isLoading" />
	<div class="message" v-else>
		<h2>You have been signed out.</h2>
	</div>
</template>
<script setup lang="ts">
import LoadingView from './LoadingView.vue';
import { logout } from '@/lib/auth';
import { ref, onMounted } from 'vue';
const isLoading = ref(true);

onMounted(async () => {
	await logout();
	isLoading.value = false;
});
</script>
<style scoped>
.message {
	position: absolute;
	top: 0;
	left: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	width: 100vw;
	height: 100vh;
	color: var(--primary);
}
</style>
