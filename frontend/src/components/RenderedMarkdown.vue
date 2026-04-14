<template>
	<div v-html="output" />
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

const output = ref<string>('');

onMounted(async () => {
	output.value = DOMPurify.sanitize(await marked.parse(props.markdown));
})

const props = defineProps<{
	markdown: string;
}>();
</script>
