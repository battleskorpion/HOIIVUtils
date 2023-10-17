package hoi4utils.clausewitz_data;

public interface Localizable {
	/**
	 * ex: HOI4 focuses have two localizable properties: name, description.
	 * @return
	 */
	default int numLocalizableProperties() {
		return 1;
	}


}
