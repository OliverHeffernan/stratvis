<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '@/lib/auth';

const router = useRouter();
const displayName = ref('');
const email = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);

async function submit(): Promise<void> {
	error.value = '';
	loading.value = true;
	try {
		await register(email.value, displayName.value, password.value);
		router.push({ name: 'upload' });
	} catch (err) {
		error.value = err instanceof Error ? err.message : 'Failed to register.';
	} finally {
		loading.value = false;
	}
}
</script>

<template>
	<div class="authPage">
		<form class="card" @submit.prevent="submit">
			<h1>Create account</h1>
			<input v-model="displayName" type="text" autocomplete="name" placeholder="Display name" required />
			<input v-model="email" type="email" autocomplete="email" placeholder="Email" required />
			<input v-model="password" type="password" autocomplete="new-password" placeholder="Password" required minlength="8" />
			<p v-if="error" class="error">{{ error }}</p>
			<button type="submit" :disabled="loading">{{ loading ? 'Creating...' : 'Create account' }}</button>
			<p class="hint">
				Already have account?
				<RouterLink :to="{ name: 'login' }">Sign in</RouterLink>
			</p>
		</form>
	</div>
</template>

<style scoped>
.authPage {
	position: fixed;
	inset: 0;
	display: grid;
	place-items: center;
	padding: 24px;
}

.card {
	width: min(420px, 100%);
	display: flex;
	flex-direction: column;
	gap: 10px;
	padding: 20px;
	border: 1px solid var(--btnBorder);
	border-radius: 10px;
	background: color-mix(in srgb, var(--sec) 92%, transparent);
}

input,
button {
	padding: 10px;
	border-radius: 8px;
	border: 1px solid var(--btnBorder);
	font: inherit;
}

input {
	color: #111;
}

button {
	background: var(--btnBG);
	cursor: pointer;
}

.error {
	color: var(--errorBorder);
	margin: 0;
	font-size: 14px;
}

.hint {
	margin: 2px 0 0;
	font-size: 14px;
}
</style>
