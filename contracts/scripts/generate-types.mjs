import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { compile } from 'json-schema-to-typescript'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const contractsRoot = path.resolve(__dirname, '..')

const schemaPath = path.join(contractsRoot, 'analysis-output-schema.json')
const outputPath = path.join(contractsRoot, 'src/generated/analysis-output-types.d.ts')

const schemaRaw = await fs.readFile(schemaPath, 'utf8')
const schema = JSON.parse(schemaRaw)

const output = await compile(schema, 'AnalysisOutput', {
  bannerComment: '// Auto-generated from analysis-output-schema.json. Do not edit manually.\n'
})

await fs.mkdir(path.dirname(outputPath), { recursive: true })
await fs.writeFile(outputPath, output, 'utf8')
