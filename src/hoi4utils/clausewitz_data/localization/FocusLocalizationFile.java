package hoi4utils.clausewitz_data.localization;

import ui.FXWindow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/* todo strange bug
	SAC_purity_through_unity:0 "Purity through Unity"
			SAC_purity_through_unity_desc:0 ""

			SAC_bomber_focus:0 "Bomber Focus"
			SAC_bomber_focus_desc:0 ""

			SAC_nondiscriminate_recruitment:0 "Nondiscriminate Recruitment"
			SAC_nondiscriminate_recruitment_desc:0 ""

			SAC_push_social_reform:0 "Push Social Reform"
			SAC_push_social_reform_desc:0 ""

			SAC_take_out_fascists:0 "Take Out Fascists"
			SAC_extra_tech_slot_2:0 "Extra Tech Slot 2"
			SAC_extra_tech_slot_desc:0 ""

			SAC_extra_tech_slot_2_desc:0 ""

			SAC_nuclear_effort:0 "Nuclear Effort"
			SAC_nuclear_effort_desc:0 ""

			SAC_forign_intervention:0 "Forign Intervention"
			SAC_forign_intervention_desc:0 ""

			SAC_infrastructure_effort_2:0 "Infrastructure Effort 2"
			SAC_infrastructure_effort_2_desc:0 ""

			SAC_equipment_effort_2:0 "Equipment Effort 2"
			SAC_equipment_effort_2_desc:0 ""

			SAC_call_workers_to_arms:0 "Call Workers to Arms"
			SAC_call_workers_to_arms_desc:0 ""

			SAC_aviation_effort:0 "Aviation Effort"
			SAC_capital_ships_effort:0 "Capital Ships Effort"
			SAC_capital_ships_effort_desc:0 ""
*/

/**
 * This is the FocusLocalization file.
 * @implNote  git/.yml prefers/requires "    " instead of using \t and "\n" for newline/line separator.
 */
public class FocusLocalizationFile extends LocalizationFile {

	protected List<List<Localization>> focusLocalizationList;

	public FocusLocalizationFile(File file) throws IllegalArgumentException {
		super(file);

		focusLocalizationList = new ArrayList<>();
	}


	public String toString() {
		if (super.isFile()) {
			return super.toString();
		}
		return super.toString();
	}

	@Override
	public void readLocalization() {
		focusLocalizationList.clear();
		super.readLocalization();

		//System.err.println("test, " + localizationList.toString());
		List<Localization> tempLocList = new ArrayList<>();
		HashMap<String, Localization> tempLocDescList = new HashMap<>();
		for (Localization localization : localizationList) {
			if (localization.ID().endsWith("_desc")) {
				tempLocDescList.put(localization.ID(), localization);
			} else {
				tempLocList.add(localization);
			}
		}

		for (Localization localization : tempLocList) {
			ArrayList<Localization> list = new ArrayList<>();
			list.add(localization);
			Localization descLoc = tempLocDescList.get(localization.ID() + "_desc");
			if (descLoc == null) {
				descLoc = new Localization(localization.ID() + "_desc", "");
			}
			list.add(descLoc);
			focusLocalizationList.add(list);
		}
	}

