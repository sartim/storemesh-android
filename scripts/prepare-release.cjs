const fs = require("node:fs");

const version = process.argv[2];
if (!/^\d+\.\d+\.\d+$/.test(version ?? "")) throw new Error("semantic-release supplied an invalid SemVer");
const [major, minor, patch] = version.split(".").map(Number);
const versionCode = major * 1_000_000 + minor * 1_000 + patch;
const path = "app/build.gradle.kts";
let source = fs.readFileSync(path, "utf8");
source = source.replace(/versionCode = \d+/, `versionCode = ${versionCode}`);
source = source.replace(/versionName = "[^"]*"/, `versionName = "${version}"`);
fs.writeFileSync(path, source);
