# Stratvis (Stratospheric Visualizer)
Stratvis is a web app that analyses stratospheric/satellite imagery and provides insights into the environmental conditions in the area. It uses OpenAI's GPT-5.4-mini for image analysis. The backend is built with Spring Boot in Java, and the frontend is developed using Vue.js.

## build script
The build script `./scripts/build-all.sh` is used to build the contracts, backend, and frontend in the correct order. It first builds the contracts to ensure that the latest types are available for both the backend and frontend. Then it builds the backend and frontend.

## sub-repositories
The project is organized into three sub-repositories: `backend`, `frontend`, and `contracts`. Each sub-repository has its own README file with more detailed information about the specific implementation and setup instructions.

### backend
The backend of Stratvis is built using Spring Boot for creating API endpoints, and SQLite as a database. The backend is responsible for processing the images, extracting relevant information, providing insights based on the analysis, and handling the database.

The backend is structured in a Docker container, which allows for easy deployment and scalability. It exposes RESTful APIs that the frontend can call to retrieve the analysis results.

#### External APIs:
- OpenAI API: Used for image analysis and generating insights based on the stratospheric/satellite imagery.
- Geoapify API: Used for reverse geocoding to convert coordinates into human-readable locations.

### frontend
The frontend of Stratvis is developed using Vue.js. It provides a user-friendly interface for users to choose areas to analyse from a map.

Maplibre-gl is used to display the map. Then ArcGis is used as a tile provider to display the map tiles.

Once the user selects an area on the map, the frontend sends the selected coordinates to the backend, where a high resolution image is stitched together from the tiles in the area. Then this image is sent through the OpenAI API for analysis. The results from the backend are then displayed to the user in a clear and concise manner.

### contracts
The contracts repository contains the json-schema definitions for the OpenAI API requests. Then the json-schema-to-types library is used to convert the json-schemas into TypeScript types that can be used in the frontend. Having this in a shared repository maintains a single source of truth for types that are shared between the frontend and backend.
