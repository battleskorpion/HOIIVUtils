package com.hoi4utils.clausewitz.localization

import com.hoi4utils.FileUtils
import com.hoi4utils.clausewitz.exceptions.LocalizationExistsException
import com.hoi4utils.clausewitz.{HOIIVUtils, HOIIVUtilsFiles}
import com.hoi4utils.ui.HOIIVUtilsWindow
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.*
import java.nio.file.{Files, Paths}
import java.util.Scanner
import scala.Option
import scala.jdk.javaapi.CollectionConverters

object EnglishLocalizationManager {
  protected val l_english = "l_english:"
  protected val versionNumberRegex = ":(\\d*)" //  (version number is optional)

  protected val language_def: String = l_english
}

class EnglishLocalizationManager extends LocalizationManager with FileUtils {
  val LOGGER: Logger = LogManager.getLogger(classOf[EnglishLocalizationManager])
  /* */ 
  setManager(this)

  /**
   * Localization collection for all English localization. 
   */
  final protected val localizationCollection = new LocalizationCollection

  //    public static void setLanguage(String language) {
  //        language_def = "l_" + language;
  //    }
  override def reload(): Unit = {
    localizationCollection.clear()
    // load all localization files within the localization folder and
    // any subfolders
    loadLocalization()
  }

  protected def loadLocalization(): Unit = {
    // Load mod localization after vanilla to give mod localizations priority
    if (HOIIVUtilsFiles.hoi4_localization_folder == null)
      LOGGER.warn("'HOI4 localization folder' is null.")
    else if (!HOIIVUtilsFiles.hoi4_localization_folder.exists)
      LOGGER.warn("'HOI4 localization folder' does not exist.")
    else if (!HOIIVUtilsFiles.hoi4_localization_folder.isDirectory)
      LOGGER.warn("'HOI4 localization folder' is not a directory.")
    else
      loadLocalization(HOIIVUtilsFiles.hoi4_localization_folder, Localization.Status.EXISTS)

    if (HOIIVUtilsFiles.mod_localization_folder == null)
      LOGGER.warn("'Mod localization folder' is null.")
    else if (!HOIIVUtilsFiles.mod_localization_folder.exists)
      LOGGER.warn("'Mod localization folder' does not exist.")
    else if (!HOIIVUtilsFiles.mod_localization_folder.isDirectory)
      LOGGER.warn("'Mod localization folder' is not a directory.")
    else
      loadLocalization(HOIIVUtilsFiles.mod_localization_folder, Localization.Status.VANILLA)
  }


  protected def loadLocalization(localizationFolder: File, status: Localization.Status): Unit = {
    val files = localizationFolder.listFiles
    if (files == null) return
    for (file <- files) {
      if (file.isDirectory) loadLocalization(file, status)
      else if (file.getName.endsWith(".yml")) loadLocalizationFile(file, status)
      else LOGGER.info("Localization files can only be of type .yml. File: {}", file.getAbsolutePath)
      LOGGER.debug("Loaded localization file: {}", file.getAbsolutePath)
    }
  }

  // todo wow :(
  protected def loadLocalizationFile(file: File, Status: Localization.Status): Unit = {
    val scanner = new Scanner(file)
    try {
      // check language
      var languageFound = false
      while (scanner.hasNextLine && !languageFound) {
        var line = scanner.nextLine
        /* ignore BOM */
        if (line.startsWith("\uFEFF")) line = line.substring(1)
        if (FileUtils.usefulData(line)) {
          if (!line.trim.startsWith(EnglishLocalizationManager.language_def)) {
            System.out.println("Localization file is not in English: " + file.getAbsolutePath)
            return
          }
          else languageFound = true
        }
      }
      if (!languageFound) {
        System.out.println("Localization file does not have a language definition: " + file.getAbsolutePath)
        return
      }
      while (scanner.hasNextLine) {
        val line = scanner.nextLine
        if (FileUtils.usefulData(line)) {
          val data = line.splitWithDelimiters(EnglishLocalizationManager.versionNumberRegex, 2)
          if (data.length != 3) {
            System.err.println("Invalid localization file format: " + file.getAbsolutePath + "\n\tline: " + line + "\n\tReason: incorrect number of line elements")
          } else {
            // trim whitespace
            for (i <- 0 until data.length) {
              data(i) = data(i).trim
            }
            // ignore ":" before version number
            data(1) = data(1).substring(1)
            // ignore escaped quotes
            data(2) = data(2).replaceAll("//\"", "\u0000")
            val startQuote = data(2).indexOf("\"")
            val endQuote = data(2).lastIndexOf("\"")
            val extra = data(2).substring(endQuote + 1).trim
            var invalid = false
            if (extra.nonEmpty && !extra.startsWith("#")) {
              System.err.println("Invalid localization file format: " + file.getAbsolutePath + "\n\tline: " + line + "\n\tReason: extraneous non-comment data after localization entry: " + extra)
              invalid = true
            }
            if (startQuote != 0 || endQuote == -1 || startQuote == endQuote) {
              System.err.println("Invalid localization file format: " + file.getAbsolutePath + "\n\tline: " + line + "\n\tReason: localization value is not correctly enclosed in quotes")
              invalid = true
            }
            if (!invalid) {
              // remove leading/trailing quotes (and any comments)
              data(2) = data(2).substring(startQuote + 1, endQuote)
              /*
              .yml example format:
              CONTROLS_GREECE: "Controls all states in the §Y$strategic_region_greece$§! strategic region"
              CONTROLS_ASIA_MINOR:1 "Controls all states in the §Y$strategic_region_asia_minor$§! strategic region"
              */
              var key: String = null
              var version: Integer = null
              var value: String = null
              if (data(1).isBlank) {
                key = data(0)
                version = null
                value = data(2)
              }
              else {
                key = data(0)
                version = data(1).toInt
                value = data(2)
              }
              // fix file format issues (as it is a UTF-8 BOM file)
              value = value.replaceAll("(Â§)", "§")
              val localization = new Localization(key, version, value, Status)
              localizationCollection.add(localization, file)
            }
          }
        }
      }
    } catch {
      case exc: IOException =>
        exc.printStackTrace()
    } finally if (scanner != null) scanner.close()
  }

