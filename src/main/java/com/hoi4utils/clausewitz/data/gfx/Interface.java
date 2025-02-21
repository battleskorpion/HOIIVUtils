package com.hoi4utils.clausewitz.data.gfx;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz_parser.ParserException;

import com.hoi4utils.clausewitz_parser.Parser;
import com.hoi4utils.clausewitz_parser.Node;
import scala.jdk.javaapi.CollectionConverters;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Represents a .gfx file in interface folder
 * Contains a set of SpriteTypes
 */
public class Interface {
	private final File file;
	private HashSet<SpriteType> spriteTypes;
	private static final HashMap<String, SpriteType> gfxHashMap = new HashMap<>();
	private static HashMap<File, Interface> interfaceFiles;

	/*
	read all in mod\nadivided-dev\interface
	format example:
	spriteTypes = {

		SpriteType = {
			name = "GFX_focus_SVA_virginia_officers"
			texturefile = "gfx/interface/goals/focus_SVA_virginia_officers.dds"
		}
		SpriteType = {
			name = "GFX_focus_generic_chromium"
			texturefile = "gfx/interface/goals/focus_generic_chromium.dds"
		}
		SpriteType = {
			name = "GFX_focus_generic_manpower"
			texturefile = "gfx/interface/goals/focus_generic_manpower.dds"
		}
	}
	 */

	public Interface(File file) {
		this.file = file;

		readGFXFile(file);
	}

	public static String getGFX(String icon) {
		SpriteType spriteType = getSpriteType(icon);
		if (spriteType == null) {
			return null;
		}
		return spriteType.getGFX();
	}

	public static SpriteType getSpriteType(String icon) {
		return gfxHashMap.get(icon);
	}

	public static int numGFX() {
		return gfxHashMap.size();
	}

	/**
	 * lists all SpriteType objects found in interface files
	 * @return
	 */
	public static SpriteType[] listGFX() {
		return gfxHashMap.values().toArray(new SpriteType[]{});
	}

	/**
	 * lists .gfx files which Interface class represents and which
	 * have been read in.
	 * @return
	 */
	public static Interface[] list() {
		return interfaceFiles.values().toArray(new Interface[]{});
	}

	/**
	 * Returns the number of .gfx interface files read
	 * @return number of interface files read
	 */
	public static int numFiles() {
		return interfaceFiles.size();
	}

	/**
	 * returns all .gfx files parsed
	 * @return list of all .gfx files of type Interface
	 */
	public static Interface[] listGFXFiles() {
		return interfaceFiles.values().toArray(new Interface[]{});
	}

	private void readGFXFile(File file) {
		spriteTypes = new HashSet<>();

		// read listed sprites

		Parser interfaceParser = new Parser(file);

		//Expression[] exps = interfaceParser.findAll("SpriteType={", false);
		Node rootNode = null;
		try {
			rootNode = interfaceParser.parse();
		} catch (ParserException e) {
			throw new RuntimeException(e);
		}
		List<Node> nodes = CollectionConverters.asJava(rootNode.filter("SpriteType={").toList());
		if (nodes == null) {
			System.err.println("No SpriteTypes in interface .gfx file, " + file);
			//System.out.println(interfaceParser.expression());
			return;
		}

		for (Node exp : nodes) {
			if (!exp.contains("name=")) {
				continue;
			}
			Node nameExp = exp.find("name=").getOrElse(null);
			if (!exp.contains("texturefile=")) {
				continue;
			}
			Node fileExp = exp.find("texturefile=").getOrElse(null);

			String name = nameExp.$stringOrElse("");
			name = name.replaceAll("\"", "");		// get rid of quotes from clausewitz code for file pathname
			if (name.isEmpty()) continue;
			String filename = fileExp.$stringOrElse("");
			filename = filename.replaceAll("\"", "" );
			if (filename.isEmpty()) continue;

			SpriteType gfx = new SpriteType(name, filename);
			spriteTypes.add(gfx);
			gfxHashMap.put(name, gfx);
		}
	}

	public static void loadGFXFiles() {
		File dir = new File(HOIIVUtils.get("mod.path") + "\\interface");
		if (!dir.exists() || !dir.isDirectory()) {
			System.err.println("interface directory does not exist");
		}
		if (Objects.requireNonNull(dir.listFiles()).length == 0) {
			System.err.println("interface directory is empty");
		}
		interfaceFiles = new HashMap<>();

		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (!file.getName().endsWith(".gfx")) {
				continue;
			}
			interfaceFiles.put(file, new Interface(file));
		}
	}

	/**
	 * Returns the name of the file represented by an instance of the Interface class
	 * @return name of this Interface file
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * Returns the path for the file represented by an instance of the Interface class
	 * @return filepath for this Interface file
	 */
	public String getPath() {
		return file.getPath();
	}

}
