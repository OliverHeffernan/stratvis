# Stratvis Contracts
This repository contains the JSON schema definitions for the OpenAI API requests used by the backend.

It also contains auto-generated TypeScript types that are used in the frontend. The `json-schema-to-types` library is used to convert the JSON schemas into TypeScript types, ensuring that there is a single source of truth for the types shared between the frontend and backend.

## commands
to rebuild the types after making changes to the JSON schemas, run the following command:
```bash
npm run generate:types
```
