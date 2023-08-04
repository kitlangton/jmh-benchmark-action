enablePlugins(ScalaJSPlugin)

val zioVersion           = "2.0.15"
val zioJsonVersion       = "0.5.0"
val scalaJavaTimeVersion = "2.5.0"

lazy val root = project
  .in(file("."))
  .settings(
    name                            := "scala-action",
    version                         := "0.1.0",
    scalaVersion                    := "3.3.0",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= Seq(
      "dev.zio"           %%% "zio"             % zioVersion,
      "dev.zio"           %%% "zio-test"        % zioVersion % Test,
      "dev.zio"           %%% "zio-test-sbt"    % zioVersion % Test,
      "dev.zio"           %%% "zio-json"        % zioJsonVersion,
      "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
