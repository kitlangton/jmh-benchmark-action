import actions.Actions
import zio.ZIO

final case class AppConfig(
    jmhOutputPath: String,
    githubToken: String,
    failureThreshold: Double,
    failOnRegression: Boolean
)

private object AppConfig:
  def parse: ZIO[Actions, Nothing, AppConfig] =
    for
      jmhOutputPath <-
        Actions
          .getInput("jmh-output-path", required = true)
          .someOrFail(throw new Exception("missing input 'jmh-output-path'"))
      githubToken <-
        Actions
          .getInput("github-token", required = true)
          .someOrFail(throw new Exception("missing input 'github-token'"))
      failureThreshold <- Actions.getInput("failure-threshold", required = false)
      failOnRegression <- Actions.getInput("fail-on-regression", required = false)
    yield AppConfig(
      jmhOutputPath,
      githubToken,
      failureThreshold.flatMap(_.toDoubleOption).getOrElse(-0.05),
      failOnRegression.flatMap(_.toBooleanOption).getOrElse(false)
    )
