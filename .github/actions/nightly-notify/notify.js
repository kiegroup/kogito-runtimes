module.exports = async ({ github, context, core }) => {
  const status = process.env.NIGHTLY_STATUS; // 'success' | 'failure'
  const label = process.env.NIGHTLY_LABEL;
  const version = process.env.NIGHTLY_VERSION;
  const projectName = process.env.NIGHTLY_PROJECT_NAME;
  const projectUrl = process.env.NIGHTLY_PROJECT_URL;
  const jobs = JSON.parse(process.env.NIGHTLY_JOBS_JSON || '[]');

  const owner = context.repo.owner;
  const repo = context.repo.repo;
  const runUrl = `${context.serverUrl}/${owner}/${repo}/actions/runs/${context.runId}`;
  const runLink = `[#${context.runNumber}](${runUrl})`;

  const failedJobsSection = () => {
    const failed = jobs.filter((j) => j.result === 'failure').map((j) => `- \`${j.id}\``);
    const skipped = jobs.filter((j) => j.result === 'skipped').map((j) => `- \`${j.id}\` (skipped)`);
    const lines = [...failed, ...skipped];
    return lines.length ? `\n\n### Failed jobs\n${lines.join('\n')}` : '';
  };

  // Reuse a single tracking issue per label across its whole lifecycle,
  // rather than creating a new one every time the nightly breaks again.
  const { data: issues } = await github.rest.issues.listForRepo({
    owner,
    repo,
    labels: label,
    state: 'all',
    per_page: 1,
    sort: 'created',
    direction: 'desc',
  });
  const issue = issues[0];

  if (status === 'success') {
    if (!issue || issue.state !== 'open') {
      core.info(`No open "${label}" tracking issue; nothing to close.`);
      return;
    }
    await github.rest.issues.createComment({
      owner,
      repo,
      issue_number: issue.number,
      body: `Nightly run ${runLink} passed against \`${version}\`. Closing.`,
    });
    await github.rest.issues.update({ owner, repo, issue_number: issue.number, state: 'closed' });
    core.info(`Closed #${issue.number}.`);
    return;
  }

  // status === 'failure'
  if (!issue) {
    const title = `[Nightly] Integration test failure – ${projectName} ${version}`;
    const body = [
      `## Nightly ${projectName} SNAPSHOT integration test failure`,
      '',
      `**${projectName} version:** \`${version}\``,
      `**Workflow run:** ${runUrl}`,
      '',
      'One or more jobs in the nightly integration test pipeline failed.',
      'Please investigate whether a breaking change was introduced in',
      `[${projectName}](${projectUrl}).${failedJobsSection()}`,
    ].join('\n');

    const { data: created } = await github.rest.issues.create({ owner, repo, title, body, labels: [label] });
    core.info(`Created #${created.number}.`);
    return;
  }

  if (issue.state === 'closed') {
    await github.rest.issues.update({ owner, repo, issue_number: issue.number, state: 'open' });
    await github.rest.issues.createComment({
      owner,
      repo,
      issue_number: issue.number,
      body: `Regression: nightly run ${runLink} failed against \`${version}\` after this had gone green. Reopening.${failedJobsSection()}`,
    });
    core.info(`Reopened #${issue.number}.`);
    return;
  }

  await github.rest.issues.createComment({
    owner,
    repo,
    issue_number: issue.number,
    body: `Nightly run ${runLink} also failed against \`${version}\`.${failedJobsSection()}`,
  });
  core.info(`Commented on #${issue.number}.`);
};
