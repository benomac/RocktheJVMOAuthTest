ThisBuild / version := "0.1.0-SNAPSHOT"

val scala3Version = "3.3.0"
val Http4sVersion = "0.23.18"
val CirisVersion = "3.2.0"
val CirceVersion = "0.14.1"


val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
val emberServer =     "org.http4s"                %% "http4s-ember-server" % Http4sVersion
val emberClient =     "org.http4s"                %% "http4s-ember-client" % Http4sVersion
val http4sDsl =       "org.http4s"                %% "http4s-dsl"          % Http4sVersion
val ciris =           "is.cir"                    %% "ciris"               % CirisVersion
val cirisCirce =      "is.cir"                    %% "ciris-circe"         % CirisVersion
val circeLibs =  Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-parser"
).map(_ % CirceVersion)

lazy val root = (project in file("."))
  .settings(
    name := "OauthRTJVMDemo",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
    emberServer,
    emberClient,
    http4sDsl,
    http4sCirce,
    ciris,
    cirisCirce
) ++ circeLibs
  )

