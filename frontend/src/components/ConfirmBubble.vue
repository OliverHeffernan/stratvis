<template>
	<div class="bubble">
		<i class="fa-solid fa-check"></i>
		<p><slot /></p>
	</div>
</template>
<style scoped>
.bubble {
	position: fixed;
	right: 20px;
	bottom: 20px;

	animation: slideIn 4s ease forwards;

	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
	gap: 10px;
	padding: 20px;
	border: 2px solid var(--goodBorder);
	border-radius: 10px;
	/*background-color: var(--sec);*/
	background-color: color-mix(in srgb, var(--sec) 80%, transparent);
	backdrop-filter: blur(10px);
	opacity: 1;
	z-index: 10004;
}

.bubble i {
	color: var(--goodBorder);
	font-size: 20px;
}

/* animate slide in from right and fade in, then after 3 seconds, slide out while fading out */
@keyframes slideIn {
	0% {
		transform: translateX(100%);
		opacity: 0;
	}
	10% {
		transform: translateX(0);
		opacity: 1;
	}
	90% {
		transform: translateX(0);
		opacity: 1;
	}
	100% {
		transform: translateX(100%);
		opacity: 0;
	}
}
</style>
