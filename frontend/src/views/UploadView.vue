<script setup lang="ts">
import type { AnalysisOutput } from '@stratvis/contracts';
import { ref } from 'vue';
import DisplayView from './DisplayView.vue';

const API_URL = import.meta.env.VITE_API_ROOT + '/api/v1/analyze';
//const API_URL = 'http://localhost:8080/api/v1/analyze';

const isModalOpen = ref(false);
const isSubmitting = ref(false);
const selectedFile = ref<File | null>(null);
const uploadError = ref('');

const image = ref<ImageBitmap | null>(null);
const analysis = ref<AnalysisOutput | null>(null);

const openModal = (): void => {
	uploadError.value = '';
	selectedFile.value = null;
	isModalOpen.value = true;
};

const closeModal = (): void => {
	if (isSubmitting.value) return;
	isModalOpen.value = false;
};

const onFileSelected = (event: Event): void => {
	const target = event.target as HTMLInputElement;
	selectedFile.value = target.files?.[0] ?? null;
};

const submitForAnalysis = async (): Promise<void> => {
	if (!selectedFile.value || isSubmitting.value) return;

	isSubmitting.value = true;
	uploadError.value = '';

	try {
		const formData = new FormData();
		formData.append('image', selectedFile.value);
		image.value = await createImageBitmap(selectedFile.value);

		const response = await fetch(API_URL, {
			method: 'POST',
			body: formData,
		});

		if (!response.ok) {
			throw new Error(`Request failed with status ${response.status}`);
		}

		const analysisResult = (await response.json()) as AnalysisOutput;
		console.info('Analysis response received:', analysisResult);
		analysis.value = analysisResult;
		isModalOpen.value = false;
	} catch (error) {
		uploadError.value = error instanceof Error ? error.message : 'Failed to upload image.';
	} finally {
		isSubmitting.value = false;
	}
};
</script>

<template>
	<DisplayView v-if="analysis != null" :image="image" :analysis="analysis" />
	<div v-else class="container">
		<button class="upload-button" type="button" @click="openModal">
			<i class="fa-solid fa-upload" aria-hidden="true"></i>
		</button>
		<p>Upload an image to analyse</p>
	</div>

	<div v-if="isModalOpen" class="modal-backdrop" @click.self="closeModal">
		<div class="modal">
			<h2>Select an image</h2>
			<input type="file" accept="image/*" @change="onFileSelected" />

			<p v-if="selectedFile">{{ selectedFile.name }}</p>
			<p v-if="uploadError" class="error">{{ uploadError }}</p>

			<div class="actions">
				<button type="button" @click="closeModal" :disabled="isSubmitting">Cancel</button>
				<button
					type="button"
					@click="submitForAnalysis"
					:disabled="!selectedFile || isSubmitting"
				>
					{{ isSubmitting ? 'Sending...' : 'Analyze image' }}
				</button>
			</div>
		</div>
	</div>
</template>
<style scoped>
.container {
	position: fixed;
	top: 0;
	left: 0;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;

	font-size: 100px;
	width: 100vw;
	height: 100vh;
}
.upload-button {
	background: transparent;
	border: 0;
	padding: 0;
	font-size: 120px;
	cursor: pointer;
}

.upload-button i {
	color: var(--sec);
	transition:
		color 0.2s ease,
		opacity 0.2s ease,
		transform 0.2s ease;
	text-shadow: 0 0 3px rgba(255, 255, 255, 0.4);
}

.upload-button:hover i {
	color: var(--text);
	opacity: 0.5;
	transform: translateY(-10px);
}

.modal-backdrop {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.45);
	display: flex;
	align-items: center;
	justify-content: center;
}

.modal {
	background: var(--sec);
	border: 1px solid var(--border);
	border-radius: 12px;
	padding: 24px;
	width: min(90vw, 500px);
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.actions {
	display: flex;
	gap: 10px;
	justify-content: flex-end;
}

.actions button {
	padding: 8px 14px;
	border: 1px solid var(--btnBorder);
	background: var(--btnBG);
	border-radius: 8px;
	cursor: pointer;
}

.actions button:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

.error {
	color: var(--errorBorder);
}
</style>
