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

package kintsugi3d.gl.interactive;

import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.Framebuffer;
import kintsugi3d.gl.core.FramebufferSize;
import kintsugi3d.gl.vecmath.Matrix4;

@FunctionalInterface
public interface SubdivisionRenderable<ContextType extends Context<ContextType>> extends Renderable<ContextType>
{
    /**
     * Draw the object using the current settings and selections in the 3D viewport,
     * potentially in subdivisions to avoid graphics card timeouts.
     *
     * @param framebuffer        The framebuffer into which to draw the object.
     * @param modelViewOverride  The model-view matrix.  If this is null, it will default to the current viewpoint in the app.
     * @param projectionOverride The projection matrix.  If this is null, it will default to the current camera in the app.
     * @param subdivWidth        The width of the rectangle of pixels to draw at once.  This can be set to a fraction of the
     *                           framebuffer width to reduce the likelihood of graphics card timeouts that would crash the application.
     * @param subdivHeight       The height of the rectangle of pixels to draw at once.  This can be set to a fraction of the
     *                           framebuffer height to reduce the likelihood of graphics card timeouts that would crash the application.
     */
    void draw(Framebuffer<ContextType> framebuffer, Matrix4 modelViewOverride, Matrix4 projectionOverride, int subdivWidth, int subdivHeight);

    @Override
    default void draw(Framebuffer<ContextType> framebuffer, Matrix4 modelViewOverride, Matrix4 projectionOverride)
    {
        FramebufferSize framebufferSize = framebuffer.getSize();
        draw(framebuffer, modelViewOverride, projectionOverride, framebufferSize.width, framebufferSize.height);
    }
}
