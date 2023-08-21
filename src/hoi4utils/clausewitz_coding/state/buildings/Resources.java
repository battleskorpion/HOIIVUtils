package hoi4utils.clausewitz_coding.state.buildings;
/*
 * Resources File
 */
public record Resources(int aluminum, int chromium, int oil, int rubber, int steel, int tungsten) {
	public Resources() {
		this(0, 0, 0, 0, 0, 0);
	}
}
