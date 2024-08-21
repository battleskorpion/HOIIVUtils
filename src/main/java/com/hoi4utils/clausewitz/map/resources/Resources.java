//package com.hoi4utils.clausewitz.map.resources;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
///*
// * Resources File
// */
//public class Resources {        //todo extens collection or smthing maybe for iteration?
//	private Set<Resource> resources;        // now we only need to store resources with nonzero quantities, if desired, otherwise 0 can be implied.
//
//	public Resources() {
//		this(0, 0, 0, 0, 0, 0);
//	}
//
//	public Resources(int aluminum, int chromium, int oil, int rubber, int steel, int tungsten) {
//		resources = new HashSet<>(6);
//		resources.add(new Resource("aluminum", aluminum));
//		resources.add(new Resource("chromium", chromium));
//		resources.add(new Resource("oil", oil));
//		resources.add(new Resource("rubber", rubber));
//		resources.add(new Resource("steel", steel));
//		resources.add(new Resource("tungsten", tungsten));
//	}
//
//	public Resources(int... resourceAmts) {
//		if (resourceAmts.length != Resource.numResourceIdentifiers()) {
//			throw new IllegalArgumentException();
//		}
//
//		this.resources = new HashSet<>(6);          // typically there are 6 resources
//		for (int amt : resourceAmts) {
//			this.resources.add(new Resource(amt));
//		}
//	}
//
//	public Resources(Set<Resource> resources) {
//		this.resources = resources;
//	}
//
//	public Resources(Resource... resources) {
//		this.resources = new HashSet<>(6);          // typically there are 6 resources
//		this.resources.addAll(Arrays.asList(resources));
//	}
//
//	public void add(Resources addtl) {
//		// Iterate addtl and update the current resources
//		for (Resource r : addtl.resources) {
//			if (containsResource(r)) {
//				// Resource with the same identifier exists, update amt
//				Resource existingResource = get(r.identifier());
//				existingResource.setAmt(existingResource.amt() + r.amt());
//			} else {
//				// Resource doesn't exist, add it
//				resources.add(new Resource(r));
//			}
//		}
//	}
//
//	private boolean containsResource(Resource resource) {
//		for (Resource r : resources) {
//			if (r.sameResource(resource)) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	private boolean containsResource(String identifier) {
//		for (Resource r : resources) {
//			if (r.sameResource(identifier)) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	/**
//	 * The number of unique resources is the number of nonzero resource types being represented, withholding any resources
//	 * that are of zero quantity for special reasons.
//	 * @return the number of unique resources
//	 */
//	private int numUniqueResources() {
//		return resources.size();
//	}
//
//
//	public Resource get(String identifier) {
//		// todo check if valid identifier
//
//		// if resource exists with a quantity,
//		// it will be returned. If not, 0 will be returned for zero quantity.
//		for (Resource r : resources) {
//			if (r.sameResource(identifier)) {
//				return r;
//			}
//		}
//
//		return new Resource(identifier, 0);
//	}
//}
