import type { AnalysisOutput } from '@stratvis/contracts';

export interface Snapshot {
	label: string;
	base64Image: string;
	analysis: AnalysisOutput | null;
	creationTime: string;
}

export default interface Session {
	sessionId: number;
	snapshots: Snapshot[];
	creationTime: string;
}
