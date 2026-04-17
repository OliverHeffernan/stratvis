import { ref, computed } from 'vue';
const TOKEN_STORAGE_KEY = 'stratvis.auth.token';
const authToken = ref<string | null>(window.localStorage.getItem(TOKEN_STORAGE_KEY));
export const isAuthenticatedRef = computed(() => !!authToken.value?.trim());

const apiBase = (
	(import.meta.env.VITE_API_URL as string | undefined) ??
	(import.meta.env.VITE_API_ROOT as string | undefined) ??
	'http://localhost:8080'
).replace(/\/$/, '');

export function getApiBase(): string {
	return apiBase;
}

export function getAuthToken(): string | null {
	return window.localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setAuthToken(token: string): void {
	window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
	authToken.value = token;
}

export function clearAuthToken(): void {
	window.localStorage.removeItem(TOKEN_STORAGE_KEY);
	authToken.value = null;
}

export function isAuthenticated(): boolean {
	const token = getAuthToken();
	return token != null && token.trim().length > 0;
}

export async function authFetch(input: RequestInfo | URL, init: RequestInit = {}): Promise<Response> {
	const token = getAuthToken();
	const headers = new Headers(init.headers ?? {});
	if (token) {
		headers.set('Authorization', `Bearer ${token}`);
	}
	return fetch(input, {
		...init,
		headers,
	});
}

export async function login(email: string, password: string): Promise<void> {
	const response = await fetch(`${apiBase}/api/v1/auth/login`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({ email, password }),
	});

	if (!response.ok) {
		const body = await response.text();
		throw new Error(body || `Login failed with status ${response.status}`);
	}

	const payload = (await response.json()) as { token?: string };
	if (!payload.token) {
		throw new Error('Login response missing token.');
	}
	setAuthToken(payload.token);
}

export async function register(email: string, displayName: string, password: string): Promise<void> {
	const response = await fetch(`${apiBase}/api/v1/auth/register`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({ email, displayName, password }),
	});

	if (!response.ok) {
		const body = await response.text();
		throw new Error(body || `Registration failed with status ${response.status}`);
	}

	const payload = (await response.json()) as { token?: string };
	if (!payload.token) {
		throw new Error('Register response missing token.');
	}
	setAuthToken(payload.token);
}

export async function logout(): Promise<void> {
	const token = getAuthToken();
	if (token) {
		await fetch(`${apiBase}/api/v1/auth/logout`, {
			method: 'POST',
			headers: {
				Authorization: `Bearer ${token}`,
			},
		});
	}
	clearAuthToken();
}
