// build.

ThisBuild / scalaVersion := "3.7.3"

lazy val root = project.in(file(".")).
	aggregate(hoi4utils.js, hoi4utils.jvm).
	settings(
		publish := {},
		publishLocal := {},
	)

lazy val hoi4utils = crossProject(JSPlatform, JVMPlatform)
	.in(file("."))
	.settings(
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



