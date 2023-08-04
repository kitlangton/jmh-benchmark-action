import actions.*
import actions.wrappers.{CreateCommentParams, GitHub, PullRequest}
import fs.Files
import git.Git
import jmh.BenchmarkRun
import zio.*
import zio.json.{DecoderOps, EncoderOps}

object Main extends ZIOAppDefault:

  private val benchmarkDataBranch: String    = "internal/benchmark-data"
  private val internalBenchmarksPath: String = ".internal-benchmarks.json"

  /** This action reads JMH benchmark output and compare the results with
    * previous data.
    *
    * It checks if any benchmarks have regressed beyond a configurable
    * threshold. On observing a regression, it either fails with an error or
    * gives a warning based on the `failOnRegression` configuration.
    *
    * If the event is a Pull Request, it adds a comment to the PR showing the
    * benchmark comparison.
    *
    * If the event is a Push (indicating a PR merge), the saved benchmark data
    * is updated with the new data.
    */
  private val program =
    for
      config           <- AppConfig.parse
      _                <- ZIO.debug(s"Parsed Config: $config")
      currentBenchmark <- readJmhBenchmark(config)
      _                <- ZIO.debug("READ BENCHMARK")
      _                <- ZIO.attempt(Git.addAll())
      _                <- ZIO.attempt(Git.resetHard())
      _                <- ZIO.debug("ABOUT TO RESET")
      _                <- checkoutBenchmarkDataBranch
      savedBenchmarks  <- readSavedBenchmarks
      comparison        = currentBenchmark.compare(savedBenchmarks.mostRecent)

      // When the event is a pull request, we want to comment on the PR with the benchmark comparison.
      _ <- ZIO.foreach(GitHub.context.payload.pull_request.toOption) { pullRequest =>
             val (message, link) = getPRCommitMessageAndLink(pullRequest)
             commentOnPullRequest(comparison, config, pullRequest.number, message, link)
           }

      // When the event is a push, indicating a PR has been merged, we want to update the saved benchmark data
      // to include the new benchmark data.
      _ <- updateBenchmarks(savedBenchmarks, currentBenchmark, config)
             .when(GitHub.context.eventName == "push")

      _ <- ZIO.when(comparison.hasRegressed(config.failureThreshold)) {
             if config.failOnRegression then
               Actions.error("Benchmarks have regressed! 😭") *>
                 ZIO.fail(new Error("Benchmarks have regressed! 😭"))
             else Actions.warning("Benchmarks have regressed! 😭")
           }
    yield ()

  private def checkoutBenchmarkDataBranch =
    for
      _ <- ZIO.attempt(Git.fetch(benchmarkDataBranch)).ignore
      _ <- ZIO.attempt(Git.checkout(benchmarkDataBranch))
      _ <- ZIO.attempt(Git.pull("origin", benchmarkDataBranch)).ignore
    yield ()

  private def readJmhBenchmark(config: AppConfig) =
    ZIO.fromEither {
      val json = Files.readFileSync(config.jmhOutputPath).toString
      json.fromJson[List[BenchmarkRun]].map(Benchmark.fromBenchmarkRuns)
    }

  private def readSavedBenchmarks: UIO[SavedBenchmarks] =
    ZIO
      .attempt(Files.readFileSync(internalBenchmarksPath).toString)
      .flatMap { json =>
        ZIO
          .fromEither(json.fromJson[SavedBenchmarks])
      }
      .orElseSucceed(SavedBenchmarks(List.empty))

  private def commentOnPullRequest(
      comparison: BenchmarkComparison,
      config: AppConfig,
      pullRequestNumber: Int,
      commitMessage: String,
      commitLink: String
  ) =
    for
      repository <-
        ZIO.fromOption(GitHub.context.payload.repository.toOption).orElseFail(new Exception("missing repository"))
      octokit = GitHub.getOctokit(config.githubToken)
      _ <- ZIO.fromFuture { _ =>
             val commitMarkdownLink = s"[`$commitMessage`]($commitLink)"
             octokit.rest.issues
               .createComment(
                 CreateCommentParams(
                   owner = repository.owner.login,
                   repo = repository.name,
                   issue_number = pullRequestNumber,
                   body = s"🤖 **Benchmark Comparison** for $commitMarkdownLink\n\n" + comparison.toMarkdownTable
                 )
               )
               .toFuture
           }
    yield ()

  private def getPRCommitMessageAndLink(pullRequest: PullRequest): (String, String) =
    val prSha     = pullRequest.head.sha
    val prRepoUrl = GitHub.context.payload.repository.get.html_url.get
    val prBranch  = pullRequest.head.ref
    Git.remoteAdd("pr_repo", prRepoUrl)
    Git.fetch("pr_repo", prBranch)
    Git.checkout(prBranch, s"pr_repo/$prBranch")
    val message = Git.getCommitMessage(prSha)
    val link    = s"$prRepoUrl/pull/${pullRequest.number}/commits/$prSha"
    (message, link)

  private def updateBenchmarks(savedBenchmarks: SavedBenchmarks, benchmark: Benchmark, config: AppConfig) =
    for
      updatedSavedBenchmarks   <- ZIO.succeed(savedBenchmarks.prepended(benchmark))
      updatedSavedBenchmarkJson = updatedSavedBenchmarks.toJson
      _ <- ZIO.attempt {
             Files.writeFileSync(internalBenchmarksPath, updatedSavedBenchmarkJson)
             Git.addAll()
             Git.setUser("zio-benchmarks-bot", "zio-benchmarks-bot@zio.dev")
             Git.commit("update benchmarks")
           }
      repository <-
        ZIO.fromOption(GitHub.context.payload.repository.toOption).orElseFail(new Exception("missing repository"))
      githubUrl <- ZIO.fromOption(repository.html_url.toOption).orElseFail(new Exception("missing html_url"))
      _         <- ZIO.attempt(Git.push(token = config.githubToken, repoUrl = githubUrl, branch = benchmarkDataBranch))
    yield ()

  val run =
    program.provide(Actions.live, Runtime.removeDefaultLoggers)
