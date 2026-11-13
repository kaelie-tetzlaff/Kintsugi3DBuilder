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

import java.nio.ShortBuffer;

class DepthTextureReader2D<ContextType extends Context<ContextType>> extends DepthTextureReaderBase
{
    private final Texture2D<ContextType> texture;

    DepthTextureReader2D(Texture2D<ContextType> texture)
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
    public void read(ShortBuffer destination, int x, int y, int width, int height)
    {
        try (FramebufferObject<ContextType> fbo = texture.getContext()
            .buildFramebufferObject(this.getWidth(), this.getHeight())
            .createFramebufferObject())
        {
            fbo.setDepthAttachment(texture);
            fbo.getTextureReaderForDepthAttachment().read(destination, x, y, width, height);
        }
    }
}
