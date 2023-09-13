package hoi4utils;

public class FirstTime {
	Boolean isFirstTime = true;

	/**
	 * this is to make sure that when the user deletes his settings it is not perma stuck on false
	 */
	public Boolean getIsFirstTime() {
		if (HOIIVUtilsProp.configFile.exists()) {
			isFirstTime = false;
		} else {
			isFirstTime = true;
		}
		return isFirstTime;
	}

	public void setIsFirstTime(Boolean isFirstTime) {
		this.isFirstTime = isFirstTime;
	}
}
