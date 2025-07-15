package com.oAuth.google


import GoogleAuth.{GoogleTokenQueryParamMatcher, GoogleTokenResponse}
import cats.effect.{IO, IOApp}
import ciris.{ConfigDecoder, Secret, file}
import ciris.circe.circeConfigDecoder
import com.oAuth.ConfigUtils.Credential
import com.comcast.ip4s.*
import io.circe.Decoder
import io.circe.parser.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.{Accept, Authorization}
import org.http4s.server.*
import org.http4s.implicits.*
object GoogleAuth {

  object GoogleTokenQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  case class GoogleTokenResponse(accessToken: String, tokenType: String, scope: String)

 
  given decoder: Decoder[GoogleTokenResponse] = Decoder.instance { h =>
    for {
      accessToken <- h.get[String]("access_token")
      tokenType <- h.get[String]("token_type")
      scope <- h.get[String]("scope")
    } yield GoogleTokenResponse(accessToken, tokenType, scope)

  }
  

  def fetchGoogleToken(code: String, config: Credential): IO[Option[String]] = {
    val form = UrlForm(
      "code" -> code,
      "client_id" -> config.key,
      "client_secret" -> config.secret.value,
      "redirect_uri" -> "http://localhost:8080/googlecallback",
      "grant_type" -> "authorization_code"

    )

    val req = Request[IO](
      Method.POST,
      uri"https://oauth2.googleapis.com/token",
      headers = Headers(Accept(MediaType.application.json))
    ).withEntity(form)

    EmberClientBuilder.default[IO].build.use(client => client.expect[String](req))
      .map(jsonString => decode[GoogleTokenResponse](jsonString))
      .map {
        case Left(e) => None
        case Right(googleTokenResp) => Some(googleTokenResp.accessToken)
      }
  }

  def fetchGoogleUserInfo(token: String): IO[String] = {
    val req = Request[IO](
      Method.GET,
      uri"https://www.googleapis.com/oauth2/v3/userinfo",
      headers = Headers(
        Accept(MediaType.application.json),
        Authorization(Credentials.Token(AuthScheme.Bearer, token))
      )
    )
    EmberClientBuilder.default[IO].build.use(client => client.expect[String](req))
  }

  def getGoogleOAuthResult(code: String, config: Credential): IO[String] =
    for {
      maybeToken <- fetchGoogleToken(code, config)
      result <-
        maybeToken match {
          case Some(token) => fetchGoogleUserInfo(token)
          case None => IO(s"Authentication failed for Google.")
        }
    } yield result
}
