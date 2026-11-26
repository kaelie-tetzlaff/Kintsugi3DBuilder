/*
 * Copyright (c) 2019 - 2026 Seth Berrier, Kaelie Tetzlaff, Jacob Buelow, Luke Denney, Ian Anderson, Zoe Cuthrell, Blane Suess, Isaac Tesch,
 *                           Nathaniel Willius, Atlas Collins, Simon Cao, Joe Luther, Jakob Schmucki, Nathan Sunday, Joseph O'Connor
 * Copyright (c) 2019 The Regents of the University of Minnesota
 *
 * Licensed under GPLv3
 * ( http://www.gnu.org/licenses/gpl-3.0.html )
 *
 * This code is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package kintsugi3d.builder.resources.project.texture;

import kintsugi3d.builder.core.texture.NamedTextureInfo;
import kintsugi3d.builder.core.texture.StandardTexture;
import kintsugi3d.builder.core.texture.TextureInfo;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.ReadonlyTexture2D;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FunctionalInterface
public interface ReadonlyTextureSet<ContextType extends Context<ContextType>>
{
    Map<TextureInfo, ? extends ReadonlyTexture2D<ContextType>> getTextures();

    /**
     * Returns a map containing only the standard textures
     * @return
     */
    default @NotNull Map<StandardTexture, ? extends ReadonlyTexture2D<ContextType>> getStandardTextures()
    {
        return StandardTexture.convertObjectMapToEnumMap(getTextures());
    }

    default ReadonlyTexture2D<ContextType> getTexture(String texName)
    {
        return getTextures().get(new NamedTextureInfo(texName));
    }

    default ReadonlyTexture2D<ContextType> getTexture(TextureInfo tex)
    {
        return getTextures().get(tex);
    }

    default ReadonlyTexture2D<ContextType> getTexture(@NotNull StandardTexture tex)
    {
        return getTextures().get(tex.getDetails());
    }
}
