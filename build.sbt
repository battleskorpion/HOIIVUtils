// build.sbt
import sbt.CrossType
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

lazy val shared = crossProject(JSPlatform, JVMPlatform)
	.in(file("shared"))
	.settings(
		scalaVersion := "3.7.3", // Or your desired Scala version
		name := "shared",
//		libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0" // Example dependency
	)

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js

lazy val frontend = project
	.in(file("js"))
	.settings(
		scalaVersion := "3.7.3",
		name := "frontend",
		libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.9.0", // Example Scala.js specific dependency
		dependsOn(sharedJS)
	)
	.enablePlugins(ScalaJSPlugin)

lazy val backend = project
	.in(file("jvm"))
	.settings(
		scalaVersion := "3.7.3",
		name := "backend",
		dependsOn(sharedJVM)
	)

