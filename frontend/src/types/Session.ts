import type { AnalysisOutput } from '@stratvis/contracts';

export type AnalysisPoiWithMap = AnalysisOutput['points_of_interest'][number] & {
	lng?: number;
	lat?: number;
	range_m?: number;
};

export type AnalysisWithMap = Omit<AnalysisOutput, 'points_of_interest'> & {
	points_of_interest: AnalysisPoiWithMap[];
};

export interface Snapshot {
	label: string;
	minLng: number;
	minLat: number;
	maxLng: number;
	maxLat: number;
	usedZoom: number;
	analysis: AnalysisWithMap | null;
	creationTime: string;
}

export default interface Session {
	sessionId: number;
	snapshot: Snapshot;
	creationTime: string;
}
