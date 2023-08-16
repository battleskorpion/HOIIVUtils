package clausewitz_coding.gfx;

import clausewitz_coding.HOI4Fixes;
import settings.LocalizerSettings;

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
        this(name, new File(texturefile));
    }

    /**
     * @return texturefile for this gfx
     */
    public File getTexturefile() {
        return texturefile;
    }

    /**
     * Returns absolute path texturefile for the gfx.
     * "Absolute" in this context refers to mod directory + relative path of the texturefile as parsed
     * @return absolute path texturefile for the gfx
     */
    public File getTexturefileAbsolute() {
        return new File(LocalizerSettings.get(MOD_DIRECTORY) + "\\" + texturefile.getPath());
    }

    /**
     * Returns the absolute filepath of the texturefile for this spriteType/gfx.
     * "Absolute" in this context refers to mod directory + relative path of the texturefile as parsed.
     * @return absolute filepath of texturefile for this gfx
     */
    public String getGFX() {
        return LocalizerSettings.get(MOD_DIRECTORY) + "\\" + texturefile.getPath();
    }

    public String getName() {
            return name;
    }
}