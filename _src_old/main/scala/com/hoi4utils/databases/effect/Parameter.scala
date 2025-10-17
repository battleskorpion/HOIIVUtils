package com.hoi4utils.databases.effect

import com.hoi4utils.exceptions.NullParameterTypeException

import scala.collection.mutable

object Parameter {
  private val allParameters = mutable.HashMap.empty[String, Parameter]

  /** 
   * Create or register a parameter that allows exactly these types. 
   **/
  def apply(name: String, allowedTypes: List[ParameterValueType]): Parameter =
    new Parameter(name, allowedTypes)

  /** 
   * Create or register a parameter that allows a single type. 
   **/
  def apply(name: String, allowedType: ParameterValueType): Parameter =
    new Parameter(name, List(allowedType))

  /** 
   * Create or register a parameter with no explicit allowed types.
   */
  def apply(name: String): Parameter =
    new Parameter(name, Nil)

  /** 
   * Clone an existing parameter by name, clearing its runtime value.
   */
  private def getClone(name: String): Option[Parameter] =
    allParameters.get(name).map(_.cloneParam())

  /** 
   * Returns true if any of the given parameters accepts `Scope` as a value type. 
   */
  def containsScopeParameter(params: List[Parameter]): Boolean =
    params.exists(_.allowedParameterValueTypes.contains(ParameterValueType.scope))

  // Internal wrapper for the value you assign at runtime
  private case class ParameterValue(value: AnyRef) {
    override def toString: String =
      value match {
        case null        => "[null parameter value]"
        case l: List[_]  => "[parameter -> list]"
        case other       => other.toString
      }
  }
}

class Parameter private (val identifier: String, var allowedParameterValueTypes: List[ParameterValueType]) 
  extends EffectParameter with Cloneable {

  import Parameter.ParameterValue

  // register in the global map
  if (identifier == null)
    throw NullParameterTypeException("Parameter identifier cannot be null")
  Parameter.allParameters.put(identifier, this)

  private var runtimeValue: Option[ParameterValue] = None

  /** 
   * Set a runtime value for this parameter (e.g. when you call `of(name, value)`). 
   */
  def withValue(v: AnyRef): this.type = {
    runtimeValue = Some(ParameterValue(v))
    this
  }

  /** 
   * Clone this parameter (clearing any runtime value). 
   */
  def cloneParam(): Parameter = {
    val c = this.clone().asInstanceOf[Parameter]
    c.runtimeValue = None
    c
  }

  @throws[CloneNotSupportedException]
  override protected def clone(): AnyRef =
    super.clone()

  /** 
   * Render as PDX script: "id = value" or just the value if no id 
   */
  def displayScript: String =
    if (identifier.nonEmpty) s"$identifier = ${runtimeValue.map(_.toString).getOrElse("")}"
    else runtimeValue.map(_.toString).getOrElse("")

  override def toString: String = displayScript
}
