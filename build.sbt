import sbt.Compile
import sbt.Keys.unmanagedSourceDirectories
// build.

ThisBuild / scalaVersion := "3.7.3"

Compile / unmanagedSourceDirectories := (Compile / unmanagedSourceDirectories).value.filterNot { dir =>
	dir.getName == "src"
}

lazy val root = project.in(file("."))
	.aggregate(hoi4utils.js, hoi4utils.jvm)
	.settings(
		name := "hoi4utils",
		publish := {},
		publishLocal := {},
		Compile / unmanagedSourceDirectories := Seq(),
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


