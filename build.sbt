// build.
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
ThisBuild / scalaVersion := "3.7.3"

lazy val root = project.in(file(".")).
	aggregate(hoi4utils.js, hoi4utils.jvm).
	settings(
		name := "hoi4utils",
		publish := {},
		publishLocal := {},
	)

lazy val hoi4utils = crossProject(JSPlatform, JVMPlatform)
	.in(file("."))
	.settings(
		name := "hoi4utils-crossproject",
		version := "20.0.1",
//		libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0" // Example dependency
	)
	.jvmSettings(
		// JVM-specific settings
	)
	.jsSettings(
		// JS-specific settings
		scalaJSUseMainModuleInitializer := true,
	)

lazy val hoi4utilsJS  = hoi4utils.js
lazy val hoi4utilsJVM = hoi4utils.jvm


