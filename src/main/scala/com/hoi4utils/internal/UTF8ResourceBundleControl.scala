package com.hoi4utils.internal

import java.io.{IOException, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.util.{Locale, PropertyResourceBundle, ResourceBundle}

/**
 * Custom ResourceBundle.Control that loads .properties files using UTF-8 encoding
 * instead of the default ISO-8859-1 encoding.
 *
 * This is necessary for properly loading property files with non-Latin characters
 * (e.g., Cyrillic, Chinese, Japanese, etc.).
 *
 * Usage:
 * {{{
 * val bundle = ResourceBundle.getBundle("i18n.menu", locale, UTF8ResourceBundleControl())
 * }}}
 */
class UTF8ResourceBundleControl extends ResourceBundle.Control:

  @throws[IOException]
  @throws[IllegalAccessException]
  @throws[InstantiationException]
  override def newBundle(
      baseName: String,
      locale: Locale,
      format: String,
      loader: ClassLoader,
      reload: Boolean
  ): ResourceBundle =
    if format != "java.properties" then
      return super.newBundle(baseName, locale, format, loader, reload)

    val bundleName = toBundleName(baseName, locale)
    val resourceName = toResourceName(bundleName, "properties")

    var bundle: ResourceBundle = null
    var stream = loader.getResourceAsStream(resourceName)

    if stream != null then
      try
        // Load properties file using UTF-8 encoding
        val reader = new InputStreamReader(stream, StandardCharsets.UTF_8)
        bundle = new PropertyResourceBundle(reader)
        reader.close()
      finally
        stream.close()

    bundle

object UTF8ResourceBundleControl:
  def apply(): UTF8ResourceBundleControl = new UTF8ResourceBundleControl()