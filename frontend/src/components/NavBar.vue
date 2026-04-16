<template>
	<nav class="background">
		<RouterLink to="/snapshots" class="nav-link">Your Snapshots</RouterLink>
		<RouterLink to="/select" class="nav-link">New Snapshot</RouterLink>
		<RouterLink v-if="authed" to="/signout" class="nav-link">Sign Out</RouterLink>
		<template v-else>
			<RouterLink to="/register" class="nav-link">Register</RouterLink>
			<RouterLink to="/login" class="nav-link">Log In</RouterLink>
		</template>
		<div class="rounded"></div>
	</nav>
</template>
<script setup lang="ts">
import { RouterLink } from 'vue-router';
import { isAuthenticated } from '@/lib/auth';
import { computed } from 'vue';
const authed = computed(() => isAuthenticated());
</script>
<style scoped>
nav {
	position: fixed;
	top: 0;
	left: 0;
	display: flex;
	z-index: 10000;
	font-size: 20px;
	padding-right: 75px;
	border-radius: 0 0 75px 0;
}

.nav-link {
	padding: 10px 20px;
	text-decoration: none;
	text-decoration: none;
	background: none;
}

.background {
	background-color: color-mix(in srgb, var(--sec) 80%, transparent);
	backdrop-filter: blur(10px);
	border-bottom: 3px solid rgba(255, 255, 255, 0.1);
	border-right: 3px solid rgba(255, 255, 255, 0.1);
}
</style>
