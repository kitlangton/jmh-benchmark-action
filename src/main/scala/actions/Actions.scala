package actions

import actions.wrappers.{InputOptions, Summary}
import zio.*

trait Actions:
  def getInput(name: String, required: Boolean = false): UIO[Option[String]]

  def setOutput(name: String, value: String): UIO[Unit]

  def setFailed(message: String): UIO[Unit]

  def setSecret(secret: String): UIO[Unit]

  def exportVariable(name: String, value: String): UIO[Unit]

  def debug(message: String): UIO[Unit]

  def error(message: String): UIO[Unit]

  def warning(message: String): UIO[Unit]

  def info(message: String): UIO[Unit]

  def setCommandEcho(enabled: Boolean): UIO[Unit]

  def addSummary(f: Summary => Summary): Task[Unit]

object Actions:

  def getInput(name: String, required: Boolean = false): ZIO[Actions, Nothing, Option[String]] =
    ZIO.serviceWithZIO[Actions](_.getInput(name, required))

  def setOutput(name: String, value: String): ZIO[Actions, Nothing, Unit] =
    ZIO.serviceWithZIO[Actions](_.setOutput(name, value))

  def setFailed(message: String): ZIO[Actions, Nothing, Unit] =
    ZIO.serviceWithZIO[Actions](_.setFailed(message))

  def warning(message: String): ZIO[Actions, Nothing, Unit] =
    ZIO.serviceWithZIO[Actions](_.warning(message))

  def error(message: String): ZIO[Actions, Nothing, Unit] =
    ZIO.serviceWithZIO[Actions](_.error(message))

  def debug(message: String): ZIO[Actions, Nothing, Unit] =
    ZIO.serviceWithZIO[Actions](_.debug(message))

  def addSummary(f: Summary => Summary): ZIO[Actions, Throwable, Unit] =
    ZIO.serviceWithZIO[Actions](_.addSummary(f))

  val live: ULayer[ActionsLive] =
    ZLayer.fromFunction(ActionsLive.apply _)

final case class ActionsLive() extends Actions:

  override def getInput(name: String, required: Boolean): UIO[Option[String]] =
    ZIO.attempt(wrappers.Core.getInput(name, InputOptions(required))).option

  override def setOutput(name: String, value: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.setOutput(name, value))

  override def setFailed(message: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.setFailed(message))

  override def setSecret(secret: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.setSecret(secret))

  override def exportVariable(name: String, value: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.exportVariable(name, value))

  override def debug(message: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.debug(message))

  override def error(message: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.error(message))

  override def warning(message: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.warning(message))

  override def info(message: String): UIO[Unit] =
    ZIO.succeed(wrappers.Core.info(message))

  override def setCommandEcho(enabled: Boolean): UIO[Unit] =
    ZIO.succeed(wrappers.Core.setCommandEcho(enabled))

  override def addSummary(f: Summary => Summary): Task[Unit] =
    ZIO.fromFuture(_ => f(wrappers.Core.summary).write().toFuture).unit

final case class TestActions(inputs: Map[String, String], outputs: Ref[Map[String, String]]) extends Actions:
  override def getInput(name: String, required: Boolean): UIO[Option[String]] =
    ZIO.succeed(inputs.get(name))

  override def setOutput(name: String, value: String): UIO[Unit] =
    outputs.update(_ + (name -> value))

  override def setFailed(message: String): UIO[Unit] =
    ZIO.debug(s"setFailed(message = $message)")

  override def setSecret(secret: String): UIO[Unit] =
    ZIO.debug(s"setSecret(secret = $secret)")

  override def exportVariable(name: String, value: String): UIO[Unit] =
    ZIO.debug(s"exportVariable(name = $name, value = $value)")

  override def debug(message: String): UIO[Unit] =
    Console.printLine(s"DEBUG: $message").!

  override def error(message: String): UIO[Unit] =
    Console.printLineError(s"ERROR: $message").!

  override def warning(message: String): UIO[Unit] =
    Console.printLine(s"WARNING: $message").!

  override def info(message: String): UIO[Unit] =
    Console.printLine(s"INFO: $message").!

  override def setCommandEcho(enabled: Boolean): UIO[Unit] =
    ZIO.debug(s"setCommandEcho(enabled = $enabled)")

  override def addSummary(f: Summary => Summary): Task[Unit] =
    ZIO.debug(s"addSummary(f = $f)")

  // # Test Methods

  def getOutputs: UIO[Map[String, String]] = outputs.get

object TestActions:

  val getOutputs: ZIO[TestActions, Nothing, Map[String, String]] =
    ZIO.serviceWithZIO[TestActions](_.getOutputs)

  def layer(inputs: Map[String, String]): ULayer[TestActions] = ZLayer {
    for outputs <- Ref.make(Map.empty[String, String])
    yield TestActions(inputs, outputs)
  }
