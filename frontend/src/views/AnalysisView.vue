<template>
	<LoadingView v-if="isLoading" />
	<div v-else-if="error" class="errorScreen">
		<h2>Analysis failed</h2>
		<p>{{ error }}</p>
		<button @click="router.push({ name: 'select' })">Back to map</button>
	</div>
	<DisplayView
		v-else-if="image && analysis"
		:image="image"
		:analysis="analysis"
	/>
</template>

<script setup lang="ts">
import type { AnalysisOutput } from '@stratvis/contracts';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import DisplayView from './DisplayView.vue';
import LoadingView from './LoadingView.vue';
import type Session from '@/types/Session';

const route = useRoute();
const router = useRouter();

const apiBase = (import.meta.env.VITE_API_ROOT as string | undefined)?.replace(/\/$/, '') ?? 'http://localhost:8080';
const ANALYZE_SESSION_URL = `${apiBase}/api/v1/analyze-session`;

const isLoading = ref(true);
const error = ref('');
const image = ref<ImageBitmap | null>(null);
const analysis = ref<AnalysisOutput | null>(null);

onMounted(async () => {
	const sessionId = String(route.query.sessionId ?? '');
	if (!sessionId) {
		error.value = 'Missing sessionId in URL.';
		isLoading.value = false;
		return;
	}

	try {
		const response = await fetch(`${ANALYZE_SESSION_URL}?sessionId=${encodeURIComponent(sessionId)}`, {
			method: 'POST',
		});

		if (!response.ok) {
			throw new Error(`Analyze session failed with status ${response.status}`);
		}

		const payload = (await response.json()) as Session;
		const latestSnapshot = payload.snapshots.at(-1);
		if (!latestSnapshot) {
			throw new Error('Session has no snapshots to display.');
		}
		if (!latestSnapshot.analysis) {
			throw new Error('Analysis is not available for the latest snapshot.');
		}

		analysis.value = latestSnapshot.analysis as AnalysisOutput;
		const imageBlob = base64ToBlob(latestSnapshot.base64Image, 'image/png');
		image.value = await createImageBitmap(imageBlob);
	} catch (err) {
		error.value = err instanceof Error ? err.message : 'Unknown error occurred during analysis.';
	} finally {
		isLoading.value = false;
	}
});

onBeforeUnmount(() => {
	if (image.value) {
		image.value.close();
	}
});

function base64ToBlob(base64: string, contentType: string): Blob {
	const byteString = atob(base64);
	const arrayBuffer = new ArrayBuffer(byteString.length);
	const uint8Array = new Uint8Array(arrayBuffer);
	for (let i = 0; i < byteString.length; i++) {
		uint8Array[i] = byteString.charCodeAt(i);
	}
	return new Blob([uint8Array], { type: contentType });
}
</script>

<style scoped>
.errorScreen {
	position: fixed;
	inset: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12px;
	background: var(--prim);
	padding: 24px;
	text-align: center;
}

.errorScreen button {
	padding: 10px 16px;
	border-radius: 8px;
	border: 1px solid var(--btnBorder);
	background: var(--btnBG);
	cursor: pointer;
}
</style>
