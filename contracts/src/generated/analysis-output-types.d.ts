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
    /**
     * Explanation of martime features detected, including the features of the image that led to these conclusions and any potential environmental implications of these findings.
     */
    explanation: string;
  };
  points_of_interest: {
    /**
     * X coordinate as a percentage of the image width (0-100). 0 = left edge, 100 = right edge.
     */
    x: number;
    /**
     * Y coordinate as a percentage of the image height (0-100). 0 = top edge, 100 = bottom edge.
     */
    y: number;
    /**
     * A one sentence heading/label describing the point of interest.
     */
    message: string;
    /**
     * A detailed explanation describing the point of interest, including environmental implications of its presence, and any actions that could be taken in response to this finding. This should expand on the message field and provide more context and information about why this point is important and what it means for the environment in this area.
     */
    long_message: string;
    /**
     * Explanation of why this point of interest was identified and placed at these coordinates. Include details about the features or patterns in the image that led to this conclusion, and the placement of the point.
     */
    placement_rationale?: string;
    /**
     * Should be a one word description of the feature that is being pointed out by this point of interest. This may include the following features, but is not limited to: erosion, sediment, turbidity, algal_bloom, deforestation, flooding, landslide_scar, fire_scar, visible_pollution, vegetation_loss, vegetation_stress, restoration_signals...
     */
    feature?: string;
    /**
     * 0 means environmental danger, that is impacting the environment negatively presently. 1 means environmental threat, which could impact the environment negatively in the future if no action is taken. 2 means environmental opportunity, which is a positive finding that could be built upon to further improve the environment in this area.
     */
    type: 0 | 1 | 2;
  }[];
  land_use: string[];
  /**
   * A comprehensive report summarizing the overall environmental health of the area depicted in the image, integrating all of the above findings into a cohesive narrative. This report should provide an overall assessment of the environmental conditions in this area, highlight key issues and opportunities, and offer recommendations for actions that could be taken to address any identified problems or to build upon any positive findings. The report should be detailed and informative, providing a clear picture of the state of the environment in this location and what it means for the future. Should be written in markdown format, with appropriate headings, bullet points, and sections to organize the information effectively. And should be suitable for sharing with policymakers, stakeholders, or the general public to inform them about the environmental conditions in this area and what can be done about it. Should be 1-2 pages in length when rendered in markdown format.
   */
  report: string;
}
