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

package kintsugi3d.gl.core;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

class ColorTextureReader2D<ContextType extends Context<ContextType>> extends ColorTextureReaderBase
{
    private final Texture2D<ContextType> texture;

    ColorTextureReader2D(Texture2D<ContextType> texture)
    {
        this.texture = texture;
    }

    @Override
    public int getWidth()
    {
        return texture.getWidth();
    }

    @Override
    public int getHeight()
    {
        return texture.getHeight();
    }

    @Override
    public void readARGB(ByteBuffer destination, int x, int y, int readWidth, int readHeight)
    {
        try (FramebufferObject<ContextType> fbo = texture.getContext()
            .buildFramebufferObject(this.getWidth(), this.getHeight())
            .addEmptyColorAttachment()
            .createFramebufferObject())
        {
            fbo.setColorAttachment(0, texture);
            fbo.getTextureReaderForColorAttachment(0).readARGB(destination, x, y, readWidth, readHeight);
        }
    }

    @Override
    public void readFloatingPointRGBA(FloatBuffer destination, int x, int y, int readWidth, int readHeight)
    {
        try (FramebufferObject<ContextType> fbo = texture.getContext()
            .buildFramebufferObject(this.getWidth(), this.getHeight())
            .addEmptyColorAttachment()
            .createFramebufferObject())
        {
            fbo.setColorAttachment(0, texture);
            fbo.getTextureReaderForColorAttachment(0).readFloatingPointRGBA(destination, x, y, readWidth, readHeight);
        }
    }

    @Override
    public void readIntegerRGBA(IntBuffer destination, int x, int y, int readWidth, int readHeight)
    {
        try (FramebufferObject<ContextType> fbo = texture.getContext()
            .buildFramebufferObject(this.getWidth(), this.getHeight())
            .addEmptyColorAttachment()
            .createFramebufferObject())
        {
            fbo.setColorAttachment(0, texture);
            fbo.getTextureReaderForColorAttachment(0).readIntegerRGBA(destination, x, y, readWidth, readHeight);
        }
    }
}