  override def saveLocalization(): Unit = {
    val localizationFolder = HOIIVUtilsFiles.mod_localization_folder
    val files = localizationFolder.listFiles
    if (files == null) return
    // Separate new and changed localizations
    val changedLocalizations = CollectionConverters.asJava(localizationCollection.filterByStatus(Localization.Status.UPDATED))
    val newLocalizations = CollectionConverters.asJava(localizationCollection.filterByStatus(Localization.Status.NEW))
    // Save updated and new localizations
    changedLocalizations.forEach(entry => writeAllLocalization(entry._2.toList, entry._1))
    newLocalizations.forEach(entry => writeAllLocalization(entry._2.toList, entry._1))
  }

  def writeAllLocalization(list: List[Localization], file: File): Unit = {
    for (localization <- list) {
      val key = localization.ID
      val version = {
        if (localization.version == null) ""
        else String.valueOf(localization.version)
      }
      val value = localization.text
      
      localization.status match {
        case Localization.Status.UPDATED => writeLocalization(file, key, version, value, false)
        case Localization.Status.NEW => writeLocalization(file, key, version, value, true)
        case _ => throw new IllegalStateException("Unexpected value: " + localization.status)
      }
    }
  }

  /**
   * Use to replace existing localization with entry
   *
   * @param file
   * @param key
   * @param version
   * @param value
   * @param append
   */
  protected def writeLocalization(file: File, key: String, version: String, value: String, append: Boolean): Unit = {
    var entry = key + ":" + version + " \"" + value + "\""
    entry = entry.replaceAll("§", "Â§") // necessary with UTF-8 BOM

    val writer: PrintWriter = getLocalizationWriter(file, true)
    if (append) try {
      writer.println(entry)
    } catch {
      case exc: IOException =>
        System.err.println("Failed to write new localization to file. " + "\n\tLocalization: " + entry + "\n\tFile: " + file.getAbsolutePath)
    } finally {
      if (writer != null) writer.close()
    }
    else try {
      var lineReplaced = false
      val lines = Files.readAllLines(Paths.get(file.getAbsolutePath))
      for (i <- 0 until lines.size) {
        var continue = true
        if (lines.get(i).trim.startsWith(key) && continue) {
          lines.set(i, entry)
          lineReplaced = true
          System.out.println("Replaced localization " + key)
          continue = false 
        }
      }
      if (!lineReplaced) throw new IOException // todo better exception
      Files.write(Paths.get(file.getAbsolutePath), lines)
    } catch {
      case exc: IOException =>
        System.err.println("Failed to update localization in file. " + "\n\tLocalization: " + entry + "\n\tFile: " + file.getAbsolutePath)
    }
  }

  @throws[IOException]
  protected def getLocalizationWriter(file: File, append: Boolean) = new PrintWriter(new BufferedWriter(new FileWriter(file, append)))

  /**
   * {@inheritDoc }
   */
  @throws[LocalizationExistsException]
  override def addLocalization(localization: Localization, file: File): Unit = {
    addLocalization(localization, localizationCollection, file)
  }

  /**
   * Finds the localization corresponding to the given key/localization ID.
   *
   * @return the localization corresponding to the given ID, or null if not found.
   */
  @throws[IllegalArgumentException]
  override def getLocalization(key: String): Option[Localization] = {
    if (key == null) throw new IllegalArgumentException("localization ID cannot be null")
    localizationCollection.get(key)
  }
  
  override def localizations: LocalizationCollection = localizationCollection

  /**
   * Capitalizes every word in a string with a pre-set whitelist
   *
   * @param title
   * @return Returns the edited string unless the string has no words
   */
  override def titleCapitalize(title: String): String = {
    if (title == null) return null
    if (title.trim.isEmpty) return title

    val words = title.split(" ").toBuffer
    val whitelist = capitalizationWhitelist
    
    if (words.head.length == 1) words(0) = Character.toUpperCase(words.head.charAt(0)) + ""
    else if (words.head.length > 1) words(0) = Character.toUpperCase(words.head.charAt(0)) + words.head.substring(1)
    else {
      // todo this should never happen now right?
      System.out.println("first word length < 1")
    }
    
    System.out.println("num words: " + words.size)
    for (i <- 1 until words.size) {
      if (!LocalizationManager.isAcronym(words(i)) && !whitelist.contains(words(i))) {
        if (words(i).length == 1) {
          words(i) = Character.toUpperCase(words(i).charAt(0)) + ""
        }
        else if (words(i).length > 1) {
          // System.out.println("working cap");
          words(i) = Character.toUpperCase(words(i).charAt(0)) + words(i).substring(1)
        }
      }
    }
    
    System.out.println("capitalized: " + String.join(" ", CollectionConverters.asJava(words)))
    String.join(" ", CollectionConverters.asJava(words))
  }

  // todo let user change?
  override def capitalizationWhitelist: Set[String] = {
    Set("a", "above", "after", "among", // among us
      "an", "and", "around", "as", "at", "below", "beneath", "beside", "between", "but", "by", "for", "from", "if", "in", "into", "nor", "of", "off", "on", "onto", "or", "over", "since", "the", "through", "throughout", "to", "under", "underneath", "until", "up", "with")
  }

  override def toString: String = "EnglishLocalizationManager{" + "localizations=" + localizationCollection + "}"
}
