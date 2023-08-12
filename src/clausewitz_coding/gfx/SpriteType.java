package clausewitz_coding.gfx;

import clausewitz_coding.HOI4Fixes;

import java.io.File;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class SpriteType {
    String name;            // ex: "GFX_focus_SVA_virginia_officers"
    File texturefile;       // ex: "gfx/interface/goals/focus_SVA_virginia_officers.dds"

    public SpriteType(String name, File texturefile) {
        this.name = name;
        this.texturefile = texturefile;
    }

    /**
     *
     * @param name
     * @param texturefile abstract file name for texture location of gfx
     */
    public SpriteType(String name, String texturefile) {
        this(name, new File(HOI4Fixes.settings.get(MOD_DIRECTORY) + "\\" + texturefile));
    }

    /**
     * @return texturefile for this gfx
     */
    public File getFile() {
        return texturefile;
    }

    /**
     * @return absolute filepath of texturefile for this gfx
     */
    public String getGFX() {
        return texturefile.getPath();
    }

}
