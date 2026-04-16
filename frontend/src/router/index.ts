import { createRouter, createWebHistory, type Router } from 'vue-router';
import UploadView from '@/views/UploadView.vue';
import SelectView from '@/views/SelectView.vue';
import AnalysisView from '@/views/AnalysisView.vue';

const router: Router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
		routes: [
			// just wanting an example route here, can't remember the format for routes
			{
				component: UploadView,
				name: 'upload',
				path: '/upload',
			},
			{
				component: SelectView,
				name: 'select',
				path: '/select',
			},
			{
				component: AnalysisView,
				name: 'analysis',
				path: '/analysis',
			},
		],
});

export default router;
