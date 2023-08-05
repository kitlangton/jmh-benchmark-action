package actions.wrappers

import scala.scalajs.js

@js.native
trait Octokit extends js.Object:
  def rest: Rest = js.native

@js.native
trait Rest extends js.Object:
  def repos: Repos   = js.native
  def issues: Issues = js.native

@js.native
trait Repos extends js.Object:
  def createCommitComment(params: CreateCommitCommentParams): js.Promise[CreateCommitCommentResponse] = js.native
  def getCommit(params: GetCommitParams): js.Promise[DataWrapper[GetCommitResponse]]                  = js.native

@js.native
trait GetCommitParams extends js.Object:
  def owner: String
  def repo: String
  def ref: String

object GetCommitParams:
  def apply(
      owner: String,
      repo: String,
      ref: String
  ): GetCommitParams =
    js.Dynamic
      .literal(
        owner = owner,
        repo = repo,
        ref = ref
      )
      .asInstanceOf[GetCommitParams]

@js.native
trait DataWrapper[A] extends js.Object:
  def data: A

@js.native
trait GetCommitResponse extends js.Object:
  def commit: Commit

@js.native
trait Issues extends js.Object:
  def createComment(params: CreateCommentParams): js.Promise[CreateCommentResponse] = js.native

@js.native
trait CreateCommitCommentParams extends js.Object:
  def owner: String
  def repo: String
  def commit_sha: String
  def body: String

object CreateCommitCommentParams:
  def apply(
      owner: String,
      repo: String,
      commit_sha: String,
      body: String
  ): CreateCommitCommentParams =
    js.Dynamic
      .literal(
        owner = owner,
        repo = repo,
        commit_sha = commit_sha,
        body = body
      )
      .asInstanceOf[CreateCommitCommentParams]

@js.native
trait CreateCommitCommentResponse extends js.Object:
  def id: Int
  def body: String

@js.native
trait CreateCommentParams extends js.Object:
  def owner: String
  def repo: String
  def issue_number: Int
  def body: String

object CreateCommentParams:
  def apply(
      owner: String,
      repo: String,
      issue_number: Int,
      body: String
  ): CreateCommentParams =
    js.Dynamic
      .literal(
        owner = owner,
        repo = repo,
        issue_number = issue_number,
        body = body
      )
      .asInstanceOf[CreateCommentParams]

@js.native
trait CreateCommentResponse extends js.Object:
  def id: Int
  def body: String
