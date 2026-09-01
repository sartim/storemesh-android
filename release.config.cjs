module.exports = {
  branches: ["main"],
  plugins: [
    "@semantic-release/commit-analyzer",
    "@semantic-release/release-notes-generator",
    ["@semantic-release/exec", { prepareCmd: "node scripts/prepare-release.cjs ${nextRelease.version}" }],
    ["@semantic-release/git", { assets: ["app/build.gradle.kts"], message: "chore(release): ${nextRelease.version} [skip ci]" }],
    "@semantic-release/github"
  ]
};
