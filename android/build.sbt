import android.Keys._

android.Plugin.androidBuildAar

platformTarget in Android := "android-27"

name := "nordpol-android"

// Do not append Scala versions to the generated artifacts
crossPaths := false

// Prevents the scala stdlib from beeing included automatically
autoScalaLibrary := false

sources in (Compile, doc) <<= sources in (Compile, doc) map {
  _.filterNot(_.getName == "R.java") }

javacOptions in (Compile, compile) ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation")
