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

/**
 * A package-private internal class in the hierarchy that allows us to have both the Texture1/2/3D implementations inheriting from this,
 * but also the abstract ColorCubemapBuilderBase, allowing ColorCubemapBuilderBase to override methods when needed
 * but preventing implementation classes from overriding methods.
 * @param <ContextType>
 * @param <TextureType>
 */
abstract class ColorTextureBuilderBaseInternal<ContextType extends Context<ContextType>, TextureType extends Texture<ContextType>>
    extends TextureBuilderBase<ContextType, TextureType> implements ColorTextureBuilder<ContextType, TextureType>
{
    private ColorFormat internalColorFormat = ColorFormat.RGBA8;
    private CompressionFormat internalCompressionFormat;

    protected final ColorFormat getInternalColorFormat()
    {
        return internalColorFormat;
    }

    protected final CompressionFormat getInternalCompressionFormat()
    {
        return internalCompressionFormat;
    }

    protected final boolean isInternalFormatCompressed()
    {
        return internalCompressionFormat != null;
    }

    ColorTextureBuilderBaseInternal(ContextType context)
    {
        super(context);
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public ColorTextureBuilder<ContextType, TextureType> setInternalFormat(ColorFormat format)
    {
        internalColorFormat = format;
        internalCompressionFormat = null;
        return this;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public ColorTextureBuilder<ContextType, TextureType> setInternalFormat(CompressionFormat format)
    {
        internalColorFormat = null;
        internalCompressionFormat = format;
        return this;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public ColorTextureBuilder<ContextType, TextureType> setMultisamples(int samples, boolean fixedSampleLocations)
    {
        super.setMultisamples(samples, fixedSampleLocations);
        return this;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public ColorTextureBuilder<ContextType, TextureType> setMipmapsEnabled(boolean enabled)
    {
        super.setMipmapsEnabled(enabled);
        return this;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public ColorTextureBuilder<ContextType, TextureType> setMaxMipmapLevel(int maxMipmapLevel)
    {
        super.setMaxMipmapLevel(maxMipmapLevel);
        return this;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public ColorTextureBuilder<ContextType, TextureType> setLinearFilteringEnabled(boolean enabled)
    {
        super.setLinearFilteringEnabled(enabled);
        return this;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public ColorTextureBuilder<ContextType, TextureType> setMaxAnisotropy(float maxAnisotropy)
    {
        super.setMaxAnisotropy(maxAnisotropy);
        return this;
    }
}
