package main.java.com.hoi4utils.clauzewitz.map.province;

import main.java.com.hoi4utils.clauzewitz.localization.Localizable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * localizable: victory point name
 */
public class VictoryPoint implements Localizable {
	static List<VictoryPoint> victoryPoints = new ArrayList<>();
	int value;
	Province province;

	protected VictoryPoint(int province, int value) {
		this.province = Province.of(province);
		this.value = value;
	}

	public static VictoryPoint of(int province, int value) {
		for (VictoryPoint vp : victoryPoints) {
			if (vp.province.id == province) {
				return vp;
			}
		}
		return new VictoryPoint(province, value);
	}

	public int value() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public @NotNull Map<Property, String> getLocalizableProperties() {
		return Map.of(Property.NAME, "VP_NAME");
	}

	@Override
	public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
		return victoryPoints;
	}
}
