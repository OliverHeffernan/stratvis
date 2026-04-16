<script setup lang="ts">
import { useRouter } from 'vue-router';
import { computed } from 'vue';
import { isAuthenticated, logout } from '@/lib/auth';

const router = useRouter();
const authed = computed(() => isAuthenticated());

function startMapAnalysis(): void {
	router.push({ name: 'select' });
}

async function signOut(): Promise<void> {
	await logout();
	router.push({ name: 'login' });
}
</script>

<template>
	<div class="container">
		<h1>Map-based analysis</h1>
		<p>Select an area on the map, run analysis, and view map pinpoints with uncertainty ranges.</p>
		<template v-if="authed">
			<button type="button" @click="startMapAnalysis">Open map</button>
			<button type="button" class="secondary" @click="signOut">Log out</button>
		</template>
		<template v-else>
			<RouterLink class="buttonLink" :to="{ name: 'login' }">Sign in</RouterLink>
			<RouterLink class="buttonLink" :to="{ name: 'register' }">Create account</RouterLink>
		</template>
	</div>
</template>

<style scoped>
.container {
	position: fixed;
	inset: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 14px;
	text-align: center;
	padding: 24px;
}

button {
	padding: 10px 16px;
	border: 1px solid var(--btnBorder);
	border-radius: 8px;
	background: var(--btnBG);
	cursor: pointer;
}

.secondary {
	opacity: 0.9;
}

.buttonLink {
	padding: 10px 16px;
	border: 1px solid var(--btnBorder);
	border-radius: 8px;
	background: var(--btnBG);
	text-decoration: none;
}
</style>
