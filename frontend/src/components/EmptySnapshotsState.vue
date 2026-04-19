<template>
	<div class="emptyState">
		<i class="fa-solid fa-camera emptyStateIcon" aria-hidden="true"></i>
		<h2>No snapshots yet</h2>
		<p>Create your first one from the map view.</p>
		<RouterLink class="emptyStateLink" :to="{ name: 'select' }">Create snapshot</RouterLink>
	</div>
</template>

<style scoped>
.emptyState {
	min-height: calc(100vh - 160px);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	text-align: center;
	gap: 12px;
}

.emptyStateIcon {
	font-size: clamp(80px, 14vw, 160px);
	color: color-mix(in srgb, var(--prim) 88%, white);
	opacity: 0.9;
	line-height: 1;
}

.emptyState h2 {
	margin: 0;
	font-size: clamp(24px, 4vw, 34px);
	font-weight: 700;
}

.emptyState p {
	margin: 0;
	opacity: 0.9;
}

.emptyStateLink {
	margin-top: 4px;
	padding: 10px 16px;
	border: 1px solid var(--btnBorder);
	border-radius: 8px;
	background: var(--btnBG);
	text-decoration: none;
}
</style>
