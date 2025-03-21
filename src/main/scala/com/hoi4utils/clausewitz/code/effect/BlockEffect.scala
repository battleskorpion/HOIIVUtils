package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.script.*

import scala.collection.mutable

//trait BlockEffect(private val structuredBlock: StructuredPDX) extends Effect with Cloneable {
trait BlockEffect extends Effect {
  _supportedScopes = Set(ScopeType.country) // Example scope types
  _supportedTargets = Set(ScopeType.state) // Example target types

  //override def isScope: Boolean = true

  //override protected def childScripts: mutable.Iterable[? <: PDXScript[?]]

  override def clone(): AnyRef = {
    super.clone().asInstanceOf[BlockEffect]
  }
}