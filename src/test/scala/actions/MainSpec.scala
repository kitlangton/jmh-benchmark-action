package actions

import zio.test.*

object MainSpec extends ZIOSpecDefault:
  def spec = suite("MainSpec")(
    test("Actions test") {
      for
        message <- Actions.getInput("message").someOrElse("missing")
        cool     = println("h\nello the\nre".replaceAll("\n", "x").take(5))
        _       <- Actions.setOutput("upper", message.toUpperCase)
        outputs <- TestActions.getOutputs
      yield assertTrue(
        outputs("upper") == message.toUpperCase,
        outputs.size == 1
      )
    }.provide(
      TestActions.layer(Map("message" -> "hello"))
    )
  )