	@Override
	public void writeLocalization() {
		/* load file data, before writer init so data not disappeared */
		StringBuilder fileBuffer;
		try {
			Scanner scanner = new Scanner(this);
			fileBuffer = new StringBuilder();
			while (scanner.hasNextLine()) {
				fileBuffer.append(scanner.nextLine()).append("\n");
			}
			scanner.close();
		} catch (Exception exception) {
			FXWindow.openGlobalErrorWindow(exception);
			return;
		}

		try {
//	  System.out.println(fileBuffer);

			FileWriter writer = new FileWriter(this, false);     // true = append
			BufferedWriter BWriter = new BufferedWriter(writer);
			PrintWriter PWriter = new PrintWriter(BWriter);			        // for println syntax

//	  PWriter.println(language);

			for (List<Localization> localization : focusLocalizationList) {
				int idpos;      // where to look for line separator for adding desc.

				if (localization.get(0).status() == Localization.Status.UPDATED) {
					/* replace loc */
					int start = fileBuffer.indexOf(localization.get(0).ID());
					if (start < 0) {
						System.err.println("Start of localization id is negative!");
					}
					int temp = fileBuffer.indexOf("\"", start);
					int end = 1;
					// end char must be literally " and not \"
					do {
						end = fileBuffer.indexOf("\"", temp + 1);
					} while (fileBuffer.charAt(end - 1) == '\\');
					if (end < 0) {
						System.err.println("end of localization id is negative!");
					}

					String loc = localization.get(0).toString();
					loc = loc.replaceAll("§", "Â§");		// necessary with UTF-8 BOM
					fileBuffer.replace(start, end + 1, loc);
					System.out.println("replaced " + localization.get(0).ID());

					idpos = start + loc.length();
					idpos = fileBuffer.indexOf("\n", idpos);
				} else if (localization.get(0).status() == Localization.Status.NEW) {
					/* append loc */
					String loc = localization.get(0).toString();
					loc = loc.replaceAll("§", "Â§");		// necessary with UTF-8 BOM
					fileBuffer.append("    ").append(loc).append("\n");
					System.out.println("append " + localization.get(0).ID());

					idpos = fileBuffer.length();
//					idpos = fileBuffer.indexOf("\n", idpos);
				} else {
					//idpos = 0;
					Localization loc = localization.get(0);
					idpos = fileBuffer.indexOf(loc.ID());
					if (idpos == -1) {
						idpos = fileBuffer.length();
					} else {
						idpos += loc.toString().length();
						idpos = fileBuffer.indexOf("\n", idpos);
					}
				}

				/* fix null description, shouldn't really be at this point but hey that's fine */
				if (localization.size() < 2) {
					System.out.println("localization description was missing for: " + localization.get(0) + ". Proceeding to write default localization.");
					localization.add(new Localization(localization.get(0).ID() + "_desc",
							"[Localization desc was missing in HOI4Utils] "  + LocalDate.now(), Localization.Status.NEW));
				}
				if (localization.get(1) == null) {
					System.out.println("localization description was null for: " + localization.get(0) + ". Proceeding to write default localization.");
					localization.set(1, new Localization( localization.get(0).ID() + "_desc",
							"[Localization desc was null in HOI4Utils] "  + LocalDate.now(), Localization.Status.NEW));
				}
				if (localization.get(1).status() == Localization.Status.UPDATED) {
					/* replace loc */
					int start = fileBuffer.indexOf(localization.get(1).ID());
					if (start < 0) {
						//HOIUtilsWindow.openError("Start of localization id is negative, for localization with \"updated\" status");
						continue;         // shouldn't do anything, localization was supposedly updated
					}
					int temp = fileBuffer.indexOf("\"", start);
					int end = 1;
					// end char must be literally " and not \"      // todo. there is a failure in this logic? as far as i can see
					do {
						end = fileBuffer.indexOf("\"", temp + 1);
					} while (fileBuffer.charAt(end - 1) == '\\');
					if (end < 0) {
						FXWindow.openGlobalErrorWindow("End of localization id is negative!");
					}

					String loc = localization.get(1).toString();
					loc = loc.replaceAll("§", "Â§");		// necessary with UTF-8 BOM
					fileBuffer.replace(start, end + 1, loc);
					System.out.println("replaced " + localization.get(1).ID());
				} else if (localization.get(1).status() == Localization.Status.NEW) {
					// todo put localization of desriptions near the focus loc if possible
					/* append loc */
					String loc = localization.get(1).toString();
					loc = loc.replaceAll("§", "Â§");		// necessary with UTF-8 BOM

					if (idpos == 0) {
						fileBuffer.append("    ").append(loc).append("\n");
						System.out.println("append " + localization.get(1).ID());
					} else {
						/* skip new lines */
						while (fileBuffer.indexOf("\n") == idpos) {
							idpos++;
						}
						fileBuffer.insert(idpos, "\n" + "    " + loc + "\n");       // last newline here is an *extra* newline, for creating a blank line
																						// between locs for separate focuses
						System.out.println("insert " + localization.get(1).ID());
					}
				} else {

				}
			}

			/* print */
			PWriter.print(fileBuffer);
			PWriter.close();
		}
		catch (Exception exception) {
			FXWindow.openGlobalErrorWindow(exception);
		}
	}

