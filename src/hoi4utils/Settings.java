package hoi4utils;

import java.io.IOException;

public enum Settings {
	MOD_PATH {
		public String toString() {
			return (String) getSetting();
		}
	},
	CURRENT_MOD,        // todo not in use
	CIVILIAN_MILITARY_FACTORY_MAX_RATIO,            // ratio for civ/mil factories highlight in buildings view
	SKIP_SETTINGS {
		public Object getSetting() {
			return SettingsManager.settingValues.get(this).equals("true");
		}

		public String defaultProperty() {
			return "false";
		}
	},
	DARK_MODE {
		public Object getSetting() {
			return SettingsManager.settingValues.get(this).equals("true");
		}

		public String defaultProperty() {
			return "false";
		}
	},
	DEV_MODE {
		public Object getSetting() {
			return SettingsManager.settingValues.get(this).equals("true");
		}

		public String defaultProperty() {
			return "false";
		}
	},
	OPEN_CONSOLE_ON_LAUNCH {
		public Object getSetting() {
			return SettingsManager.settingValues.get(this).equals("true");
		}

		public String defaultProperty() {
			return "false";
		}
	},
	PREFERRED_SCREEN {
		public Object getSetting() {
			try {
				return Integer.parseInt(SettingsManager.settingValues.get(this));
			} catch (NumberFormatException exc) {
				if (DEV_MODE.enabled()) {
					System.err.print(exc);
				}
				return 0;
			}
		}

		public String defaultProperty() {
			return "0";
		}
	},
	LOAD_TO_MENU {
		// to skip settings (if not first time user) and load direct to main menu
		public Object getSetting() {
			return SettingsManager.settingValues.get(this).equals("true");
		}

		public String defaultProperty() {
			return "true";
		}
	},
	HOI4_PATH {
		public String toString() {
			return (String) getSetting();
		}
	},
	;

	/**
	 * Returns if setting is enabled (setting is true).
	 *
	 * @return true if the setting's property is equal to true, false otherwise.
	 */
	public boolean enabled() {
		return SettingsManager.settingValues.get(this).equals("true");
	}

	/**
	 * Sets the value of the setting
	 *
	 * @param value
	 */
	void setValue(Object value) {
		SettingsManager.settingValues.put(this, String.valueOf(value));
		try {
			SettingsManager.saveSettings();
			if ((boolean) DEV_MODE.getSetting()) {
				System.out.println("Updated setting " + this.name() + ": " + SettingsManager.settingValues.get(this));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return;
	}

	public String defaultProperty() {
		return "null";
	}

	public Object getSetting() {
		return SettingsManager.settingValues.get(this);
	}

	public String toString() {
		return null;
	}
}
