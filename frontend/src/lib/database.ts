import { authFetch } from "./auth";
const apiBase = import.meta.env.VITE_API_ROOT;
export async function deleteSession(sessionId: string): Promise<boolean> {
	const response = await authFetch(`${apiBase}/api/v1/session?sessionId=${sessionId}`, {
		method: 'DELETE',
		headers: {
			'Content-Type': 'application/json',
		},
	});
	return response.ok;
}
