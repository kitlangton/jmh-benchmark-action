# JMH Benchmarking Action

This GitHub action is designed for running and comparing JMH benchmarks. It runs your benchmarks and checks for any
performance regressions beyond a configurable threshold. When it detects a regression, it can either fail or provide a
warning based on the `failOnRegression` configuration.

If the GitHub event is a pull request, it adds a comment to the PR showing the benchmark comparison. If it's a push
event, it updates the saved benchmark data with the new data (stored in a separate `internal/benchmark-data` branch).

### Example Automated Pull Request Comment

![CleanShot 2023-08-04 at 12 08 55@2x](https://github.com/kitlangton/jmh-benchmark-action/assets/7587245/1e06415a-2966-4c06-b4f5-9d40bea0b271)

Taken from this [Example Project](https://github.com/kitlangton/jmh-benchmark-action-example-project).

## Usage

To use the JMH Benchmarking Action, you need to include it in your GitHub workflow. An example workflow configuration
can be found below:

```yaml
name: Benchmark
on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

permissions:
  contents: write
  pull-requests: write

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          distribution: 'adopt'
      - name: Cache SBT dependencies
        uses: coursier/cache-action@v6
      - name: Build and run JMH benchmark
        run: |
          sbt clean compile 
          sbt 'benchmarks/jmh:run -i 3 -wi 3 -f1 -t1 -rf json -rff output.json .*'
      - name: JMH Benchmark Action
        uses: kitlangton/jmh-benchmark-action@main
        with:
          jmh-output-path: benchmarks/output.json
          github-token: ${{ secrets.GITHUB_TOKEN }}
```

When you set up the action, you will need to provide the path to your benchmark output and your GitHub token.

## Configuration

You can configure the action by providing additional parameters. The available parameters are:

- `jmh-output-path`: The file path where your JMH benchmark results are written (e.g., `benchmarks/output.json`).
- `github-token`: Your GitHub token, (e.g., `${{ secrets.GITHUB_TOKEN }}`).
- `fail-on-regression`: (Optional) A boolean that indicates whether the action should fail when it detects regressions
  beyond a certain threshold (defaults to false).
- `failure-threshold`: (Optional) The change beyond which a performance decrease is considered a regression (defaults to
  -0.05).
