lazy val base = project.in(file("."))
  .settings(BaseProject.releaseSettings: _*)
  .settings(publish := ())
  .aggregate(core, android)

lazy val core = project

lazy val android = project
  .dependsOn(core)
