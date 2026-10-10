import { readFileSync, statSync } from "node:fs";
import { resolve } from "node:path";

const outputDir = resolve(process.argv[2] ?? "build/cd-demo/site");
const htmlPath = resolve(outputDir, "index.html");
const metadataPath = resolve(outputDir, "deployment.json");
const html = readFileSync(htmlPath, "utf8");
const metadata = JSON.parse(readFileSync(metadataPath, "utf8"));

if (statSync(htmlPath).size === 0) throw new Error("Generated index.html is empty");
if (/\{\{[A-Z_]+\}\}/.test(html)) throw new Error("A build metadata placeholder remains in index.html");
for (const id of ["deployment-commit", "deployment-run", "deployment-time", "deployment-source"]) {
  if (!html.includes(`id="${id}"`)) throw new Error(`Missing deployment metadata element: ${id}`);
}
if (!metadata.commit || !metadata.runNumber || !metadata.sourceState || !Number.isFinite(Date.parse(metadata.builtAt))) {
  throw new Error("Deployment metadata is incomplete");
}
if (!html.includes(metadata.commit) || !html.includes(metadata.runNumber) || !html.includes(metadata.builtAt) || !html.includes(metadata.sourceState)) {
  throw new Error("Rendered HTML does not show the metadata stored in deployment.json");
}

console.log(`Validated CD demo artifact for ${metadata.commit} (run ${metadata.runNumber})`);
