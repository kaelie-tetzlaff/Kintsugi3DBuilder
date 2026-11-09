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

package kintsugi3d.builder.rendering;

import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.fit.settings.ExportSettings;
import kintsugi3d.builder.resources.DynamicResourceManager;
import kintsugi3d.builder.resources.project.GraphicsResourcesImageSpace;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.Framebuffer;
import kintsugi3d.gl.core.FramebufferSize;
import kintsugi3d.gl.interactive.InteractiveRenderableResource;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.vecmath.Matrix4;

import java.io.File;

/**
 * Interface for the implementation of the actual image-based rendering / relighting technique.
 * @param <ContextType> The type of the graphics context that this implementation uses.
 */
public interface ImageBasedRenderable<ContextType extends Context<ContextType>>
    extends InteractiveRenderableResource<ContextType>, ReadonlyImageBasedRenderable<ContextType>
{
    @Override
    default void draw(Framebuffer<ContextType> framebuffer, Matrix4 modelViewOverride, Matrix4 projectionOverride)
    {
        FramebufferSize framebufferSize = framebuffer.getSize();
        this.draw(framebuffer, modelViewOverride, projectionOverride, framebufferSize.width , framebufferSize.height);
    }

    @Override
    default void draw(Framebuffer<ContextType> framebuffer)
    {
        draw(framebuffer, null, null);
    }

    /**
     * Sets the loading monitor for this implementation.
     * The implementation may use this as a callback to update a loading bar as the object is being loaded.
     * @param progressMonitor
     */
    void setProgressMonitor(ProgressMonitor progressMonitor);

    @Override
    ViewSet getViewSet();

    @Override
    SceneModel getSceneModel();

    /**
     * Reloads all of the shaders.
     */
    void reloadShaders();

    @Override
    GraphicsResourcesImageSpace<ContextType> getResources();

    /**
     * Gets the scene resource manager (handles environment map, backplate, tonemapping, etc.)
     * @return The scene resource manager
     */
    DynamicResourceManager getDynamicResourceManager();

    @Override
    default void saveGLTF(File outputDirectory, ExportSettings settings)
    {
        saveGLTF(outputDirectory, "model.glb", settings);
    }

    @Override
    default void saveGLTF(File outputDirectory, String filename, ExportSettings settings)
    {
        saveGLTF(outputDirectory, filename, settings, null);
    }

}
