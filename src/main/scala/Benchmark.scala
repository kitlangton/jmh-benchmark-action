import jmh.{BenchmarkRun, ScoreUnit}
import zio.json.*

import java.time.{LocalDateTime, ZoneId}

case class Benchmark(time: LocalDateTime, benchmarkRuns: List[BenchmarkRun]):
  lazy val runsMap: Map[String, BenchmarkRun] =
    benchmarkRuns.map(benchmarkRun => benchmarkRun.benchmark -> benchmarkRun).toMap

  def compare(previous: Option[Benchmark]): BenchmarkComparison =
    val previousRunMap = previous.map(_.runsMap).getOrElse(Map.empty)
    val comparison = benchmarkRuns.map { benchmarkRun =>
      val previous = previousRunMap.get(benchmarkRun.benchmark)
      BenchmarkRunComparison.fromBenchmarkRun(benchmarkRun.benchmark, previous, benchmarkRun)
    }
    BenchmarkComparison(comparison)

final case class BenchmarkComparison(
    comparisons: List[BenchmarkRunComparison]
):
  def toMarkdownTable: String =
    val header = "| Benchmark | Previous | Current | Change |\n| --------- | ------- | ------- | ----- |\n"
    val rows   = comparisons.map(_.toMarkdownRow).mkString("\n")
    header + rows

  def hasRegressed(failureThreshold: Double): Boolean =
    comparisons.exists(_.isWorseThan(failureThreshold))

object BenchmarkComparison:
  implicit val codec: JsonCodec[BenchmarkComparison] = DeriveJsonCodec.gen[BenchmarkComparison]

final case class BenchmarkRunComparison(
    benchmarkName: String,
    previous: Option[BenchmarkRun],
    current: BenchmarkRun
):

  def toMarkdownRow: String =
    val previousScore = previous.map(_.primaryMetric.render).getOrElse("N/A")
    val changeString  = change.map(change => f"${change * 100}%.2f").getOrElse("N/A")
    s"| `$benchmarkName` | `$previousScore` | `${current.primaryMetric.render}` | `$changeString%` |"

  def ratio: Option[Double] =
    for prev <- previous.map(_.primaryMetric) if prev.scoreUnit == current.primaryMetric.scoreUnit
    yield current.primaryMetric.scoreUnit match
      case ScoreUnit.OpsPerSecond => current.primaryMetric.score / prev.score
      case _                      => prev.score / current.primaryMetric.score

  def change: Option[Double] =
    ratio.map(_ - 1.0)

  def isWorseThan(failureThreshold: Double): Boolean =
    change.getOrElse(0.0) < failureThreshold

object BenchmarkRunComparison:
  def fromBenchmarkRun(
      benchmarkName: String,
      previous: Option[BenchmarkRun],
      current: BenchmarkRun
  ): BenchmarkRunComparison =
    BenchmarkRunComparison(benchmarkName, previous, current)

  implicit val codec: JsonCodec[BenchmarkRunComparison] = DeriveJsonCodec.gen[BenchmarkRunComparison]

object Benchmark:
  implicit val codec: JsonCodec[Benchmark] = DeriveJsonCodec.gen[Benchmark]

  def fromBenchmarkRuns(benchmarkRuns: List[BenchmarkRun]): Benchmark =
    val time = LocalDateTime.now(ZoneId.of("UTC"))
    Benchmark(time, benchmarkRuns)

final case class SavedBenchmarks(benchmarks: List[Benchmark]):
  def prepended(benchmark: Benchmark): SavedBenchmarks =
    SavedBenchmarks(benchmarks = benchmark :: benchmarks)

  def mostRecent: Option[Benchmark] =
    benchmarks.headOption

object SavedBenchmarks:
  implicit val codec: JsonCodec[SavedBenchmarks] = DeriveJsonCodec.gen[SavedBenchmarks]

  val empty: SavedBenchmarks = SavedBenchmarks(List.empty)
