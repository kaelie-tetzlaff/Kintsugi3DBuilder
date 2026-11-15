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

import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.rendering.components.RenderingSubject;
import kintsugi3d.builder.resources.project.ImageBasedGraphicsResources;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.nativebuffer.NativeVectorBufferFactory;
import kintsugi3d.gl.util.ImageHelper;
import kintsugi3d.gl.vecmath.Vector3;
import kintsugi3d.util.ArrayBackedColorImage;
import kintsugi3d.util.EncodableColorImage;
import kintsugi3d.util.EnvironmentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class DynamicResourceLoader<ContextType extends Context<ContextType>> implements DynamicResourceManager
{
    private static final Logger LOG = LoggerFactory.getLogger(DynamicResourceLoader.class);
    private final ProgressMonitor progressMonitor;
    private final ContextType context;
    private final ImageBasedGraphicsResources<ContextType> resources;
    private final LightingResources<ContextType> lightingResources;
    private final RenderingSubject<ContextType> subject;

    /** Flag checked by the render thread in {@link #update()}; set by a worker thread in {@link #loadEnvironmentMap(File)}. */
    private volatile boolean newEnvironmentDataAvailable;

    /** Only accessed within {@code synchronized(loadEnvironmentLock)} blocks. */
    private EnvironmentMap newEnvironmentData;

    /** Flag checked by the render thread in {@link #update()}; set by a worker thread in {@link #loadEnvironmentMap(File)}. */
    private volatile boolean environmentMapUnloadRequested = false;

    /** Only accessed within {@code synchronized(loadEnvironmentLock)} blocks. */
    private File currentEnvironmentFile;

    /** Only accessed within {@code synchronized(loadEnvironmentLock)} blocks. */
    private long environmentLastModified;

    /** Lock for synchronized access to environment map data. */
    private final Object loadEnvironmentLock = new Object();

    /** Flag checked by the render thread in {@link #update()}; set by the JavaFX thread in {@link #requestFragmentShader(File)}. */
    private volatile File desiredShaderFile;

    /** Read by the render thread in {@link #update()}; set by the JavaFX thread in {@link #requestFragmentShader(File)}. */
    private volatile Supplier<Map<String, Optional<Object>>> shaderDefinesFactory;

    /** Written before acquiring lock in {@link #loadEnvironmentMap(File)}; read inside {@code synchronized(loadEnvironmentLock)}. */
    private volatile File desiredEnvironmentFile;

    /** Flag checked by the render thread in {@link #update()}; set by a worker thread in {@link #loadBackplate(File)}. */
    private volatile boolean newBackplateDataAvailable;

    /** Only accessed within {@code synchronized(loadBackplateLock)} blocks. */
    private BufferedImage newBackplateData;

    /** Flag checked by the render thread in {@link #update()}; set by a worker thread in {@link #loadBackplate(File)}. */
    private volatile boolean backplateUnloadRequested = false;

    /** Only accessed within {@code synchronized(loadBackplateLock)} blocks. */
    private File currentBackplateFile;

    /** Only accessed within {@code synchronized(loadBackplateLock)} blocks. */
    private long backplateLastModified;

    /** Lock for synchronized access to backplate data. */
    private final Object loadBackplateLock = new Object();

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

    /** Written before acquiring lock in {@link #loadBackplate(File)}; read inside {@code synchronized(loadBackplateLock)}. */
    private volatile File desiredBackplateFile;

    /** Only accessed within {@code synchronized(loadEnvironmentLock)} blocks. */
    private EncodableColorImage currentEnvironmentMap;

    public DynamicResourceLoader(ProgressMonitor progressMonitor, ImageBasedGraphicsResources<ContextType> resources,
        RenderingSubject<ContextType> subject, LightingResources<ContextType> lightingResources)
    {
        this.progressMonitor = progressMonitor;
        this.context = resources.getContext();
        this.resources = resources;
        this.lightingResources = lightingResources;
        this.subject = subject;
    }

    public void update()
    {
        if (this.environmentMapUnloadRequested)
        {
            lightingResources.takeEnvironmentMap(null);
            this.environmentMapUnloadRequested = false;
        }

        if (this.backplateUnloadRequested)
        {
            lightingResources.takeBackplateTexture(null);
            this.backplateUnloadRequested = false;
        }

        if (this.desiredShaderFile != null)
        {
            this.subject.useFragmentShader(desiredShaderFile);
            this.subject.setExtraFragmentShaderDefinesFactory(this.shaderDefinesFactory);
            this.subject.reloadShaders();

            this.desiredShaderFile = null;
        }

        if (this.newEnvironmentDataAvailable)
        {
            try
            {
                Cubemap<ContextType> newEnvironmentTexture = null;

                synchronized(loadEnvironmentLock)
                {
                    if (this.newEnvironmentData != null)
                    {
                        EnvironmentMap environmentData = this.newEnvironmentData;
                        this.newEnvironmentData = null;

                        float[][] sides = environmentData.getData();

                        newEnvironmentTexture = context.getTextureFactory().buildColorCubemap(environmentData.getSide())
                            .loadFace(CubemapFace.POSITIVE_X, NativeVectorBufferFactory.getInstance().createFromFloatArray(3,
                                sides[EnvironmentMap.PX].length / 3, sides[EnvironmentMap.PX]))
                            .loadFace(CubemapFace.NEGATIVE_X, NativeVectorBufferFactory.getInstance().createFromFloatArray(3,
                                sides[EnvironmentMap.NX].length / 3, sides[EnvironmentMap.NX]))
                            .loadFace(CubemapFace.POSITIVE_Y, NativeVectorBufferFactory.getInstance().createFromFloatArray(3,
                                sides[EnvironmentMap.PY].length / 3, sides[EnvironmentMap.PY]))
                            .loadFace(CubemapFace.NEGATIVE_Y, NativeVectorBufferFactory.getInstance().createFromFloatArray(3,
                                sides[EnvironmentMap.NY].length / 3, sides[EnvironmentMap.NY]))
                            .loadFace(CubemapFace.POSITIVE_Z, NativeVectorBufferFactory.getInstance().createFromFloatArray(3,
                                sides[EnvironmentMap.PZ].length / 3, sides[EnvironmentMap.PZ]))
                            .loadFace(CubemapFace.NEGATIVE_Z, NativeVectorBufferFactory.getInstance().createFromFloatArray(3,
                                sides[EnvironmentMap.NZ].length / 3, sides[EnvironmentMap.NZ]))
                            .setInternalFormat(ColorFormat.RGB32F)
                            .setMipmapsEnabled(true)
                            .setLinearFilteringEnabled(true)
                            .createTexture();

                        newEnvironmentTexture.setTextureWrap(TextureWrapMode.REPEAT, TextureWrapMode.NONE);
                    }
                }

                if (newEnvironmentTexture != null)
                {
                    lightingResources.takeEnvironmentMap(newEnvironmentTexture);
                }
            }
            catch (RuntimeException e)
            {
                LOG.error("An error has occurred", e);
            }
            catch (Error e)
            {
                LOG.error("An error has occurred", e);
                //noinspection ProhibitedExceptionThrown
                throw e;
            }
            finally
            {
                this.newEnvironmentDataAvailable = false;
                this.progressMonitor.complete();
            }
        }

        if (this.newBackplateDataAvailable)
        {
            try
            {
                Texture2D<ContextType> newBackplateTexture = null;

                synchronized(loadBackplateLock)
                {
                    if (this.newBackplateData != null)
                    {
                        BufferedImage backplateData = this.newBackplateData;
                        this.newBackplateData = null;

                        newBackplateTexture = context.getTextureFactory().build2DColorTextureFromImage(backplateData, true)
                            .setInternalFormat(CompressionFormat.RGB_PUNCHTHROUGH_ALPHA1_4BPP)
                            .setLinearFilteringEnabled(true)
                            .setMipmapsEnabled(true)
                            .createTexture();
                    }
                }

                if (newBackplateTexture != null)
                {
                    lightingResources.takeBackplateTexture(newBackplateTexture);
                }
            }
            catch (RuntimeException e)
            {
                LOG.error("An error has occurred", e);
            }
            catch (Error e)
            {
                LOG.error("An error has occurred", e);
                //noinspection ProhibitedExceptionThrown
                throw e;
            }
            finally
            {
                this.newBackplateDataAvailable = false;
            }
        }

        ViewSet viewSet = this.resources.getViewSet();

        if (this.newLuminanceEncodingDataAvailable)
        {
            if (this.newLinearLuminanceValues != null && this.newEncodedLuminanceValues != null)
            {
                viewSet.setLuminanceEncoding(this.newLinearLuminanceValues, this.newEncodedLuminanceValues);
            }
            else
            {
                viewSet.clearLuminanceEncoding();
            }

            this.newLuminanceEncodingDataAvailable = false;
        }

        if (this.newLightCalibrationAvailable)
        {
            viewSet.setAllLightPositions(this.newLightCalibration);
            this.newLightCalibrationAvailable = false;
        }
    }

    @Override
    public void requestFragmentShader(File shaderFile)
    {
        this.desiredShaderFile = shaderFile;
    }

    @Override
    public void requestFragmentShader(File shaderFile, Supplier<Map<String, Optional<Object>>> extraDefinesFactory)
    {
        this.desiredShaderFile = shaderFile;
        this.shaderDefinesFactory = extraDefinesFactory;
    }

    @Override
    public Optional<EncodableColorImage> loadEnvironmentMap(File environmentFile) throws FileNotFoundException
    {
        if (environmentFile == null)
        {
            if (this.lightingResources.getEnvironmentMap() != null)
            {
                this.environmentMapUnloadRequested = true;
            }

            synchronized(loadEnvironmentLock)
            {
                currentEnvironmentMap = null;
            }

            return Optional.empty();
        }
        else if (environmentFile.exists())
        {
            LOG.info("Loading new environment texture.");

            // Update desired file immediately so concurrent callers can see the latest request
            this.desiredEnvironmentFile = environmentFile;
            long lastModified = environmentFile.lastModified();

            synchronized(loadEnvironmentLock)
            {
                // Check if this request is still the desired one; if a newer call updated desiredEnvironmentFile, skip loading
                if (Objects.equals(environmentFile, desiredEnvironmentFile) &&
                    (!Objects.equals(environmentFile, currentEnvironmentFile) || lastModified != environmentLastModified) && !this.progressMonitor.isConflictingProcess())
                {
                    this.progressMonitor.start();
                    this.progressMonitor.setMaxProgress(0.0);
                    this.progressMonitor.setProcessName("Load Environment Map");

                    try
                    {
                        // Use Michael Ludwig's code to convert to a cube map (supports either cross or panorama input)
                        this.newEnvironmentData = EnvironmentMap.loadFromHDRFile(environmentFile);
                        this.currentEnvironmentFile = environmentFile;
                        int width = newEnvironmentData.getSide() * 4;
                        int height = newEnvironmentData.getSide() * 2;
                        float[] pixels = EnvironmentMap.toPanorama(newEnvironmentData.getData(), newEnvironmentData.getSide(), width, height);

                        this.newEnvironmentDataAvailable = true;
                        environmentLastModified = lastModified;
                        currentEnvironmentMap = new ArrayBackedColorImage(width, height, pixels);
                    }
                    catch (FileNotFoundException e)
                    {
                        throw e;
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error loading environment map:", e);
                    }
                }

                // Always return the actual currently loaded map, regardless of code path
                return Optional.ofNullable(currentEnvironmentMap);
            }
        }
        else
        {
            throw new FileNotFoundException(environmentFile.getPath());
        }
    }

    @Override
    public void loadBackplate(File backplateFile) throws FileNotFoundException
    {
        if (backplateFile == null && lightingResources.getBackplateTexture() != null)
        {
            this.backplateUnloadRequested = true;
        }
        else if (backplateFile != null && backplateFile.exists())
        {
            LOG.info("Loading new backplate texture.");

            // Update desired file immediately so concurrent callers can see the latest request
            this.desiredBackplateFile = backplateFile;
            long lastModified = backplateFile.lastModified();

            synchronized(loadBackplateLock)
            {
                // Check if this request is still the desired one; if a newer call updated desiredBackplateFile, exit early
                if (Objects.equals(backplateFile, desiredBackplateFile))
                {
                    if (!Objects.equals(backplateFile, currentBackplateFile) || lastModified != backplateLastModified)
                    {
                        try
                        {
                            this.newBackplateData = ImageHelper.read(backplateFile).getBufferedImage();
                            this.currentBackplateFile = backplateFile;

                            this.newBackplateDataAvailable = true;
                            backplateLastModified = lastModified;
                        }
                        catch (FileNotFoundException e)
                        {
                            throw e;
                        }
                        catch (IOException e)
                        {
                            LOG.error("Error loading backplate:", e);
                        }
                    }
                }
            }
        }
        else if (backplateFile != null)
        {
            throw new FileNotFoundException(backplateFile.getPath());
        }
    }

    @Override
    public void setTonemapping(double[] linearLuminanceValues, byte[] encodedLuminanceValues)
    {
        this.newLinearLuminanceValues = linearLuminanceValues.clone();
        this.newEncodedLuminanceValues = encodedLuminanceValues.clone();
        this.newLuminanceEncodingDataAvailable = true;
    }

    @Override
    public void clearTonemapping()
    {
        this.newLinearLuminanceValues = null;
        this.newEncodedLuminanceValues = null;
        this.newLuminanceEncodingDataAvailable = true;
    }

    @Override
    public void setLightCalibration(Vector3 lightCalibration)
    {
        this.newLightCalibration = lightCalibration;
        this.newLightCalibrationAvailable = true;
    }
}
