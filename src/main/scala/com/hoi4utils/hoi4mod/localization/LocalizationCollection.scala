package com.hoi4utils.hoi4mod.localization

import java.io.File
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.boundary

class LocalizationCollection extends mutable.HashMap[File, ListBuffer[Localization]] {

  /**
   * very important for lookup by key optimization.
   */
  final private val localizationKeyMap = new TrieMap[String, Localization]

  // Custom method to add a Localization
  def add(localization: Localization, file: File): Unit = {
    if (localization == null || file == null) throw new IllegalArgumentException("Localization and file must not be null")

    val localizationList = this.getOrElseUpdate(file, new ListBuffer[Localization])
//    val localizationsList = this.computeIfAbsent(file, (k: File) => new ListBuffer[Localization])
    localizationList.addOne(localization)
    localizationKeyMap.put(localization.id, localization) // Indexing for fast retrieval
  }

  // Custom method to remove a Localization
  def remove(localization: Localization): Boolean = {
    var anyRemoved = false
    if (localization == null) return false
    this.foreach { case (_, localizationsList) =>
      val removed = localizationsList.exists(_.equals(localization))
      if (removed) {
        localizationsList -= localization
        localizationKeyMap.remove(localization.id) // Remove from index
        anyRemoved = true
      }
    }
    if (anyRemoved) {
      removeListIfEmpty()
    }
    anyRemoved
//    val removed = this.values.parallelStream.anyMatch((localizationsList: List[Localization]) => localizationsList.remove(localization))
//    if (removed) localizationKeyMap.remove(localization.ID) // Remove from index
//    removeListIfEmpty()
//    removed
  }

  private def removeListIfEmpty(): Unit = {
    //this.values.removeIf(List.isEmpty)
    this.filterInPlace((_, localizationsList) => localizationsList.nonEmpty)
  }

  // Custom method to get all Localizations
  def getAll: Iterable[Localization] = {
    localizationKeyMap.values
    //localizationKeyMap.values.parallelStream.toList
  }

  def getAll(localizationKeys: Iterable[String]): Iterable[Localization] = {
    localizationKeyMap.values.filter { localization => localizationKeys.toList.contains(localization.id) }
    //localizationKeyMap.values.parallelStream.filter((localization: Localization) => localizationKeys.contains(localization.ID)).toList
  }

  // total number of localizations
  def numLocalization: Int = {
    //localizationKeyMap.size
    this.values.map(_.size).sum
    //this.values.parallelStream.mapToInt(util.List.size).sum
  }

  override def put(key: File, value: ListBuffer[Localization]): Option[ListBuffer[Localization]] = {
    throw new UnsupportedOperationException("Use add(Localization, File) method instead")
  }

  override def remove(key: File): Option[ListBuffer[Localization]] = throw new UnsupportedOperationException("Use remove(Localization) method instead")

  //override def containsKey(key: AnyRef) = throw new UnsupportedOperationException("Use containsLocalizationKey(String) method instead")

  /**
   * Checks if the given localization ID is localized (has a localization entry).
   * @param id the ID of the localization to check
   * @return true if the localization is localized, false otherwise
   */
  def containsLocKey(id: String): Boolean = {
    // Fast lookup from index
    localizationKeyMap.keySet.contains(id)
    // localizationKeyMap.containsKey(id)
  }

  def get(key: String): Option[Localization] = localizationKeyMap.get(key) // Fast lookup from index

  // Method to filter localizations by status
//  def filterByStatus(status: Localization.Status): Iterable[util.Map.Entry[File, util.List[Localization]]] = {
//    this.entrySet.parallelStream.map((entry: util.Map.Entry[File, util.List[Localization]]) => util.Map.entry(entry.getKey, entry.getValue.parallelStream.filter((localization: Localization) => localization.status eq status).toList)).filter((entry: util.Map.Entry[File, util.List[Localization]]) => !entry.getValue.isEmpty).toList
//  }
  def filterByStatus(status: Localization.Status): Iterable[(File, Iterable[Localization])] = {
  // todo parallel stream?
    this.map { case (file, localizations) =>
      val filteredLocalizations = localizations.filter(_.status == status)
      if (filteredLocalizations.nonEmpty) Some(file -> filteredLocalizations) else None
    }.flatten
//      this.entrySet.asScala // Convert the Java Set to Scala Set
//      .par // Parallel processing
//      .map { entry =>
//        val filteredLocalizations = entry.getValue.asScala // Convert the Java List to Scala List
//          .filter(_.status == status) // Filter by status
//        if (filteredLocalizations.nonEmpty) Some(entry.getKey -> filteredLocalizations) else None
//      }
//      .flatten // Remove any None values
  }

  def replace(key: String, localization: Localization): Option[Localization] = boundary {
    if (localization == null) throw new IllegalArgumentException("Localization must not be null")

    foreach { case (file, localizationsList) =>
      val index = localizationsList.indexWhere(_.id == key)
      if (index != -1) {
        val prevLocalization = localizationsList(index)
        localizationsList(index) = localization
        localizationKeyMap.put(localization.id, localization) // Update index
        boundary.break(Some(prevLocalization))
      }
    }
    None
  }
//    for (entry <- this.entrySet) {
//      val localizationsList = entry.getValue
//      for (i <- 0 until localizationsList.size) {
//        val currentLocalization = localizationsList.get(i)
//        if (currentLocalization.ID == key) {
//          // Replace the localization and return the old one
//          localizationsList.set(i, localization)
//          return currentLocalization
//        }
//      }
//    }
//    // Return null if no localization with the given key was found
//    null

  def getLocalizationFile(key: String): File = {
    // todo parallel stream?
    this.filter { case (file, localizations) => localizations.exists(_.id.equals(key)) }.keys.headOption.orNull
    //this.entrySet.parallelStream.filter((entry: util.Map.Entry[File, util.List[Localization]]) => entry.getValue.parallelStream.anyMatch((localization: Localization) => localization.ID == key)).map(util.Map.Entry.getKey).findFirst.orElse(null)
  }

  def getLocalizationFiles: Iterable[File] = this.keys
}
