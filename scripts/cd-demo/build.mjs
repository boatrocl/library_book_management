import { execFileSync } from "node:child_process";
import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const templatePath = resolve(repoRoot, "doc/cd-demo/index.html");
const outputDir = resolve(repoRoot, process.env.CD_DEMO_OUT_DIR ?? "build/cd-demo/site");

const getCommit = () => {
  if (process.env.GITHUB_SHA) return process.env.GITHUB_SHA;
  try {
    return execFileSync("git", ["rev-parse", "HEAD"], { cwd: repoRoot, encoding: "utf8" }).trim();
  } catch {
    return "local";
  }
};

const getSourceState = () => {
  if (process.env.GITHUB_ACTIONS === "true" || process.env.GITHUB_SHA) return "GitHub Actions commit";
  try {
    const hasLocalChanges = execFileSync("git", ["status", "--porcelain"], {
      cwd: repoRoot,
      encoding: "utf8",
    }).trim().length > 0;
    return hasLocalChanges ? "Local working tree (uncommitted)" : "Local clean commit";
  } catch {
    return "Local source";
  }
};

const metadata = {
  commit: getCommit(),
  runNumber: process.env.GITHUB_RUN_NUMBER ?? "local",
  builtAt: new Date().toISOString(),
  sourceState: getSourceState(),
};

let html = readFileSync(templatePath, "utf8");
const replacements = {
  "{{BUILD_SHA}}": metadata.commit,
  "{{RUN_NUMBER}}": metadata.runNumber,
  "{{BUILT_AT}}": metadata.builtAt,
  "{{SOURCE_STATE}}": metadata.sourceState,
};

for (const [token, value] of Object.entries(replacements)) {
  if (!html.includes(token)) throw new Error(`Missing template token: ${token}`);
  html = html.replaceAll(token, value);
}

mkdirSync(outputDir, { recursive: true });
writeFileSync(resolve(outputDir, "index.html"), html);
writeFileSync(resolve(outputDir, "deployment.json"), `${JSON.stringify(metadata, null, 2)}\n`);
console.log(`Built CD demo for ${metadata.commit} into ${outputDir}`);
