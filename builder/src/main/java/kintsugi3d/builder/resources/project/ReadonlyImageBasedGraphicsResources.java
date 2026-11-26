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

package kintsugi3d.builder.resources.project;

import kintsugi3d.builder.core.viewset.ReadonlyViewSet;
import kintsugi3d.builder.io.events.ProjectProcessedListener;
import kintsugi3d.builder.resources.project.events.TexturesReplacedListener;
import kintsugi3d.builder.resources.project.stream.GraphicsStreamFactory;
import kintsugi3d.builder.resources.project.texture.ReadonlyTextureResources;
import kintsugi3d.builder.util.events.EventListeners;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.interactive.ShaderResources;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.UserCancellationException;
import kintsugi3d.gl.vecmath.IntVector2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface ReadonlyImageBasedGraphicsResources<ContextType extends Context<ContextType>> extends ShaderResources<ContextType>
{
    /**
     * The view set that these resources were loaded from.
     * @return A read-only view of the view set
     */
    @NotNull ReadonlyViewSet getViewSet();

    /**
     * Diffuse, normal, specular, roughness maps
     * @return
     */
    ReadonlyTextureResources<ContextType> getTextureResources();

    /**
     * 1D textures for encoding and decoding
     * @return
     */
    ReadonlyLuminanceMapResources<ContextType> getLuminanceMapResources();

    boolean hasProcessedWeightMaps();

    /**
     *
     * @return The texture resolution of the weight maps if the project has been fully processed,
     * otherwise throws IllegalStateException
     */
    IntVector2 getProcessedWeightMapResolution();

    EventListeners<TexturesReplacedListener> texturesReplacedListeners();
    EventListeners<ProjectProcessedListener> weightMapsProcessedListeners();

    /**
     * Stream over enabled views only -- disabled views are not included.
     * @return
     */
    default @NotNull GraphicsStreamFactory<ContextType> streamFactory()
    {
        return new GraphicsStreamFactory<>(this);
    }

    ImageCache<ContextType> cache(ReadonlyImageCacheSettings settings, @NotNull ProgressMonitor monitor) throws IOException, UserCancellationException;
}
