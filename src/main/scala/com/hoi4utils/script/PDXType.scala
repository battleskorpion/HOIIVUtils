package com.hoi4utils.script

trait PDXType[T <: PDXScript[?]](var pdxTypeIdentifier: String, pdxTypeCollectionSupplier: () => Iterable[T]) {
  def isValidPDXTypeIdentifier(identifier: String): Boolean = {
    pdxTypeCollectionSupplier().exists(_.isValidID(identifier))
  }
}