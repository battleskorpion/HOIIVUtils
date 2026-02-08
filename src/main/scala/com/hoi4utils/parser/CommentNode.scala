package com.hoi4utils.parser

class CommentNode
(
  /** Tokens that occurred before the "core" of this node. */
  var leadingTrivia: Seq[Token] = Seq.empty,
  /** The node's value. This may be a literal (String, Int, Double, Boolean),
   a list (block) of child nodes, or a Comment. */
  var rawValue: Comment,
  /** Tokens that came after the node's core. */
  var trailingTrivia: Seq[Token] = Seq.empty
):

  def this(value: Comment) =
    this(rawValue = value)
  
  override def asString: String =
    rawValue.toString

  def clear(): Unit =
    rawValue = Comment.empty