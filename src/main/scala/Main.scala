import actions.*
import actions.wrappers.{CreateCommentParams, GetCommitParams, GitHub, PullRequest}
import fs.Files
import git.Git
import jmh.BenchmarkRun
import zio.*
import zio.json.{DecoderOps, EncoderOps}
import scala.scalajs.js
import scala.scalajs.js.JSON.stringify
import js.Dynamic.global

object Main extends ZIOAppDefault:
  private val environment                    = global.process.env
  private val githubStepSummaryFile          = environment.GITHUB_STEP_SUMMARY.toString
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
      config           <- AppConfig.parse.debug("Parsed AppConfig")
      currentBenchmark <- readJmhBenchmark(config)
      _                <- ZIO.debug("Read current benchmark data")
      _                <- ZIO.attempt { Git.addAll(); Git.resetHard() }
      _                <- ZIO.debug("Reset git repo")
      _                <- checkoutBenchmarkDataBranch
      _                <- ZIO.debug("Checked out benchmark data branch")
      savedBenchmarks  <- readSavedBenchmarks
      _                <- ZIO.debug("Read saved benchmark data")
      comparison        = currentBenchmark.compare(savedBenchmarks.mostRecent)

      // When the event is a pull request, we want to comment on the PR with the benchmark comparison.
//      _ <- ZIO.foreach(GitHub.context.payload.pull_request.toOption) { pullRequest =>
//             ZIO.debug(s"Found pull request ${stringify(pullRequest)}") *>
//               commentOnPullRequest(comparison, config, pullRequest) *>
//               ZIO.debug("Commented on pull request")
//           }
      _ <- ZIO.attempt {
             Files.appendFileSync(githubStepSummaryFile, "## Benchmark Comparison\n\n" + comparison.toMarkdownTable)
           }

      // When the event is a push, indicating a PR has been merged, we want to update the saved benchmark data
      // to include the new benchmark data.
      _ <- updateBenchmarks(savedBenchmarks, currentBenchmark, config)
             .when(GitHub.context.eventName == "push")
      _ <- ZIO.debug(s"Updated saved benchmark data when 'push' == ${GitHub.context.eventName}")

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
      pullRequest: PullRequest
  ) =
    for
      octokit <- ZIO.succeed(GitHub.getOctokit(config.githubToken))
      commitMessage <- ZIO
                         .fromFuture(_ =>
                           octokit.rest.repos
                             .getCommit(
                               GetCommitParams(
                                 owner = pullRequest.head.repo.owner.login,
                                 repo = pullRequest.head.repo.name,
                                 ref = pullRequest.head.sha
                               )
                             )
                             .toFuture
                         )
                         .tap { payload =>
                           ZIO.debug(s"Got commit message ${stringify(payload)}")
                         }
                         .map(_.data.commit.message)
      link = s"${pullRequest.html_url}/pull/${pullRequest.number}/commits/${pullRequest.head.sha}"
      _ <- ZIO.fromFuture { _ =>
             val commitMarkdownLink = s"[`${commitMessage.replace("\n", " ")}`]($link)"
             octokit.rest.issues
               .createComment(
                 CreateCommentParams(
                   owner = GitHub.context.repo.owner,
                   repo = GitHub.context.repo.repo,
                   issue_number = pullRequest.number,
                   body = s"🤖 **Benchmark Comparison** for $commitMarkdownLink\n\n" + comparison.toMarkdownTable
                 )
               )
               .toFuture
           }
    yield ()

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
      _ <-
        ZIO.attempt(Git.push(token = config.githubToken, repoUrl = repository.html_url, branch = benchmarkDataBranch))
    yield ()

  val run =
    program.provide(Actions.live, Runtime.removeDefaultLoggers)
