import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSLinkerConfig
import sbt.Keys.{fork, skip}
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
		/**
		 * Configure Scala.js to emit modules in the optimal way to
		 * connect to Vite's incremental reload.
		 * - emit ECMAScript modules
		 * - emit as many small modules as possible for classes in the "livechart" package
		 * - emit as few (large) modules as possible for all other classes
		 *   (in particular, for the standard library)
		 */
		scalaJSLinkerConfig ~= {
			_.withModuleKind(ModuleKind.ESModule)
				.withModuleSplitStyle(
					ModuleSplitStyle.SmallModulesFor(List("hoi4utils")))
		},
		/**
		 * Depend on the scalajs-dom library.
     * It provides static types for the browser DOM APIs.
     */
		libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
	)
	.jvmSettings(
		// JVM-specific settings
		Compile / mainClass := Some("com.hoi4utils.HOI4Utils"),
		run / fork := true
	)

lazy val hoi4utilsJS  = hoi4utils.js
lazy val hoi4utilsJVM = hoi4utils.jvm

addCommandAlias("runDev", ";hoi4utilsJVM/reStart --mode dev;hoi4utilsJS/fastLinkJS")


