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

package kintsugi3d.builder.resources;

import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.rendering.components.RenderingSubject;
import kintsugi3d.builder.resources.project.ImageBasedGraphicsResources;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.Cubemap;
import kintsugi3d.gl.core.Texture2D;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.vecmath.Vector3;
import kintsugi3d.util.EncodableColorImage;
import kintsugi3d.util.EnvironmentMap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class DynamicResourceLoader<ContextType extends Context<ContextType>> implements DynamicResourceManager
{
    private final ProgressMonitor progressMonitor;
    private final ImageBasedGraphicsResources<ContextType> resources;
    private final RenderingSubject<ContextType> subject;

    /** Async-loaded resource for environment maps. */
    private final AsyncLoadedResource<EnvironmentMap, Cubemap<ContextType>> environmentResource;

    /** Async-loaded resource for backplate images. */
    private final AsyncLoadedResource<BufferedImage, Texture2D<ContextType>> backplateResource;

    /** Flag checked by the render thread in {@link #update()}; set by the JavaFX thread in {@link #requestFragmentShader(File)}. */
    private volatile File desiredShaderFile;

    /** Read by the render thread in {@link #update()}; set by the JavaFX thread in {@link #requestFragmentShader(File)}. */
    private volatile Supplier<Map<String, Optional<Object>>> shaderDefinesFactory;

    /** Flag checked by the render thread in {@link #update()}; set by the JavaFX thread in {@link #setTonemapping(double[], byte[])} / {@link #clearTonemapping()}. */
    private volatile boolean newLuminanceEncodingDataAvailable;

    /** Array reference replaced atomically; read by the render thread in {@link #update()}; set by the JavaFX thread.
     * The arrays themselves are never mutated after assignment (cloned on write). */
    private volatile double[] newLinearLuminanceValues;

    /** Array reference replaced atomically; read by the render thread in {@link #update()}; set by the JavaFX thread.
     * The arrays themselves are never mutated after assignment (cloned on write). */
    private volatile byte[] newEncodedLuminanceValues;

    /** Flag checked by the render thread in {@link #update()}; set by the JavaFX thread in {@link #setLightCalibration(Vector3)}. */
    private volatile boolean newLightCalibrationAvailable;

    /** Read by the render thread in {@link #update()}; set by the JavaFX thread in {@link #setLightCalibration(Vector3)}. */
    private volatile Vector3 newLightCalibration;

    public DynamicResourceLoader(ProgressMonitor progressMonitor, ImageBasedGraphicsResources<ContextType> resources,
        RenderingSubject<ContextType> subject, LightingResources<ContextType> lightingResources)
    {
        this.progressMonitor = progressMonitor;
        this.resources = resources;
        this.subject = subject;

        ContextType context = resources.getContext();
        environmentResource = new AsyncLoadedResource<>(new LoadableEnvironmentCubemap<>(context, lightingResources));
        backplateResource = new AsyncLoadedResource<>(new LoadableBackplateTexture<>(context, lightingResources));
    }

    public final void update()
    {
        // Delegate environment map consumption to AsyncLoadedResource
        environmentResource.consume(progressMonitor);

        // Delegate backplate consumption to AsyncLoadedResource
        backplateResource.consume(null);

        if (desiredShaderFile != null)
        {
            subject.useFragmentShader(desiredShaderFile);
            subject.setExtraFragmentShaderDefinesFactory(shaderDefinesFactory);
            subject.reloadShaders();

            desiredShaderFile = null;
        }

        ViewSet viewSet = resources.getViewSet();

        if (newLuminanceEncodingDataAvailable)
        {
            if (newLinearLuminanceValues != null && newEncodedLuminanceValues != null)
            {
                viewSet.setLuminanceEncoding(newLinearLuminanceValues, newEncodedLuminanceValues);
            }
            else
            {
                viewSet.clearLuminanceEncoding();
            }

            newLuminanceEncodingDataAvailable = false;
        }

        if (newLightCalibrationAvailable)
        {
            viewSet.setAllLightPositions(newLightCalibration);
            newLightCalibrationAvailable = false;
        }
    }

    @Override
    public final void requestFragmentShader(File shaderFile)
    {
        desiredShaderFile = shaderFile;
    }

    @Override
    public final void requestFragmentShader(File shaderFile, Supplier<Map<String, Optional<Object>>> extraDefinesFactory)
    {
        desiredShaderFile = shaderFile;
        shaderDefinesFactory = extraDefinesFactory;
    }

    @Override
    public final EncodableColorImage loadEnvironmentMap(File environmentFile) throws FileNotFoundException
    {
        return environmentResource.load(environmentFile, progressMonitor);
    }

    @Override
    public final void loadBackplate(File backplateFile) throws FileNotFoundException
    {
        backplateResource.load(backplateFile, null);
    }

    @Override
    public final void setTonemapping(double[] linearLuminanceValues, byte[] encodedLuminanceValues)
    {
        newLinearLuminanceValues = linearLuminanceValues.clone();
        newEncodedLuminanceValues = encodedLuminanceValues.clone();
        newLuminanceEncodingDataAvailable = true;
    }

    @Override
    public final void clearTonemapping()
    {
        newLinearLuminanceValues = null;
        newEncodedLuminanceValues = null;
        newLuminanceEncodingDataAvailable = true;
    }

    @Override
    public final void setLightCalibration(Vector3 lightCalibration)
    {
        newLightCalibration = lightCalibration;
        newLightCalibrationAvailable = true;
    }
}
