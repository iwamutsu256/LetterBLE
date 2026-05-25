const fs = require("node:fs");

const filePath = "MEMO/割り振り.md";
const repo = process.env.GITHUB_REPOSITORY;
const token = process.env.GITHUB_TOKEN;

if (!repo) {
  throw new Error("GITHUB_REPOSITORY is not set.");
}

if (!token) {
  throw new Error("GITHUB_TOKEN is not set.");
}

const source = fs.readFileSync(filePath, "utf8");
const issueNumbers = [...new Set([...source.matchAll(/`#(\d+)`/g)].map((match) => match[1]))];
const issueStates = new Map();

async function fetchIssueState(issueNumber) {
  const response = await fetch(`https://api.github.com/repos/${repo}/issues/${issueNumber}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28",
      "User-Agent": "letterble-assignment-sync",
    },
  });

  if (response.status === 404) {
    console.warn(`Issue #${issueNumber} was not found. Skipping.`);
    return null;
  }

  if (!response.ok) {
    throw new Error(`Failed to fetch issue #${issueNumber}: ${response.status} ${response.statusText}`);
  }

  const issue = await response.json();
  return issue.state;
}

async function main() {
  for (const issueNumber of issueNumbers) {
    issueStates.set(issueNumber, await fetchIssueState(issueNumber));
  }

  const updated = source
    .split(/\r?\n/)
    .map((line) => {
      const checklistMatch = line.match(/^(\s*-\s+\[)( |x|X)(\]\s+.*`#(\d+)`.*)$/);
      if (checklistMatch) {
        const state = issueStates.get(checklistMatch[4]);
        if (state === "closed") {
          return `${checklistMatch[1]}x${checklistMatch[3]}`;
        }
        if (state === "open") {
          return `${checklistMatch[1]} ${checklistMatch[3]}`;
        }
        return line;
      }

      const headingMatch = line.match(/^(##\s+.*`#(\d+)`.*?)(\s+DONE)?$/);
      if (headingMatch) {
        const state = issueStates.get(headingMatch[2]);
        const heading = headingMatch[1].trimEnd();
        if (state === "closed") {
          return `${heading} DONE`;
        }
        if (state === "open") {
          return heading;
        }
      }

      return line;
    })
    .join("\n");

  if (updated !== source) {
    fs.writeFileSync(filePath, updated, "utf8");
    console.log(`${filePath} was updated.`);
  } else {
    console.log(`${filePath} is already up to date.`);
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
