package com.hoi4utils.script

import scala.annotation.StaticAnnotation
final class ChildPDX(val order: Int = 0) extends StaticAnnotation

import scala.quoted.*
import scala.collection.mutable

object ChildPDXDerive:
	inline def from[T](self: T): mutable.Iterable[? <: PDXScript[?]] =
		${ impl[T]('self) }

	private def impl[T: Type](self: Expr[T])(using Quotes): Expr[mutable.Iterable[? <: PDXScript[?]]] =
		import quotes.reflect.*

		val pdxTpe = TypeRepr.of[PDXScript[?]]
		val tpe = TypeRepr.of[T]
		val sym = tpe.typeSymbol

		// find vals: annotated with @ChildPDX and <: PDXScript[_]
		val fields =
			sym.declaredFields.flatMap {
				case f if f.isValDef =>
					val hasAnno = f.annotations.exists(_.tpe =:= TypeRepr.of[ChildPDX])
					val isPDX = f.tree match
						case v: ValDef => v.tpt.tpe <:< pdxTpe
						case _ => false
					if hasAnno && isPDX then Some(f) else None
				case _ => None
			}

		// stable order: by annotation 'order', then by source order
		def orderOf(f: Symbol): Int =
			f.annotations.collectFirst {
				case ann if ann.tpe =:= TypeRepr.of[ChildPDX] =>
					ann match
						case Apply(_, List(Literal(IntConstant(n)))) => n
						case _ => 0
			}.getOrElse(0)

		val sorted = fields.sortBy(f => (orderOf(f), f.pos.map(_.start).getOrElse(Int.MaxValue)))

		val selects: List[Term] = sorted.map(f => Select.unique(self.asTerm, f.name))
		val elems = Varargs(selects.map(_.asExprOf[PDXScript[?]]))
		val listBufExpr: Expr[mutable.Iterable[? <: PDXScript[?]]] =
			'{ _root_.scala.collection.mutable.ListBuffer[PDXScript[?]]($elems *) }
			
		listBufExpr
