package com.oAuth

import cats.effect.{IO, IOApp}
import ciris.circe.circeConfigDecoder
import ciris.{ConfigDecoder, Secret, file}
import com.oAuth.ConfigUtils.{AppConfig, Credential}
import com.oAuth.github.GithubAuth.*
import com.oAuth.google.GoogleAuth.*
import com.comcast.ip4s.*
import com.oAuth.ory.oryAuth.getOryOAuthResult
import io.circe.Decoder
import io.circe.parser.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.{Accept, Authorization}
import org.http4s.implicits.*
import org.http4s.server.*

import java.nio.file.Paths

object OAuthDemo extends IOApp.Simple {

  /*
    1. User tries to log into my small app -> click a link
    2. redirect the user to the providers authorization page (github)
    3. user is redirected to a "callback" page on the small app with a code
    4. (in the back end) server gets data from the auth provider with that code
    5. (on github) auth server responds with an auth token and some data
    6. my small app shows something to the user depending on the token and the data
   */
  val appConfig = file(Paths.get("src/main/resources/appConfig.json")).as[AppConfig]
  
  val dsl = Http4sDsl[IO]
  
  import dsl.*
  
  def routes(config: AppConfig): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root / "home" =>
      StaticFile 
        .fromString("src/main/resources/html/index.html", Some(req))
        .getOrElseF(NotFound())
    case GET -> Root / "callback" :? GithubTokenQueryParamMatcher(code) => // client ID and secret loaded from appConfig.json
      getGithubOAuthResult(code, config.credentials.head).flatMap(Ok(_))
      
    case GET -> Root / "googlecallback" :? GoogleTokenQueryParamMatcher(code) => // client ID and secret loaded from appConfig.json
      getGoogleOAuthResult(code, config.credentials.last).flatMap(Ok(_))
      
    case req @ GET -> Root / "orycallback" => req.cookies.find(_.name == "ory_kratos_session") // client id and secret set in contrib/quickstart/kratos/email-password/kratos.yml
    .map {
      (cookie: RequestCookie) => Ok(getOryOAuthResult(cookie))
    }.getOrElse(NotFound("ERROR!!!!"))
  }

  override def run: IO[Unit] = for {
    config <- appConfig.load[IO] // IO[AppConfig]
    server <- EmberServerBuilder
    .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes(config).orNotFound)
      .build
      .use(_ => IO(println("Server up and running on http://localhost:8080")) *> IO.never)
  } yield ()
}


