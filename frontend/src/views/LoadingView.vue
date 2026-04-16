<!-- A simple loading spinner component -->
<template>
	<div class="loading">
		<i class="fa-solid fa-spinner"></i>
	</div>
</template>
<style scoped>
.loading {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: var(--prim);
	z-index: 20;
}

.loading i {
	position: fixed;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