	public void addLocalization(Localization newLocalization, Localization newLocalizationDesc) {
		super.addLocalization(newLocalization);

		List<Localization> list = new ArrayList<>();
		list.add(newLocalization);
		list.add(newLocalizationDesc);
		focusLocalizationList.add(list);
	}

	/**
	 * Gets localization from this file corresponding to the ID
	 * @param ID
	 * @return Localization corresponding with ID, or null if not found
	 */
	public Localization getLocalization(String ID) {
		if(ID == null) {
			System.err.println("Null localization ID in " + this);
			return null;
		}

		for (Localization loc : localizationList) {
			if (loc.ID().equals(ID)) {
				return loc;
			}
			System.out.println(loc.ID());
		}

		return null;
	}

	public Localization getLocalizationDesc(String ID) {
		if(ID == null) {
			System.err.println("Null localization ID in " + this);
			return null;
		}

		// loop through description localizations
		for (List<Localization> locList : focusLocalizationList) {
			if (locList.get(1).ID().equals(ID)) {
				return locList.get(1);
			}
			System.out.println(locList.get(1).ID());
		}

		return null;
	}

	@Override
	public Localization setLocalization(String key, String text) {
		ArrayList<Localization> listTemp = new ArrayList<>(localizationList);

		for (List<Localization> loc : focusLocalizationList) {
			if (loc.get(0).ID().equals(key)) {
				Localization newLoc = new Localization(key, text, Localization.Status.UPDATED);
				Localization[] newLocList = new Localization[]{newLoc, loc.get(1)};


				listTemp.remove(loc.get(0));	  // record is final
				listTemp.add(newLoc);
				localizationList = listTemp;

				focusLocalizationList.remove(loc);
				focusLocalizationList.add(List.of(newLocList));
				return newLoc;
			}
		}

		Localization newLoc = new Localization(key, text, Localization.Status.NEW);
		Localization newLocDesc = new Localization(key + "_desc", "[null]", Localization.Status.NEW);
		Localization[] newLocList = new Localization[]{newLoc, newLocDesc};

		listTemp.add(newLoc);
		localizationList = listTemp;

		focusLocalizationList.add(List.of(newLocList));

		return newLoc;
	}

	@Override
	public void setLocalization(Localization localization) {
		super.setLocalization(localization);

		int i = 0;
		for (List<Localization> locList : focusLocalizationList) {
			int j = 0;
			for (Localization l : locList) {
				if (l.ID().equals(localization.ID())) {
					focusLocalizationList.get(i).remove(j);
					focusLocalizationList.get(i).add(j, localization);
					return;
				}
				j++;
			}
			i++;
		}

		/* if not exists in focus loc list add it */
		if (localization.ID().endsWith("_desc")) {
			// really should need a focus first, but sure?
			System.out.println("Note: adding \"_desc\" before focus id localization.");
			addLocalization(new Localization(remove_descFromIdentifier(localization), "[New focus localization added by HOI4Utils on " + LocalDate.now() + "]", Localization.Status.DEFAULT), localization);
		} else {
			addLocalization(localization, new Localization(localization.ID() + "_desc", "", Localization.Status.DEFAULT));
		}
	}

	private String remove_descFromIdentifier(Localization localization) {
		return localization.ID().substring(0, localization.ID().length() - 5);
	}

	public Localization setLocalizationDesc(String key, String text) {
		for (List<Localization> loc : focusLocalizationList) {
			if (loc.get(1).ID().equals(key)) {
				focusLocalizationList.remove(loc);
				Localization localization = new Localization(key, text, Localization.Status.UPDATED);
				focusLocalizationList.add(List.of(new Localization[]{loc.get(0), localization}));
				return localization;
			}
		}
		return null;
	}
}
