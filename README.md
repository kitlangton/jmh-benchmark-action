# JMH Benchmarking Action

This GitHub action is designed for running and comparing JMH benchmarks. It runs your benchmarks and checks for any performance regressions beyond a configurable threshold. When it detects a regression, it can either fail or provide a warning based on the `failOnRegression` configuration.

If the GitHub event is a pull request, it adds a comment to the PR showing the benchmark comparison. If it's a push event (indicating a PR merge), it updates the saved benchmark data with the new data.

## Usage

To use the JMH Benchmarking Action, you need to include it in your GitHub workflow. An example workflow configuration can be found below:

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
        uses: actions/cache@v2
        with:
          path: |
            ~/.m2/repository
            ~/.sbt
            ~/.ivy2/cache
          key: ${{ hashFiles('**/build.sbt') }}
      - name: Build and run JMH benchmark
        run: |
          sbt clean compile 
          sbt 'benchmarks/runBenchmarks'
      - name: JMH Benchmark Action
        uses: kitlangton/jmh-benchmark-action@main
        with:
          jmh-output-path: benchmarks/output.json
          github-token: ${{ secrets.GITHUB_TOKEN }}
```

When you set up the action, you will need to provide the path to your benchmark output and your GitHub token.

## Configuration

You can configure the action by providing additional parameters. The available parameters are:

-  `jmh-output-path`: The file path where your JMH benchmark results are written (e.g., `benchmarks/output.json`).
-  `github-token`: Your GitHub token, which you can get from your GitHub settings.
-  `failOnRegression`: (Optional) A boolean that indicates whether the action should fail when it detects regressions beyond a certain threshold (defaults to false).
-  `failureThreshold`: (Optional) The percentage beyond which a performance decrease is considered a regression (defaults to 5%).

## Notes

The action requires Git and Java to be set up on your runner. You should include actions/setup-java and actions/checkout in your workflow before this action.

## Troubleshooting

If you're having trouble with the action, please open an issue in this repository and we'll try to help as much as possible.

## Contributions

Contributions to the action are most welcome. You can open a PR to suggest changes or improvements. All contributions should follow the contributor guidelines, which you can find [here](./CONTRIBUTING.md).