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

import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.util.EncodableColorImage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Encapsulates the resource-specific operations that differ between environment maps and backplates.
 *
 * <p>Implementations provide the file loading, in-memory data type, and GPU resource type for a
 * particular async-loaded resource. The {@code CPUResourceType} type parameter is the in-memory data returned
 * by {@link #loadCPU(File)}, and the {@code GPUResourceType} type parameter is the GPU resource type produced
 * by {@link #loadGPU(CPUResourceType)}.
 *
 * @param <CPUResourceType> The in-memory data type (e.g. {@code EnvironmentMap} or {@code BufferedImage}).
 * @param <GPUResourceType> The GPU resource type (e.g. {@code Cubemap} or {@code Texture2D}).
 */
public interface LoadableResource<CPUResourceType, GPUResourceType>
{
    /**
     * Returns a human-readable name for this loading operation, displayed in the progress monitor.
     *
     * @return The name of the loading process.
     */
    String getProcessName();

    /**
     * Returns whether a GPU resource is currently loaded and active.
     *
     * <p>Called from {@link AsyncLoadedResource#load(File, ProgressMonitor)} when the file argument is {@code null},
     * to determine whether an unload signal should be sent before clearing.
     *
     * @return {@code true} if a resource is currently loaded.
     */
    default boolean isLoaded()
    {
        return false;
    }

    /**
     * Loads the resource data from the given file.
     *
     * @param file The file to load.
     * @return The loaded resource data.
     * @throws IOException If an I/O error occurs while loading.
     */
    CPUResourceType loadCPU(File file) throws IOException;

    /**
     * Builds a GPU resource from the loaded resource data.
     *
     * @param data The loaded resource data.
     * @return The created GPU resource.
     */
    GPUResourceType loadGPU(CPUResourceType data);

    /**
     * Applies the newly built GPU resource to the rendering pipeline.
     *
     * <p>Called from the consumer thread (e.g. the render thread) after {@link #loadGPU(CPUResourceType)}
     * has produced a new resource. Passing {@code null} signals that the resource should be cleared/unloaded.
     *
     * @param resource The new GPU resource to apply, or {@code null} to clear the resource.
     */
    void consume(@Nullable GPUResourceType resource);

    /**
     * Returns a preview image derived from the loaded data, if any.
     * For example, an environment map loader may return a panorama image alongside the cube map data.
     *
     * @param data The loaded resource data.
     * @return The preview image result, or {@code null} if none.
     */
    default @Nullable EncodableColorImage getPreviewImage(CPUResourceType data)
    {
        return null;
    }
}
