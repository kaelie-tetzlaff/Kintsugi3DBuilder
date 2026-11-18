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

package kintsugi3d.gl.core;

import kintsugi3d.gl.vecmath.IntVector2;

final class NonReadableFramebufferViewport<ContextType extends Context<ContextType>> extends FramebufferViewportBase<ContextType>
{
    NonReadableFramebufferViewport(Framebuffer<ContextType> fullFramebuffer, IntVector2 viewportOffset, FramebufferSize viewportSize)
    {
        super(fullFramebuffer, viewportOffset, viewportSize);
    }

    @Override
    public Framebuffer<ContextType> getViewport(int x, int y, int width, int height)
    {
        // Make sure that a FramebufferViewport never has another FramebufferViewport as its "full framebuffer"
        return new NonReadableFramebufferViewport<>(getFullFramebuffer(),
            new IntVector2(x + getOffset().x, y + getOffset().y), new FramebufferSize(width, height));
    }
}
