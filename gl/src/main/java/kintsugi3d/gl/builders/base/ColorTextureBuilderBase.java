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

package kintsugi3d.gl.builders.base;

import kintsugi3d.gl.builders.ColorTextureBuilder;
import kintsugi3d.gl.core.ColorFormat;
import kintsugi3d.gl.core.CompressionFormat;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.Texture;

public abstract class ColorTextureBuilderBase<ContextType extends Context<ContextType>, TextureType extends Texture<ContextType>>
    extends ColorTextureBuilderBaseInternal<ContextType, TextureType>
{
    protected ColorTextureBuilderBase(ContextType context)
    {
        super(context);
    }

    @Override
    public final ColorTextureBuilder<ContextType, TextureType> setInternalFormat(ColorFormat format)
    {
        return super.setInternalFormat(format);
    }

    @Override
    public final ColorTextureBuilder<ContextType, TextureType> setInternalFormat(CompressionFormat format)
    {
        return super.setInternalFormat(format);
    }

    @Override
    public final ColorTextureBuilder<ContextType, TextureType> setMultisamples(int samples, boolean fixedSampleLocations)
    {
        return super.setMultisamples(samples, fixedSampleLocations);
    }

    @Override
    public final ColorTextureBuilder<ContextType, TextureType> setMipmapsEnabled(boolean enabled)
    {
        return super.setMipmapsEnabled(enabled);
    }

    @Override
    public final ColorTextureBuilder<ContextType, TextureType> setMaxMipmapLevel(int maxMipmapLevel)
    {
        return super.setMaxMipmapLevel(maxMipmapLevel);
    }

    @Override
    public final ColorTextureBuilder<ContextType, TextureType> setLinearFilteringEnabled(boolean enabled)
    {
        return super.setLinearFilteringEnabled(enabled);
    }

    @Override
    public final ColorTextureBuilder<ContextType, TextureType> setMaxAnisotropy(float maxAnisotropy)
    {
        return super.setMaxAnisotropy(maxAnisotropy);
    }
}
