name := "scout"

organization := "scout"

version := "0.0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"   % "3.0.8"  % "test",
  "org.scalacheck" %% "scalacheck"  % "1.14.0" % "test"
)

lazy val commonCompilerOptions =
  Seq(
      "-unchecked",
      "-encoding", "UTF-8",
      "-deprecation",
      "-feature",
      "-Ypartial-unification"
    )

scalacOptions ++= 
  commonCompilerOptions ++ 
  Seq(
      "-Xfatal-warnings",
      "-Xlint:_",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-unused-import",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
     )

scalacOptions in (Compile, console) := commonCompilerOptions

scalacOptions in (Test, console) := commonCompilerOptions

