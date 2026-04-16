<template>
	<div class="open-button">
		<BubbleButton @click="isOpen = true">
			<i class="fa-solid fa-comment-dots"></i> Feedback
		</BubbleButton>
	</div>
	<BubblePopup
		v-if="isOpen"
		@close="isOpen = false"
	>
		<h3>We value your feedback!</h3>
		<p>Please let us know your thoughts, suggestions, or any issues you encountered while using the application. Your feedback helps us improve and provide a better experience for everyone.</p>
		<textarea
			placeholder="Enter your feedback here..."
			rows="4"
			style="width: 100%; margin-bottom: 10px;"
			ref="feedbackText"
		></textarea>
		<BubbleButton @click="submit" fullWidth>
			Submit Feedback
		</BubbleButton>
	</BubblePopup>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import BubblePopup from './BubblePopup.vue';
import BubbleButton from './BubbleButton.vue';
import { authFetch, clearAuthToken, getApiBase } from '@/lib/auth';
import { useRouter } from 'vue-router';
const isOpen = ref(false);
const router = useRouter();

const feedbackText = ref<HTMLTextAreaElement | null>(null);

const apiBase = getApiBase();
async function submit() {
	const message = feedbackText.value?.value.trim();
	if (!message) {
		alert('Please enter your feedback before submitting.');
		return;
	}

	try {
		const res = await authFetch(apiBase + '/api/v1/feedback', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ message }),
		});

		if (res.status === 401) {
			clearAuthToken();
			isOpen.value = false;
			router.push({ name: 'login' });
			return;
		}

		if (!res.ok) {
			throw new Error(`Failed to submit feedback: ${res.statusText}`);
		}

		if (feedbackText.value) feedbackText.value.value = '';
		isOpen.value = false;
		alert('Thank you for your feedback!');
	} catch (error) {
		console.error('Error submitting feedback:', error);
		alert('An error occurred while submitting your feedback. Please try again later.');
	}
}
</script>
<style scoped>
.open-button {
	position: fixed;
	bottom: 20px;
	left: 20px;
	z-index: 50;
}

input {
	padding: 8px;
	border: 1px solid var(--sec);
	border-radius: 4px;
	color: black;
}

textarea {
	padding: 8px;
	border: 1px solid var(--sec);
	border-radius: 4px;
	resize: vertical;
	color: black;
	width: 100%;
	box-sizing: border-box;
}
</style>
