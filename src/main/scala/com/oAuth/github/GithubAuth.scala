package com.oAuth.github

import cats.effect.IO
import com.oAuth.ConfigUtils.Credential
import io.circe.Decoder
import org.http4s.*
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.{Accept, Authorization}
import org.http4s.implicits.uri
import io.circe.Decoder
import io.circe.parser.*

object GithubAuth {

  object GithubTokenQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  case class GithubUser(email: String, primary: Boolean, verified: Boolean) derives Decoder

  case class GithubTokenResponse(accessToken: String, tokenType: String, scope: String)

  given decoder: Decoder[GithubTokenResponse] = Decoder.instance { h =>
    for {
      accessToken <- h.get[String]("access_token")
      tokenType <- h.get[String]("token_type")
      scope <- h.get[String]("scope")
    } yield GithubTokenResponse(accessToken, tokenType, scope)
  }

  

  def fetchGithubToken(code: String, config: Credential): IO[Option[String]] = {
    val form = UrlForm(
      "client_id" -> config.key,
      "client_secret" -> config.secret.value,
      "code" -> code
    )

    val req = Request[IO](
      Method.POST,
      uri"https://github.com/login/oauth/access_token",
      headers = Headers(Accept(MediaType.application.json))
    ).withEntity(form)

    EmberClientBuilder.default[IO].build.use(client => client.expect[String](req))
      .map(jsonString => decode[GithubTokenResponse](jsonString))
      .map {
        case Left(e) => None
        case Right(githubTokenResp) => Some(githubTokenResp.accessToken)
      }
  }

  def fetchGithubUserInfo(token: String): IO[String] = {
    val req = Request[IO](
      Method.GET,
      uri"https://api.github.com/user/emails",
      headers = Headers(
        Accept(MediaType.application.json),
        Authorization(Credentials.Token(AuthScheme.Bearer, token))
      )
    )
    EmberClientBuilder.default[IO].build.use(client => client.expect[String](req))
      .map { response =>
        decode[List[GithubUser]](response).toOption.flatMap(listOfGithubUser => 
          listOfGithubUser.find(githubUser => githubUser.primary)) match {
          case Some(user) => s"Success! Logged in as ${user.email}"
          case None => "No primary email on Github... weird"
        }
      }
  }

  def getGithubOAuthResult(code: String, config: Credential): IO[String] = // TODO May need to make another one of these for google
    for {
      maybeToken <- fetchGithubToken(code, config)
      result <-
        maybeToken match {
          case Some(token) => fetchGithubUserInfo(token)
          case None => IO("Authentication failed for Github.")
        }
    } yield result

}
