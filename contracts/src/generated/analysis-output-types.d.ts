// Auto-generated from analysis-output-schema.json. Do not edit manually.

export interface AnalysisOutput {
  summary: string;
  vegetation_health: {
    status: "healthy" | "stressed" | "poor" | "unknown";
    canopy_density_percent: number;
    greenness_index: number;
    explanation: string;
  };
  crop_health: {
    status: "excellent" | "good" | "fair" | "poor" | "not_applicable";
    stressed_area_percent: number;
    main_issue: "drought" | "pest" | "nutrient_deficiency" | "disease" | "none" | "unknown";
    explanation: string;
  };
  deforestation: {
    tree_cover_percent: number;
    recent_loss_percent: number;
    explanation: string;
  };
  water_quality: {
    status: "clear" | "turbid" | "polluted" | "algal_bloom" | "unknown";
    visible_pollution: boolean;
    explanation: string;
  };
  coastal_erosion: {
    erosion_detected: boolean;
    severity: "low" | "medium" | "high" | "none";
    explanation: string;
  };
  disaster_indicators: {
    flooding: boolean;
    fire_scar: boolean;
    landslide: boolean;
    other: string | null;
    explanation: string;
  };
  maritime_features: {
    ships_detected: number;
    oil_spill: boolean;
    explanation: string;
  };
  points_of_interest: {
    x: number;
    y: number;
    message: string;
    long_message: string;
    type: 0 | 1 | 2;
  }[];
  land_use: string[];
}
