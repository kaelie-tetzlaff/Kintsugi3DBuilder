/*
 * Copyright (c) 2019 - 2026 Seth Berrier, Kaelie Tetzlaff, Jacob Buelow, Luke Denney, Ian Anderson, Zoe Cuthrell, Blane Suess, Isaac Tesch, Nathaniel Willius, Atlas Collins, Simon Cao, Joe Luther, Jakob Schmucki, Nathan Sunday, Joseph O'Connor
 * Copyright (c) 2019 The Regents of the University of Minnesota
 *
 * Licensed under GPLv3
 * ( http://www.gnu.org/licenses/gpl-3.0.html )
 *
 * This code is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package kintsugi3d.builder.resources;

import kintsugi3d.gl.core.*;
import kintsugi3d.gl.nativebuffer.NativeVectorBufferFactory;
import kintsugi3d.util.ArrayBackedColorImage;
import kintsugi3d.util.EncodableColorImage;
import kintsugi3d.util.EnvironmentMap;

import java.io.File;
import java.io.IOException;

class LoadableEnvironmentCubemap<ContextType extends Context<ContextType>> implements LoadableResource<EnvironmentMap, Cubemap<ContextType>>
{
    private final ContextType context;
    private final LightingResources<ContextType> lightingResources;

    LoadableEnvironmentCubemap(ContextType context, LightingResources<ContextType> lightingResources)
    {
        this.context = context;
        this.lightingResources = lightingResources;
    }

    @Override
    public final boolean isLoaded()
    {
        return this.lightingResources.getEnvironmentMap() != null;
    }

    @Override
    public final String getProcessName()
    {
        return "Load Environment Map";
    }

    @Override
    public final EnvironmentMap loadCPU(File file) throws IOException
    {
        return EnvironmentMap.loadFromHDRFile(file);
    }

    @Override
    public final Cubemap<ContextType> loadGPU(EnvironmentMap data)
    {
        Cubemap<ContextType> texture = context.getTextureFactory().buildColorCubemap(data.getSide())
            .loadFace(CubemapFace.POSITIVE_X, NativeVectorBufferFactory.createFromFloatArray(3,
                data.getData()[EnvironmentMap.PX].length / 3, data.getData()[EnvironmentMap.PX]))
            .loadFace(CubemapFace.NEGATIVE_X, NativeVectorBufferFactory.createFromFloatArray(3,
                data.getData()[EnvironmentMap.NX].length / 3, data.getData()[EnvironmentMap.NX]))
            .loadFace(CubemapFace.POSITIVE_Y, NativeVectorBufferFactory.createFromFloatArray(3,
                data.getData()[EnvironmentMap.PY].length / 3, data.getData()[EnvironmentMap.PY]))
            .loadFace(CubemapFace.NEGATIVE_Y, NativeVectorBufferFactory.createFromFloatArray(3,
                data.getData()[EnvironmentMap.NY].length / 3, data.getData()[EnvironmentMap.NY]))
            .loadFace(CubemapFace.POSITIVE_Z, NativeVectorBufferFactory.createFromFloatArray(3,
                data.getData()[EnvironmentMap.PZ].length / 3, data.getData()[EnvironmentMap.PZ]))
            .loadFace(CubemapFace.NEGATIVE_Z, NativeVectorBufferFactory.createFromFloatArray(3,
                data.getData()[EnvironmentMap.NZ].length / 3, data.getData()[EnvironmentMap.NZ]))
            .setInternalFormat(ColorFormat.RGB32F)
            .setMipmapsEnabled(true)
            .setLinearFilteringEnabled(true)
            .createTexture();
        texture.setTextureWrap(TextureWrapMode.REPEAT, TextureWrapMode.NONE);
        return texture;
    }

    @Override
    public final void consume(Cubemap<ContextType> resource)
    {
        lightingResources.takeEnvironmentMap(resource);
    }

    @Override
    public final EncodableColorImage getPreviewImage(EnvironmentMap data)
    {
        int width = data.getSide() * 4;
        int height = data.getSide() * 2;
        float[] pixels = EnvironmentMap.toPanorama(data.getData(), data.getSide(), width, height);
        return new ArrayBackedColorImage(width, height, pixels);
    }
}
