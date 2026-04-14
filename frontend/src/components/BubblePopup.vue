<template>
	<div class="bubble-popupY">
		<slot />
	</div>
</template>
<style scoped>
.bubble-popupY {
	position: absolute;
	top: 0;
	left: 50%;
	transform: translateX(-50%);
	background: var(--sec);
	border: 1px solid var(--border);
	border-radius: 12px;
	padding: 12px;
}
</style>
