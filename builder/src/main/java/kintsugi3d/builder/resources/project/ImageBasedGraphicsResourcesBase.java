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

import kintsugi3d.builder.core.texture.TextureInfo;
import kintsugi3d.builder.core.viewset.View;
import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.fit.decomposition.BasisMaterialInfo;
import kintsugi3d.builder.io.ReadonlyLoadOptionsModel;
import kintsugi3d.builder.io.events.ProjectProcessedListener;
import kintsugi3d.builder.resources.project.events.TexturesReplacedListener;
import kintsugi3d.builder.resources.project.texture.TextureResources;
import kintsugi3d.builder.util.events.EventListeners;
import kintsugi3d.builder.util.events.MappedChange;
import kintsugi3d.builder.util.events.Observer;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.geometry.GeometryResources;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.UserCancellationException;
import kintsugi3d.gl.vecmath.IntVector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

abstract class ImageBasedGraphicsResourcesBase<ContextType extends Context<ContextType>> implements ImageBasedGraphicsResources<ContextType>
{
    private final GraphicsResourcesCommon<ContextType> commonResources;

    /**
     * Only one instance will be the owner of the shared resources (typicaly created when a project is loaded)
     */
    private final boolean ownerOfCommonResources;

    ImageBasedGraphicsResourcesBase(GraphicsResourcesCommon<ContextType> commonResources, boolean ownerOfCommonResources)
    {
        this.commonResources = commonResources;
        this.ownerOfCommonResources = ownerOfCommonResources;
    }

    final GraphicsResourcesCommon<ContextType> getCommonResources()
    {
        return commonResources;
    }

    @Override
    public final @NotNull ContextType getContext()
    {
        return commonResources.getContext();
    }

    @Override
    public final @Nullable ViewSet getViewSet()
    {
        return commonResources.getViewSet();
    }

    @Override
    public final @NotNull GeometryResources<ContextType> getGeometryResources()
    {
        return commonResources.getGeometryResources();
    }

    @Override
    public final TextureResources<ContextType> getTextureResources()
    {
        return commonResources.getTextureResources();
    }

    @Override
    public final @NotNull LuminanceMapResources<ContextType> getLuminanceMapResources()
    {
        return commonResources.getLuminanceMapResources();
    }

    /**
     * Creates a resource for just a single view, using the default image for that view but with custom load options
     *
     * @param view
     * @param loadOptions
     * @return
     * @throws IOException
     */
    public final @NotNull SingleCalibratedImageResource<ContextType> createSingleImageResource(@NotNull View view, @NotNull ReadonlyLoadOptionsModel loadOptions)
        throws IOException
    {
        return new SingleCalibratedImageResource<>(getContext(), view, getGeometry(), loadOptions);
    }

    @Override
    public final @NotNull ImageCache<ContextType> cache(@NotNull ReadonlyImageCacheSettings settings, ProgressMonitor monitor)
        throws IOException, UserCancellationException
    {
        ImageCache<ContextType> cache = new ImageCache<>(this, settings);

        if (!cache.isInitialized())
        {
            cache.initialize(monitor);
        }

        return cache;
    }

    @Override
    public final boolean hasProcessedWeightMaps()
    {
        return commonResources.hasProcessedWeightMaps();
    }

    @Override
    public final @NotNull IntVector2 getProcessedWeightMapResolution()
    {
        return commonResources.getProcessedWeightMapResolution();
    }

    @Override
    public final @NotNull EventListeners<ProjectProcessedListener> weightMapsProcessedListeners()
    {
        return commonResources.weightMapsProcessedListeners();
    }

    @Override
    public final @NotNull EventListeners<TexturesReplacedListener> texturesReplacedListeners()
    {
        return commonResources.texturesReplacedListeners();
    }

    @Override
    public final void replaceTextureResources(TextureResources<ContextType> textureResources)
    {
        commonResources.replaceTextureResources(textureResources);
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public void close()
    {
        if (ownerOfCommonResources && commonResources != null)
        {
            commonResources.close();
        }
    }

    public final @NotNull EventListeners<Observer<MappedChange<String, BasisMaterialInfo>>> basisListeners()
    {
        return commonResources.basisListeners();
    }

    public final @NotNull EventListeners<Observer<MappedChange<String, TextureInfo>>> texturesListeners()
    {
        return commonResources.texturesListeners();
    }
}
