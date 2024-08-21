//package com.hoi4utils.clausewitz.data.country
//
//import com.hoi4utils.clausewitz.script.{AbstractPDX, PDXScript, StringPDX}
//import com.hoi4utils.clausewitz_parser.Node
//import org.jetbrains.annotations.NotNull
//
//import scala.collection.mutable.ListBuffer
//
//object CountryTagPDX extends Iterable[CountryTagPDX] {
//  private lazy val _tagList: ListBuffer[CountryTagPDX] = {
//    ListBuffer[CountryTagPDX]()
//  }
//
//  override def iterator: Iterator[CountryTagPDX] = {
//    _tagList.iterator
//  }
//
//  def addTag(tag: CountryTagPDX): Unit = {
//    _tagList.addOne(tag)
//  }
//}
//
////class CountryTag(tag: String) extends StringPDX(List("tag")) {
////  setNode(tag)
////  CountryTag.addTag(this)
//class CountryTagPDX(expression: Node) extends StringPDX(List("tag")) {
////  setNode(tag)
//  loadPDX(expression)
//  CountryTagPDX.addTag(this)
//  tag
//  
//  def tag: CountryTag = {
//    super.get() match
//      case Some(tag) => CountryTag(tag)
//      case None => CountryTag.NULL_TAG
//  }
//  
//  override def set(obj: String): Unit = {
//    setNode(obj)
//    tag
//  }
//
//  override def equals(other: PDXScript[?]): Boolean = {
//    other match {
//      case other: CountryTagPDX =>
//        super.equals(other)
//      case _ => false
//    }
//  }
//
//  override def toString: String = {
//    get() match {
//      case Some(tag) => tag
//      case None => CountryTag.NULL_TAG.toString
//    }
//  }
//}
