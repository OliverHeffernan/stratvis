<template>
	<div v-if="!deleted" class="container">
		<RouterLink :to="{ name: 'analysis', params: { sessionId: session.sessionId } }" ><h3>{{ session.label }}</h3></RouterLink>
		<p>{{ session.dateObject ? formatDate(session.dateObject) : session.creationTime}}</p>
		<button @click="showDeleteModal = true" class="deleteButton"><i class="fa-solid fa-trash"></i></button>
	</div>
	<OptionModal @confirm="deleteThis" @cancel="showDeleteModal = false" v-if="showDeleteModal && !deleted">
		<p>Are you sure you want to delete this snapshot?</p>
		<p><b>{{ session.label }}</b></p>
	</OptionModal>
	<ConfirmBubble v-if="deleted">Snapshot deleted</ConfirmBubble>
</template>
<script setup lang="ts">
import OptionModal from './OptionModal.vue';
import ConfirmBubble from './ConfirmBubble.vue';

import type SessionInfo from '@/types/SessionInfo';
import { RouterLink } from 'vue-router';
import { ref } from 'vue';
import { deleteSession } from '@/lib/database';

const showDeleteModal = ref(false);
const deleted = ref(false);
const props = defineProps<{
	session: SessionInfo;
}>();

function deleteThis(): void {
	deleteSession(props.session.sessionId)
		.then(() => {
			deleted.value = true;
			showDeleteModal.value = false;
		})
		.catch((err) => {
			console.error('Failed to delete session:', err);
			alert('Failed to delete session. Please try again.');
		});
}

function formatDate(date: Date): string {
	const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'long', day: 'numeric' };
	return date.toLocaleDateString(undefined, options);
}
</script>
<style scoped>
.container {
	padding: 20px;
	border: 1px solid var(--btnBorder);
	border-radius: 10px;
	background: color-mix(in srgb, var(--sec) 92%, transparent);
	display: flex;
	flex-direction: column;
	gap: 5px;
	position: relative;
	text-decoration: none;
}

.container > * {
	margin: 0;
	padding: 0;
}

.deleteButton {
	background: none;
	border: none;
	position: absolute;
	right: 30px;
	bottom: 30px;
	font-size: 18px;
}
</style>
