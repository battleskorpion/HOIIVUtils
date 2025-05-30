package com.hoi4utils.gfx

import com.hoi4utils.HOIIVUtils

import java.io.File


/**
 * This is the SpriteType file.
 * @param _name ex: "GFX_focus_SVA_virginia_officers"
 * @param _textureFile ex: "gfx/interface/goals/focus_SVA_virginia_officers.dds"
 */
class SpriteType(private[gfx] var _name: String, private[gfx] var _textureFile: File, basepath: File) {
  /**
   *
   * @param name
   * @param texturefile abstract file name for texture location of gfx
   * @param basepath  basepath for texturefile (mod or base game)
   */
  def this(name: String, texturefile: String, basepath: File) = {
    this(name, new File(texturefile), basepath)
  }

  /**
   * @return texturefile for this gfx
   */
  def textureFile: File = _textureFile

  /**
   * Returns absolute path texturefile for the gfx.
   * "Absolute" in this context refers to mod directory + relative path of the texturefile as parsed
   *
   * @return absolute path texturefile for the gfx
   */
  def textureFileAbsolutePath = new File(HOIIVUtils.get("mod.path") + "\\" + _textureFile.getPath)

  /**
   * Returns the absolute filepath of the texturefile for this spriteType/gfx.
   * "Absolute" in this context refers to mod directory + relative path of the texturefile as parsed.
   *
   * @return absolute filepath of texturefile for this gfx
   */
  def gfx: String = basepath.getPath + "\\" + _textureFile.getPath

  def name: String = _name
}
