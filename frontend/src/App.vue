<script setup lang="ts">
import { RouterView } from 'vue-router';
import { computed } from 'vue';
import NavBar from '@/components/NavBar.vue';
import Feedback from '@/components/Feedback.vue';
import { isAuthenticated } from '@/lib/auth';

const showFeedback = computed(() => isAuthenticated());
</script>

<template>
	<NavBar />
	<Feedback v-if="showFeedback" />
	<RouterView />
</template>

<style>
:root {
	--prim: #0b1119;
	--sec: #13202e;
	/*--sec: #2c3e50;*/
	--text: #ffffff;
	--border: #656668;
	--btnBG: #13202e;
	--btnBorder: #6180bf;
	--errorBorder: #ff4c71;
	--goodBorder: #00ed9e;
	--errorBG: #3c1c2a;
	--gold: #ffd700;
	--accent: rgb(75, 192, 192);
	--accentTransparent: rgba(75, 192, 192, 0.2);
	--stravaOrange: #fc5200;
}

p {
	font-size: 16px;
}

html {
	font-family: Avenir, Helvetica, Arial, sans-serif;
	-webkit-font-smoothing: antialiased;
	-moz-osx-font-smoothing: grayscale;
	/*text-align: center;*/
	color: var(--text);
	background-color: var(--prim);
	margin-top: 60px;
	margin: 0;
	padding: 0;
	font-size: 16px;
}

body {
	margin: 0;
	padding: 0;
}

* {
	color: var(--text);
}
</style>
