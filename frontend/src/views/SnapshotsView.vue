<template>
	<div class="gap"></div>
	<div class="container margins">
		<SessionContainer v-for="session in sessions" :key="session.sessionId" :session="session" />
	</div>
	<div class="gap"></div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { authFetch, clearAuthToken, getApiBase } from '@/lib/auth';
import SessionContainer from '@/components/SessionContainer.vue';
import type SessionInfo from '@/types/SessionInfo';
const sessions = ref<SessionInfo[]>([]);
const router = useRouter();

const apiBase = getApiBase();

onMounted(async () => {
	const response = await authFetch(`${apiBase}/api/v1/sessions/ids`);

	if (response.status === 401) {
		clearAuthToken();
		router.push({ name: 'login' });
		return;
	}

	if (!response.ok) {
		throw new Error(`Failed to fetch sessions with status ${response.status}`);
	}

	const payload = (await response.json()) as SessionInfo[];

	sessions.value = payload;
	for (const session of sessions.value) {
		session.dateObject = new Date(session.creationTime);
	}
	console.log(sessions.value);
});

</script>
<style scoped>
.gap {
	height: 80px;
}

.container {
	display: flex;
	flex-direction: column;
	gap: 20px;
}
</style>
