import { createRouter, createWebHistory, type Router } from 'vue-router';
import UploadView from '@/views/UploadView.vue';
import SelectView from '@/views/SelectView.vue';
import AnalysisView from '@/views/AnalysisView.vue';
import LoginView from '@/views/LoginView.vue';
import RegisterView from '@/views/RegisterView.vue';
import SignOutView from '@/views/SignOutView.vue';
import SnapshotsView from '@/views/SnapshotsView.vue';
import { isAuthenticated } from '@/lib/auth';

const router: Router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
		routes: [
			{
				path: '/',
				redirect: { name: 'upload' },
			},
			{
				component: UploadView,
				name: 'upload',
				path: '/upload',
			},
			{
				component: LoginView,
				name: 'login',
				path: '/login',
			},
			{
				component: RegisterView,
				name: 'register',
				path: '/register',
			},
			{
				component: SelectView,
				name: 'select',
				path: '/select',
				meta: { requiresAuth: true },
			},
			{
				component: AnalysisView,
				name: 'analysis',
				path: '/analysis/:sessionId',
				props: [ 'sessionId' ],
				meta: { requiresAuth: true },
			},
			{
				component: SignOutView,
				name: 'signout',
				path: '/signout',
			},
			{
				component: SnapshotsView,
				name: 'snapshots',
				meta: { requiresAuth: true },
				path: '/snapshots',
			}
		],
});

router.beforeEach((to) => {
	if (to.meta.requiresAuth && !isAuthenticated()) {
		return { name: 'login' };
	}
	if ((to.name === 'login' || to.name === 'register') && isAuthenticated()) {
		return { name: 'upload' };
	}
	return true;
});

export default router;
