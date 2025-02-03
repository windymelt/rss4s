val scala3Version = "3.6.3"

lazy val root = project
  .in(file("."))
  .settings(
    name                                            := "rss4s",
    version                                         := "0.1.0-SNAPSHOT",
    scalaVersion                                    := scala3Version,
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
    libraryDependencies += "org.scalactic"          %% "scalactic" % "3.2.19",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  )
