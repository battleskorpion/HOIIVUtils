package com.hoi4utils.hoi4.gfx

import com.hoi4utils.hoi4.gfx.Interface.interfaceErrors
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Parser, ParserException}
import com.hoi4utils.script.{PDXError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.boundary

import scala.collection.parallel.CollectionConverters._

/**
 * Represents a .gfx file in interface folder
 * Contains a set of SpriteTypes
 */
object Interface extends PDXReadable with LazyLogging {
	private val gfxMap: mutable.Map[String, SpriteType] = new mutable.HashMap
	private var interfaceFiles: mutable.Map[File, Interface] = new mutable.HashMap
	var interfaceErrors: ListBuffer[PDXError] = ListBuffer.empty[PDXError]

	def getGFX(icon: String): Option[String] = {
		getSpriteType(icon) match {
			case Some(spriteType) => Some(spriteType.gfx)
			case None => None
		}
	}

	def getSpriteType(icon: String): Option[SpriteType] = gfxMap.get(icon)

	def numGFX: Int = gfxMap.size

	/** Lists all SpriteType objects found in interface files */
	def listGFX: Iterable[SpriteType] = gfxMap.values

	/** Returns all .gfx files parsed */
	def listGFXFiles: Iterable[Interface] = interfaceFiles.values

	/** Lists .gfx files which Interface class represents and which have been read in. */
	def list: Iterable[Interface] = listGFXFiles

	/** Returns the number of .gfx interface files read */
	def numFiles: Int = interfaceFiles.size

	private def readMod(): Boolean = {
		if (!HOIIVFiles.Mod.interface_folder.exists || !HOIIVFiles.Mod.interface_folder.isDirectory) {
			logger.error("Warning: mod interface directory does not exist")
			false
		} else if (HOIIVFiles.Mod.interface_folder.listFiles == null || HOIIVFiles.Mod.interface_folder.listFiles.isEmpty) {
			logger.error("Warning: mod interface directory is empty")
			false
		} else {
			for (file <- HOIIVFiles.Mod.interface_folder.listFiles.filter(_.getName.endsWith(".gfx"))) {
				interfaceFiles.put(file, new Interface(file))
			}
			true
		}
	}

	private def readHoi4(): Boolean = {
		if (!HOIIVFiles.HOI4.interface_folder.exists || !HOIIVFiles.HOI4.interface_folder.isDirectory) {
			logger.error("HOI4 interface directory does not exist")
			false
		} else if (HOIIVFiles.HOI4.interface_folder.listFiles == null || HOIIVFiles.HOI4.interface_folder.listFiles.isEmpty) {
			logger.error("HOI4 interface directory is empty")
			false
		} else {
			val gfxFiles = HOIIVFiles.HOI4.interface_folder.listFiles.filter(_.getName.endsWith(".gfx"))
			val interfaceFilesMap = gfxFiles.par.map(f => f -> new Interface(f)).seq

			interfaceFilesMap.foreach { (f, interface) =>
				interfaceFiles.put(f, interface)
			}
			true
		}
	}

	def read(): Boolean = {
		val modSuccess = readMod()
		val hoi4Success = readHoi4()
		modSuccess && hoi4Success
	}

	override def clear(): Unit = {
		gfxMap.clear()
		interfaceFiles.clear()
	}
}

/**
 * interface file class
 *
 * @param file .gfx file to be read
 *
 * read all in mod\nadivided-dev\interface
 * <p> format example: </p> <pre>
 * spriteTypes = {

 *   SpriteType = {
 *     name = "GFX_focus_SVA_virginia_officers"
 *     texturefile = "gfx/interface/goals/focus_SVA_virginia_officers.dds"
 *   }
 *   SpriteType = {
 *     name = "GFX_focus_generic_chromium"
 *     texturefile = "gfx/interface/goals/focus_generic_chromium.dds"
 *   }
 *   SpriteType = {
 *     name = "GFX_focus_generic_manpower"
 *     texturefile = "gfx/interface/goals/focus_generic_manpower.dds"
 *   }
 * } </pre>
*/
class Interface(private val file: File) extends LazyLogging {
	private var spriteTypes: mutable.Set[SpriteType] = new mutable.HashSet

	/* init */
	readGFXFile(file)

	private def readGFXFile(file: File): Unit = {
		spriteTypes.clear()

		val parser = new Parser(file)
		try {
			parser.parse
		} catch {
			case e: ParserException =>
				val pdxError = new PDXError(
					exception = e,
					file = Some(file),
					additionalInfo = Map("context" -> "Error parsing interface .gfx file")
				)
				interfaceErrors += pdxError
				return ()
		}

		/* load listed sprites */
		val spriteTypeNodes = {
			parser.rootNode.filterCaseInsensitive("spriteTypes").subFilterCaseInsensitive("spriteType")
		}
		if (spriteTypeNodes == null) {
			val pdxError = new PDXError(
				file = Some(file),
				additionalInfo = Map(
					"context" -> "No SpriteTypes defined in interface .gfx file",
					"reason" -> "spriteTypeNodes is null"
				)
			)
			interfaceErrors += pdxError
			return ()
		}

		val validSpriteTypes = spriteTypeNodes.filter(_.containsAllCaseInsensitive("name", "texturefile"))
		for (spriteType <- validSpriteTypes) {
			try {
				val name = spriteType.getValueCaseInsensitive("name").$stringOrElse("").replace("\"", "")
				val filename = spriteType.getValueCaseInsensitive("texturefile").$stringOrElse("").replace("\"", "")
				if (name.isEmpty || filename.isEmpty) {
					val pdxError = new PDXError(
						file = Some(file),
						additionalInfo = Map(
							"context" -> "SpriteType in interface .gfx file has empty name or texturefile",
							"name" -> name,
							"filename" -> filename
						)
					)
					interfaceErrors += pdxError
				}
				else {
					val gfx = new SpriteType(name, filename, basepath = file.getParentFile.getParentFile)
					spriteTypes.add(gfx)
					Interface.gfxMap.put(name, gfx)
				}
			} catch {
				case e: ParserException =>
					val pdxError = new PDXError(
						exception = e,
						file = Some(file),
						additionalInfo = Map("context" -> "Error parsing SpriteType in interface .gfx file")
					)
					interfaceErrors += pdxError
			}
		}
	}

	/**
	 * Returns the name of the file represented by an instance of the Interface class
	 *
	 * @return name of this Interface file
	 */
	def getName = file.getName

	/**
	 * Returns the path for the file represented by an instance of the Interface class
	 *
	 * @return filepath for this Interface file
	 */
	def getPath = file.getPath
	
	def getGFXMAP: mutable.Map[String, SpriteType] = Interface.gfxMap
}
