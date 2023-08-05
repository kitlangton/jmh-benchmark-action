package actions.wrappers

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@actions/core", JSImport.Namespace)
object Core extends js.Object:
  def getInput(name: String, options: js.UndefOr[InputOptions]): String = js.native

  def setOutput(name: String, value: String): Unit = js.native

  def setFailed(message: String): Unit = js.native

  def setSecret(secret: String): Unit = js.native

  def exportVariable(name: String, value: String): Unit = js.native

  def debug(message: String): Unit = js.native

  def error(message: String): Unit = js.native

  def warning(message: String): Unit = js.native

  def info(message: String): Unit = js.native

  def setCommandEcho(enabled: Boolean): Unit = js.native

  def summary: Summary = js.native

@js.native
trait InputOptions extends js.Object:
  val required: Boolean = js.native

object InputOptions:
  def apply(required: Boolean): InputOptions =
    js.Dynamic.literal(required = required).asInstanceOf[InputOptions]

@js.native
trait Summary extends js.Object:
  def addRaw(text: String, addEOL: Boolean = false): Summary = js.native
  def addHeading(text: String, level: Int = 1): Summary      = js.native
  def write(): js.Promise[Summary]                           = js.native
