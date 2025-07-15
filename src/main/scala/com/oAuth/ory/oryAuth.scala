package com.oAuth.ory

import cats.effect.IO
import org.http4s.client.dsl.io.*
import org.http4s.Method.*
import org.http4s.{Headers, MediaType, Method, Request, RequestCookie, Uri, UrlForm}
import org.http4s.circe.*
import io.circe.Json
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.Accept
import org.http4s.implicits.uri

object oryAuth {
  def getOryOAuthResult(sessionCookie: RequestCookie): IO[String] =
    val req: Request[IO] = Request[IO](
      Method.GET,
      uri"http://127.0.0.1:4433/sessions/whoami",
      headers = Headers(
        Accept(MediaType.application.json),
      )
    ).addCookie(sessionCookie)
    EmberClientBuilder.default[IO].build.use(client => client.expect[String](req))
}
