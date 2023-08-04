package git
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("child_process", JSImport.Namespace)
object ChildProcess extends js.Object:
  def execSync(cmd: String): String = js.native

object Process:
  def run(command: String): String =
    ChildProcess.execSync(command)

object Git:
  def checkout(branch: String): Unit =
    Process.run(s"git checkout $branch || git checkout -b $branch")

  def checkout(branch: String, repo: String): Unit =
    Process.run(s"git checkout -b $branch $repo")

  def fetch(branch: String): Unit =
    Process.run(s"git fetch origin $branch")

  def fetch(repo: String, branch: String): Unit =
    Process.run(s"git fetch $repo $branch")

  def remote(): String =
    Process.run(s"git remote -v")

  def branches(): String =
    Process.run(s"git branch -a")

  def remoteAdd(name: String, url: String): Unit =
    Process.run(s"git remote add $name $url")

  def fetch(): Unit =
    Process.run(s"git fetch")

  def addAll(): Unit =
    Process.run(s"git add -A")

  def commit(message: String): Unit =
    Process.run(s"git commit -m '$message'")

  def setUser(name: String, email: String): Unit =
    Process.run(s"git config --global user.name '$name' && git config --global user.email '$email'")

  def push(): Unit =
    Process.run(s"git push")

  def push(token: String, repoUrl: String, branch: String): Unit =
    val repoUrlWithoutProtocol = repoUrl.replace("https://", "")
    val url                    = s"https://x-access-token:$token@$repoUrlWithoutProtocol"
    Process.run(s"git push $url $branch")

  def pull(origin: String, branch: String): Unit =
    Process.run(s"git pull $origin $branch")

  def log(n: Int): String =
    Process.run(s"git log -n $n")

  def getCommitMessage(sha: String): String =
    Process.run(s"git log --pretty=format:'%s' -n 1 $sha")
