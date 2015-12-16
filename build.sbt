val baseSettings = BaseProject.metaSettings ++ BaseProject.scalaSettings ++
  BaseProject.scalariformSettings ++ BaseProject.releaseSettings ++
  Seq(
    publishMavenStyle := true,
    bintrayOrganization := Some("fidesmo"),
    bintrayPackageLabels := Seq("android", "nfc"),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
  )

lazy val base = project.in(file("."))
  .settings(publish := ())
  .aggregate(core, android)

lazy val core = project
  .settings((libraryDependencies +=
    "org.scalatest" %% "scalatest" % "2.2.4" % "test") ++ baseSettings)

lazy val android = project
  .settings(baseSettings)
  .dependsOn(core)
