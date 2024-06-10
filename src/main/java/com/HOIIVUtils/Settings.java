package com.HOIIVUtils;

import com.HOIIVUtils.clauzewitz.exceptions.SettingsWriteException;

public enum Settings {
	MOD_PATH {
		@Override
		public String toString() {
			return (String) getSetting();
		}
	},
	HOI4_PATH {
		@Override
		public String toString() {
			return (String) getSetting();
		}
	},
	CURRENT_MOD, // todo not in use
	CIVILIAN_MILITARY_FACTORY_MAX_RATIO, // ratio for civ/mil factories highlight in buildings view
	SKIP_SETTINGS {
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return DEFAULT_VALUE;
		}
	},
	DARK_MODE {
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return DEFAULT_VALUE;
		}
	},
	DEV_MODE {
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return DEFAULT_VALUE;
		}
	},
	DEMO_MODE {
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return DEFAULT_VALUE;
		}
	},
	OPEN_CONSOLE_ON_LAUNCH {
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return DEFAULT_VALUE;
		}
	},
	PREFERRED_SCREEN {
		@Override
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

		@Override
		public String defaultProperty() {
			return "0";
		}
	},
	LOAD_TO_MENU {
		// to skip settings (if not first time user) and load direct to main menu
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return "true";
		}
	},

	ATTEMPT_LOAD_LOCALIZATION {
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return "true";
		}
	},
	DRAW_FOCUS_TREE {
		@Override
		public Object getSetting() {
			return enabled();
		}

		@Override
		public String defaultProperty() {
			return DEFAULT_VALUE;
		}
	},;

	private static final String DEFAULT_VALUE = "false";

	/**
	 * Returns if setting is enabled (setting is true).
	 *
	 * @return true if the setting's property is equal to true, false otherwise.
	 */
	public boolean enabled() {
		return SettingsManager.settingValues.get(this).equals("true");
	}

	/**
	 * Returns if setting is disabled (setting is false).
	 *
	 * @return true if the setting's property is equal to false, false otherwise.
	 */
	public boolean disabled() {
		return !enabled();
	}

	/**
	 * Sets the value of the setting
	 *
	 * @param value
	 * @throws SettingsWriteException
	 */
	void setValue(Object value) {
		SettingsManager.settingValues.put(this, String.valueOf(value));
		SettingsManager.writeSettings();
		System.out.println("Updated setting " + this.name() + ": " + SettingsManager.settingValues.get(this));
	}

	public String defaultProperty() {
		return "null";
	}

	public Object getSetting() {
		return SettingsManager.settingValues.get(this);
	}

	@Override
	public String toString() {
		return "";
	}
}
