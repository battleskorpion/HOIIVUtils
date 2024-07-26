package com.hoi4utils.clausewitz.map.resources

import java.util
import java.util.{Arrays, HashSet, Set}


/*
 * Resources File
 */
class Resources {
  //todo extens collection or smthing maybe for iteration?
  private var resources: util.Set[Resource] = _ // now we only need to store resources with nonzero quantities, if desired, otherwise 0 can be implied.

  def this(aluminum: Int, chromium: Int, oil: Int, rubber: Int, steel: Int, tungsten: Int) = {
    this()
    resources = new util.HashSet[Resource](6)
    resources.add(new Resource("aluminum", aluminum))
    resources.add(new Resource("chromium", chromium))
    resources.add(new Resource("oil", oil))
    resources.add(new Resource("rubber", rubber))
    resources.add(new Resource("steel", steel))
    resources.add(new Resource("tungsten", tungsten))
  }

  def this() = {
    this(0, 0, 0, 0, 0, 0)
  }

//  def this(resourceAmts: Int*): Unit

  def this(resources: util.Set[Resource]) = {
    this()
    this.resources = resources
  }

  def this(resources: Resource*) = {
    this()
    this.resources = new util.HashSet[Resource](6) // typically there are 6 resources

    this.resources.addAll(util.Arrays.asList(resources))
  }

  def add(addtl: Resources): Unit = {
    // Iterate addtl and update the current resources
    import scala.jdk.CollectionConverters
    for (r <- addtl.resources) {
      if (containsResource(r)) {
        // Resource with the same identifier exists, update amt
        val existingResource = get(r.identifier)
        existingResource.setAmt(existingResource.amt + r.amt)
      }
      else {
        // Resource doesn't exist, add it
        resources.add(new Resource(r))
      }
    }
  }

  private def containsResource(resource: Resource): Boolean = {
    import scala.jdk.CollectionConverters
    for (r <- resources) {
      if (r.sameResource(resource)) return true
    }
    false
  }

  private def containsResource(identifier: String): Boolean = {
    import scala.jdk.CollectionConverters
    for (r <- resources) {
      if (r.sameResource(identifier)) return true
    }
    false
  }

  /**
   * The number of unique resources is the number of nonzero resource types being represented, withholding any resources
   * that are of zero quantity for special reasons.
   *
   * @return the number of unique resources
   */
  private def numUniqueResources = resources.size

  def get(identifier: String): Resource = {
    // todo check if valid identifier
    // if resource exists with a quantity,
    // it will be returned. If not, 0 will be returned for zero quantity.
    import scala.jdk.CollectionConverters
    for (r <- resources) {
      if (r.sameResource(identifier)) return r
    }
    new Resource(identifier, 0)
  }
}
