package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.Scope
import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.script.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util
import java.util.EnumSet
import java.util.function.Supplier


/**
 * For information: <a href="https://hoi4.paradoxwikis.com/Effect">Effects
 * Wiki</a>
 */
class Effect[T](private var identifier: String, tSupplier: Supplier[PDXScript[T]], structuredBlock: StructuredPDX) extends DynamicPDX[T, StructuredPDX](tSupplier, structuredBlock) with ScopedPDXScript {
  //    protected static final SortedMap<String, Effect<?>> effects = new TreeMap<>();
  //
  private var defintionScope: Scope = _
  private var targetScope: Scope = _
  private var supportedScopes: util.EnumSet[ScopeType] = _
  private var supportedTargets: util.EnumSet[ScopeType] = _

  /**
   * For simple effects that have no block version.
   *
   * @param TSupplier
   */
  def this(identifier: String, tSupplier: Supplier[PDXScript[T]]) = {
    this(identifier, tSupplier, null)
  }

  /**
   * For effects that only have a block definition version.
   *
   * @param structuredBlock
   */
  def this(identifier: String, structuredBlock: StructuredPDX) = {
    this(identifier, null, structuredBlock)
  }

  def getIdentifier: String = identifier

  override def getSupportedScopes: util.EnumSet[ScopeType] = supportedScopes

  def getSupportedTargets: util.EnumSet[ScopeType] = supportedTargets

  def hasSupportedTargets: Boolean = supportedTargets != null && !(supportedTargets.isEmpty)

  def setTarget(target: Scope): Unit = {
    this.targetScope = target
  }

  @throws[Exception]
  def setTarget(string: String, within: Scope): Unit = {
    setTarget(Scope.of(string, within))
  }

  def getDefintionScope: Scope = defintionScope

  def getTargetScope: Scope = targetScope

  def target: String = {
    if (targetScope == null) return "[null target]"
    targetScope.name
  }

  def hasTarget: Boolean = targetScope != null

  override def getDefinitionScope: Scope = this.defintionScope

  //
  //    @Override protected Object clone() throws CloneNotSupportedException {
  //        Effect<?> c = (Effect<?>) super.clone();
  //        c.parameters = new ArrayList<>();
  //        return c;
  //    }
  def isScope: Boolean = false
}