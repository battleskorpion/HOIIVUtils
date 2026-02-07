package com.hoi4utils.script2

trait PDXEntity:
  // Recursive reflection to find fields in parent classes too
  private lazy val properties: List[PDXProperty[?]] =
    def getFields(cls: Class[?]): List[PDXProperty[?]] =
      val local = cls.getDeclaredFields.toList
        .filter(f => classOf[PDXProperty[?]].isAssignableFrom(f.getType))
        .map { f =>
          f.setAccessible(true)
          f.get(this).asInstanceOf[PDXProperty[?]]
        }
      val parent = Option(cls.getSuperclass).map(getFields).getOrElse(Nil)
      local ++ parent

    getFields(this.getClass)

  def loadPDX(node: SeqNode, file: File): Unit =
    val contextName = this.toString // Usually the Focus ID
    for
      prop <- properties
      valueNode <- Option(node.find(prop.pdxKey))
      // todo handle if was required
    do
      prop.loadFrom(valueNode, contextName)(err => handlePDXError(err))

  def handlePDXError(error: PDXFileError): Unit =
    // Your existing logging logic here
    println(s"[PDX Error] ${error.message}")

  def pdx[T](key: String)(using parser: PDXParser[T]): PDXProperty[T] =
    new PDXProperty[T](key, None)
