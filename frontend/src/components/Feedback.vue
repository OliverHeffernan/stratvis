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
		<input
			type="text"
			placeholder="Your name (optional)"
			style="width: 100%; margin-bottom: 10px;"
			ref="nameInput"
		/>
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
const isOpen = ref(false);

const nameInput = ref<HTMLInputElement | null>(null);
const feedbackText = ref<HTMLTextAreaElement | null>(null);

const apiBase = (import.meta.env.VITE_API_ROOT as string | undefined)?.replace(/\/$/, '') ?? 'http://localhost:8080';
function submit() {
	const name = nameInput.value?.value.trim() || 'Anonymous';
	const message = feedbackText.value?.value.trim();
	if (!message) {
		alert('Please enter your feedback before submitting.');
		return;
	}

	const response = fetch(apiBase + '/api/v1/feedback', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({ name, message }),
	})
	.then((res) => {
		if (!res.ok) {
			throw new Error(`Failed to submit feedback: ${res.statusText}`);
		}
		return res.json();
	})
	.then(() => {
		alert('Thank you for your feedback!');
	})
	.catch((error) => {
		console.error('Error submitting feedback:', error);
		alert('An error occurred while submitting your feedback. Please try again later.');
	});
	// Here you would typically send the feedback to your server or an external service
	console.log('Feedback submitted:', { name, message });

	// Clear the form and close the popup
	if (nameInput.value) nameInput.value.value = '';
	if (feedbackText.value) feedbackText.value.value = '';
	isOpen.value = false;

	alert('Thank you for your feedback!');
}
</script>
<style scoped>
.open-button {
	position: fixed;
	bottom: 20px;
	right: 20px;
	z-index: 1000;
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
}
</style>
