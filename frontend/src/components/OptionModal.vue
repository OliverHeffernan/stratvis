<template>
	<BubblePopup notFullHeight @close="$emit('cancel')">
		<div class="content">
			<slot />
		</div>
		<div class="options">
			<BubbleButton @click="$emit('confirm')" red>Yes</BubbleButton>
			<BubbleButton @click="$emit('cancel')">No</BubbleButton>
		</div>
	</BubblePopup>
</template>
<script setup lang="ts">
import BubblePopup from './BubblePopup.vue';
import BubbleButton from './BubbleButton.vue';

const emit = defineEmits<{
	(e: 'confirm'): void;
	(e: 'cancel'): void;
}>();

</script>
<style scoped>
.options {
	width: 100%;
	display: flex;
	justify-content: space-around;
	gap: 10px;
}
</style>
