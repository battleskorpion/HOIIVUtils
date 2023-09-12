package hoi4utils.clausewitz_coding.state.resources;

public class Resource {
	private static String[] resourceIdentifiers = {"aluminum", "chromium", "oil", "rubber", "steel", "tungsten"};    // default: aluminum, chromium, oil, rubber, steel, tungsten todo load in resources if modified.
	private static int identifierCounter = 0;
	private final String identifier;
	private int amt;                // quantity of resource being represented
	
	public Resource(String resource) {
		this(resource, 0);
	}

	public Resource(String identifier, int amt) {
		// todo if identifier not valid thats not good!
		this.identifier = identifier;
		this.amt = amt;
	}

	/**
	 * use this constructor when creating new resource objects for every resource in order,
	 * the identifiers (resource types) will be set automatically according to the order of the identifiers.
	 * @param amt quantity of resource
	 */
	public Resource(int amt) {
		this(resourceIdentifiers[identifierCounter], amt);
		identifierCounter++;
		identifierCounter %= numResourceIdentifiers();
	}

	public Resource(Resource r) {
		this(r.identifier, r.amt);
	}

	private static void setResourceIdentifiers(String[] identifiers) {
		Resource.resourceIdentifiers = identifiers;
	}

	public boolean sameResource(Resource resource) {
		return this.identifier.equals(resource.identifier);
	}
	public boolean sameResource(String identifier) {
		return this.identifier.equals(identifier);
	}

	public String identifier() {
		return identifier;
	}

	public static int numResourceIdentifiers() {
		return resourceIdentifiers.length;
	}

	public int amt() {
		return amt;
	}

	public void setAmt(int amt) {
		this.amt = amt;
	}
}