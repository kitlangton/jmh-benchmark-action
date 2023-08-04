import jmh.{BenchmarkMode, BenchmarkRun, PrimaryMetric, ScoreUnit}
import zio.json.*
import zio.test.*

object BenchmarkParserSpec extends ZIOSpecDefault:
  def spec = suite("BenchmarkParserSPec")(
    test("BenchmarkParser") {
      val json =
        """
[
{
    "jmhVersion" : "1.36",
    "benchmark" : "benchmarks.MyBenchmark.measure",
    "mode" : "thrpt",
    "threads" : 1,
    "forks" : 1,
    "jvm" : "/Library/Java/JavaVirtualMachines/graalvm-ce-java11-22.3.1/Contents/Home/bin/java",
    "jvmArgs" : [
        "-XX:ThreadPriorityPolicy=1",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+EnableJVMCIProduct",
        "-XX:JVMCIThreadsPerNativeLibraryRuntime=1",
        "-XX:-UnlockExperimentalVMOptions"
    ],
    "jdkVersion" : "11.0.18",
    "vmName" : "OpenJDK 64-Bit Server VM",
    "vmVersion" : "11.0.18+10-jvmci-22.3-b13",
    "warmupIterations" : 0,
    "warmupTime" : "10 s",
    "warmupBatchSize" : 1,
    "measurementIterations" : 1,
    "measurementTime" : "10 s",
    "measurementBatchSize" : 1,
    "primaryMetric" : {
        "score" : 17551.660360749385,
        "scoreError" : "NaN",
        "scoreConfidence" : [
            "NaN",
            "NaN"
        ],
        "scorePercentiles" : {
            "0.0" : 17551.660360749385,
            "50.0" : 17551.660360749385,
            "90.0" : 17551.660360749385,
            "95.0" : 17551.660360749385,
            "99.0" : 17551.660360749385,
            "99.9" : 17551.660360749385,
            "99.99" : 17551.660360749385,
            "99.999" : 17551.660360749385,
            "99.9999" : 17551.660360749385,
            "100.0" : 17551.660360749385
        },
        "scoreUnit" : "ops/s",
        "rawData" : [
            [
                17551.660360749385
            ]
        ]
    },
    "secondaryMetrics" : {
    }
},
{
    "jmhVersion" : "1.36",
    "benchmark" : "benchmarks.MyBenchmark.measure2",
    "mode" : "thrpt",
    "threads" : 1,
    "forks" : 1,
    "jvm" : "/Library/Java/JavaVirtualMachines/graalvm-ce-java11-22.3.1/Contents/Home/bin/java",
    "jvmArgs" : [
        "-XX:ThreadPriorityPolicy=1",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+EnableJVMCIProduct",
        "-XX:JVMCIThreadsPerNativeLibraryRuntime=1",
        "-XX:-UnlockExperimentalVMOptions"
    ],
    "jdkVersion" : "11.0.18",
    "vmName" : "OpenJDK 64-Bit Server VM",
    "vmVersion" : "11.0.18+10-jvmci-22.3-b13",
    "warmupIterations" : 0,
    "warmupTime" : "10 s",
    "warmupBatchSize" : 1,
    "measurementIterations" : 1,
    "measurementTime" : "10 s",
    "measurementBatchSize" : 1,
    "primaryMetric" : {
        "score" : 1.6652949420226753E7,
        "scoreError" : "NaN",
        "scoreConfidence" : [
            "NaN",
            "NaN"
        ],
        "scorePercentiles" : {
            "0.0" : 1.6652949420226753E7,
            "50.0" : 1.6652949420226753E7,
            "90.0" : 1.6652949420226753E7,
            "95.0" : 1.6652949420226753E7,
            "99.0" : 1.6652949420226753E7,
            "99.9" : 1.6652949420226753E7,
            "99.99" : 1.6652949420226753E7,
            "99.999" : 1.6652949420226753E7,
            "99.9999" : 1.6652949420226753E7,
            "100.0" : 1.6652949420226753E7
        },
        "scoreUnit" : "ops/s",
        "rawData" : [
            [
                1.6652949420226753E7
            ]
        ]
    },
    "secondaryMetrics" : {
    }
}
]
          """.trim

      val expected =
        List(
          BenchmarkRun(
            "benchmarks.MyBenchmark.measure",
            BenchmarkMode.Throughput,
            PrimaryMetric(17551.660360749385, ScoreUnit.OpsPerSecond)
          ),
          BenchmarkRun(
            "benchmarks.MyBenchmark.measure2",
            BenchmarkMode.Throughput,
            PrimaryMetric(1.6652949420226753e7, ScoreUnit.OpsPerSecond)
          )
        )

      val runs = json.fromJson[List[BenchmarkRun]]
      assertTrue(
        runs.toOption.get == expected
      )
    }
  )
