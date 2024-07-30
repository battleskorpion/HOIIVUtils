package com.hoi4utils.clausewitz.data.focus

import com.hoi4utils.clausewitz.HOIIVFile
import com.hoi4utils.clausewitz.data.country.CountryTagsManager
import com.hoi4utils.clausewitz.script.*
import com.hoi4utils.clausewitz.localization.Localizable
import com.hoi4utils.clausewitz.data.country.CountryTag
import com.hoi4utils.clausewitz.localization.Localizable.Property
import org.jetbrains.annotations.*

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable

/**
 * ALL of the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
// todo extends file?
object FocusTree {
  private val focusTrees = new mutable.HashMap[File, FocusTree]()
  private val focusTreesList = new mutable.ListBuffer[FocusTree]()

  // private Modifier countryModifier;
  // private boolean defaultFocus; // ! todo Do This
  // private Point continuousFocusPosition; // ! todo DO THIS
  def get(focus_file: File): Option[FocusTree] = {
    if (!focusTrees.contains(focus_file)) new FocusTree(focus_file)
    focusTrees.get(focus_file)
  }

  //def observeFocusTrees: ObservableList[FocusTree] = new ObservableList[FocusTree](focusTreesList)

  def read(): Unit = {
    if (!HOIIVFile.mod_focus_folder.exists || !HOIIVFile.mod_focus_folder.isDirectory) {
      System.err.println("Focus folder does not exist or is not a directory.")
      return
    }
    if (HOIIVFile.mod_focus_folder.listFiles == null || HOIIVFile.mod_focus_folder.listFiles.length == 0) {
      System.err.println("No focuses found in " + HOIIVFile.mod_focus_folder)
      return
    }
    for (f <- HOIIVFile.mod_focus_folder.listFiles) {
      if (f.getName.endsWith(".txt")) new FocusTree(f)
    }
  }

  def add(focusTree: FocusTree): mutable.HashMap[File, FocusTree] = {
    focusTrees.put(focusTree.focus_file, focusTree)
    new mutable.HashMap[File, FocusTree](focusTrees)
  }

  def listFocusTrees: Iterable[FocusTree] = focusTrees.values

  /**
   * Returns focus tree corresponding to the tag, if it exists
   *
   * @param tag
   * @return The focus tree, or null if could not be found/not yet created.
   */
  def get(tag: CountryTag): FocusTree = {
    //focusTrees.values.stream.filter((focusTree: FocusTree) => focusTree.country.nodeEquals(tag)).findFirst.orElse(null)
    for (tree <- listFocusTrees) {
      if (tree.country.nodeEquals(tag)) return tree
    }
    null
  }

  def getdankwizardisfrench(tag: CountryTag): FocusTree = {
    for (tree <- listFocusTrees) {
      assert(tree.country.get() != null)
      if (tree.country.nodeEquals(tag)) return tree
    }
    null
  }

//  private def updateObservableValues(): Unit = {
//    //focusTreesList.setAll(focusTrees.values)
//  }

//  try focusTrees.addListener((change: MapChangeListener.Change[_ <: File, _ <: FocusTree]) => {
//    updateObservableValues()
//    focusTreesList.sort(Comparator.naturalOrder)
//
//  }.asInstanceOf[MapChangeListener[File, FocusTree]])

}

class FocusTree private(private var focus_file: File)
  extends StructuredPDX("focus_tree") with Localizable with Comparable[FocusTree] with Iterable[Focus] {
  /* pdxscript */
  final var country: ReferencePDX[CountryTag] = new ReferencePDX[CountryTag](() => CountryTag.toList, t => t.get(), "country")
  final var focuses: MultiPDX[Focus] = new MultiPDX[Focus](() => new Focus(this), "focus")
  final var id: AbstractPDX[String] = new StringPDX("id")
  private val focusIDList: ListBuffer[String] = null

  //obj.addAll(childScripts)  // todo
  loadPDX(focus_file)
  FocusTree.add(this)
  //private final ObservableMap<String, Focus> focuses;

  override protected def childScripts: Iterable[? <: PDXScript[?]] = List(id, country, focuses)

//  private def checkPendingFocusReferences(): Unit = {
//  }

  /**
   * Lists last set of focuses
   *
   * @return
   */
  def listFocusIDs: ListBuffer[String] = {
    if (focusIDList == null) return null // bad :(
    focusIDList
  }

  def countryTag: CountryTag = if (country.get() != null) country.get()
  else {
    // idk :(
    null
  }

  def focusFile: File = focus_file

  override def equals(other: AnyRef): Boolean = {
    if (other == null) return false
    if (other.getClass eq this.getClass) return this.focus_file eq other.asInstanceOf[FocusTree].focus_file
    false
  }

  override def toString: String = {
    if (id.get() != null) return id.get()
    if (country.get() != null) return country.get().toString
    super.toString
  }

  def minX: Int = {
    if (focuses.isEmpty) return 0
    focuses.minBy(_.absoluteX).absoluteX
  }

  def listFocuses: List[Focus] = {
    focuses.toList
  }

  override def compareTo(o: FocusTree): Int = {
    var c = 0
    if (this.country.get() != null && o.country.get() != null) c = this.country.get().compareTo(o.country.get())
    var d = 0
    if (this.id.get() != null && o.id.get() != null) d = this.id.get().compareTo(o.id.get())
    if (c == 0) d
    else c
  }

  override def iterator: Iterator[Focus] = focuses.iterator

  override def getLocalizableProperties: mutable.HashMap[Localizable.Property, String] = {
    // lets us map null if we use hashmap instead of generic of() method
    val properties = new mutable.HashMap[Localizable.Property, String]
    properties.put(Property.NAME, id.get())
    properties
  }

  /**
   * Get the localizable group of this focus tree, which is the list of focuses.
   *
   * @return
   */
  override def getLocalizableGroup: Iterable[? <: Localizable] = focuses

  override def nodeEquals(other: PDXScript[?]): Boolean = {
    if (other.isInstanceOf[FocusTree]) return this == other
    false
  }
}