lazy val base = project.in(file("."))
  .settings(BaseProject.releaseSettings: _*)
  .settings(publish := ())
  .aggregate(core, android)

lazy val core = project
  .settings(libraryDependencies +=
    "org.scalatest" %% "scalatest" % "2.2.4" % "test")

lazy val android = project
  .dependsOn(core)
