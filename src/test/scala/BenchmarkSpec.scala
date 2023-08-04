import jmh.{BenchmarkMode, BenchmarkRun, PrimaryMetric, ScoreUnit}
import zio.test.*

import java.time.LocalDateTime

object BenchmarkSpec extends ZIOSpecDefault:
  def spec = suite("BenchmarkSpec")(
    suite("calculates ratio")(
      test("ops/s") {
        val prevBenchmark   = makeBenchmark(100.0, ScoreUnit.OpsPerSecond)
        val newBenchmark    = makeBenchmark(95.0, ScoreUnit.OpsPerSecond)
        val firstComparison = newBenchmark.compare(Some(prevBenchmark)).comparisons.head
        assertTrue(
          firstComparison.ratio == Option(0.95),
          approximatelyEquals(firstComparison.change.get, -0.05, 0.000001)
        )
      },
      test("ms") {
        val prevBenchmark   = makeBenchmark(95.0, ScoreUnit.Milliseconds)
        val newBenchmark    = makeBenchmark(100.0, ScoreUnit.Milliseconds)
        val firstComparison = newBenchmark.compare(Some(prevBenchmark)).comparisons.head
        assertTrue(
          firstComparison.ratio == Option(0.95),
          approximatelyEquals(firstComparison.change.get, -0.05, 0.000001)
        )
      },
      test("mismatched score units return None") {
        val prevBenchmark   = makeBenchmark(95.0, ScoreUnit.Milliseconds)
        val newBenchmark    = makeBenchmark(100.0, ScoreUnit.OpsPerSecond)
        val firstComparison = newBenchmark.compare(Some(prevBenchmark)).comparisons.head
        assertTrue(firstComparison.ratio.isEmpty)
      }
    )
  )

  def approximatelyEquals(lhs: Double, rhs: Double, tolerance: Double): Boolean =
    (lhs - rhs).abs < tolerance

  private def makeBenchmark(score: Double, scoreUnit: ScoreUnit) =
    Benchmark(
      LocalDateTime.now(),
      List(
        BenchmarkRun(
          "Benchmark One",
          BenchmarkMode.Throughput,
          PrimaryMetric(score, scoreUnit)
        )
      )
    )
