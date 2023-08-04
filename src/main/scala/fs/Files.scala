package fs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("fs", JSImport.Namespace)
object Files extends js.Object:
  def readFileSync(path: String): js.Object = js.native

  def writeFileSync(path: String, data: String, options: js.Object): Unit = js.native
  def writeFileSync(path: String, data: String): Unit                     = js.native
