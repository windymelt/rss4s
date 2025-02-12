val scala3Version = "3.3.5"

lazy val root = project
  .in(file("."))
  .settings(
    organization         := "dev.capslock",
    organizationName     := "capslock.dev",
    organizationHomepage := Some(url("https://www.3qe.us")),
    startYear            := Some(2025),
    licenses += ("BSD 2-Clause", url(
      "https://opensource.org/licenses/BSD-2-Clause",
    )),
    homepage := Some(url("https://github.com/windymelt/rss4s")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/windymelt/rss4s"),
        "https://github.com/windymelt/rss4s.git",
      ),
    ),
    developers += Developer(
      id = "windymelt",
      name = "windymelt",
      email = "windymelt@capslock.dev",
      url = url("https://www.3qe.us"),
    ),
    name                                            := "rss4s",
    version                                         := "0.1.0-SNAPSHOT",
    scalaVersion                                    := scala3Version,
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
    libraryDependencies += "org.typelevel"          %% "cats-core" % "2.13.0",
    libraryDependencies += "org.scalactic"          %% "scalactic" % "3.2.19",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  )

// Publishing settings

import xerial.sbt.Sonatype.sonatypeCentralHost
ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
