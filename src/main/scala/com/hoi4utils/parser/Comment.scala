package com.hoi4utils.parser

class Comment(private var _comment: String) {
  
  def comment_= (comment: String): Unit = _comment = comment
  
  def comment: String = _comment
  
  override def toString: String = _comment
}
