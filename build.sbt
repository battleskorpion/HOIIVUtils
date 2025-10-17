import sbt.Keys.skip
// build.

ThisBuild / organization := "com.hoi4utils"
ThisBuild / scalaVersion := "3.7.3"
ThisBuild / version      := "20.0.1"

lazy val root = project.in(file("."))
	.aggregate(hoi4utils.js, hoi4utils.jvm)
	.settings(
		name := "hoi4utils",
		publish := {},
		publishLocal := {},
		run / skip := true // no mains in lib style
	)

lazy val hoi4utils = crossProject(JSPlatform, JVMPlatform)
	.in(file("."))
	.settings(
		name := "hoi4utils-crossproject",
		//		libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0" // Example dependency
	)
	.jsSettings(
		// JS-specific settings
		scalaJSUseMainModuleInitializer := true,
		Compile / mainClass := Some("tutorial.webapp.TutorialApp")
	)
	.jvmSettings(
		// JVM-specific settings
		Compile / mainClass := Some("com.hoi4utils.HOI4Utils")
	)

lazy val hoi4utilsJS  = hoi4utils.js
lazy val hoi4utilsJVM = hoi4utils.jvm

addCommandAlias("runDev", "; hoi4utilsJVM/reStart --mode dev")


