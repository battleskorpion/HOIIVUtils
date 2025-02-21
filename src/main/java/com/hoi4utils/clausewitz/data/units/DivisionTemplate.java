package com.hoi4utils.clausewitz.data.units;

import java.util.Set;

/**
 * Refer to <url> <a href="https://hoi4.paradoxwikis.com/Division_modding">HOI4 Division Modding Wiki</a> </url>
 * todo
 */
public class DivisionTemplate {
	private final String name;
	//private String division_names_group
	private final Set<Regiment> regiments;        // combat batallions
	private final Set<Regiment> support;          // support companies, can be empty/null set.
	private int priority = 1;                   // = 1 default, 0-2 generally
	private final boolean isLocked = false;                   // refer to wiki
	private final boolean force_allow_recruiting = false;     // refer to wiki
	private final int division_cap = -1;                      // -1 for no cap, refer to wiki

	public DivisionTemplate(String name, String division_names_group, Set<Regiment> regiments, Set<Regiment> support, int priority) {
		this.name = name;
		//this.division_names_group = division_names_group;
		this.regiments = regiments;
		this.support = support;
		this.priority = priority;
	}
}
