// build.sbt
import sbt.CrossType
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

lazy val hoi4utils = crossProject(JSPlatform, JVMPlatform)
	.in(file("."))
	.settings(
		scalaVersion := "3.7.3", // Or your desired Scala version
		name := "hoi4utils",
		version := "20.0.1",
//		libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0" // Example dependency
	)
	.jvmSettings(
		// JVM-specific settings
		libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
	)
	.jsSettings(
		// JS-specific settings
		scalaJSUseMainModuleInitializer := true,
	)

lazy val root = project.in(file(".")).
	aggregate(hoi4utils.js, hoi4utils.jvm).
	settings(
		publish := {},
		publishLocal := {},
	)

