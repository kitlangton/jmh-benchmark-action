package actions.wrappers

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@actions/github", JSImport.Namespace)
object GitHub extends js.Object:
  def context: Context                   = js.native
  def getOctokit(token: String): Octokit = js.native

@js.native
trait Context extends js.Object:
  def eventName: String = js.native
  def sha: String       = js.native
  def ref: String       = js.native
  def workflow: String  = js.native
  def action: String    = js.native
  def actor: String     = js.native
  def payload: Payload  = js.native
  def repo: Repository  = js.native

object Context:
  def commitLink =
    val context = GitHub.context
    val owner   = context.repo.owner
    val repo    = context.repo.repo
    val sha     = context.sha
    s"https://github.com/$owner/$repo/commit/$sha"

@js.native
trait Repository extends js.Object:
  def owner: String
  def repo: String

@js.native
trait Payload extends js.Object:
  def repository: js.UndefOr[PayloadRepository]
  def issue: js.UndefOr[Issue]
  def pull_request: js.UndefOr[PullRequest]
  def push: js.UndefOr[Push]
  def sender: js.UndefOr[Sender]
  def action: js.UndefOr[String]
  def comment: js.UndefOr[Comment]

@js.native
trait PullRequest extends js.Object:
  def number: Int
  def html_url: String
  def body: String
  def head: PullRequestHead
  def _links: Links

@js.native
trait Links extends js.Object:
  def self: Link
  def comments: Link
  def commits: Link

@js.native
trait Link extends js.Object:
  def href: String

@js.native
trait Push extends js.Object:
  def commits: js.Array[Commit]

@js.native
trait Commit extends js.Object:
  def id: String
  def message: String
  def url: String

@js.native
trait PullRequestHead extends js.Object:
  def label: String
  def ref: String
  def sha: String

@js.native
trait Sender extends js.Object:
  def login: String
  def name: js.UndefOr[String]

@js.native
trait Issue extends js.Object:
  def number: Int
  def html_url: js.UndefOr[String]
  def body: js.UndefOr[String]

@js.native
trait Comment extends js.Object:
  def id: Int
  def body: js.UndefOr[String]

@js.native
trait PayloadRepository extends js.Object:
  def name: String
  def full_name: js.UndefOr[String]
  def owner: Owner
  def html_url: js.UndefOr[String]

@js.native
trait Owner extends js.Object:
  def login: String
  def name: js.UndefOr[String]
