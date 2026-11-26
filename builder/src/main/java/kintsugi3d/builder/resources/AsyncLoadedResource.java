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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

/**
 * Handles asynchronous loading of a resource with thread-safe hand-off to a consumer thread.
 *
 * <p>Encapsulates the common pattern shared by environment maps and backplates:
 * <ul>
 *   <li>Worker-thread loading with desired-file tracking for concurrent request optimization.</li>
 *   <li>Change detection based on file reference and last-modified timestamp.</li>
 *   <li>Safe hand-off of loaded data to a consumer thread (e.g. the render thread) via volatile flags.</li>
 *   <li>Consumer-thread GPU resource building and resource replacement.</li>
 *   <li>Unload signaling.</li>
 * </ul>
 *
 * <p>Usage:
 * <ol>
 *   <li>Construct with a {@link LoadableResource} that performs resource-specific loading and resource building.</li>
 *   <li>Call {@link #load(File, ProgressMonitor)} from a worker thread to request a load.</li>
 *   <li>Call {@link #consume(ProgressMonitor)} from the consumer thread to build and apply the GPU resource.</li>
 * </ol>
 *
 * @param <CPUResourceType> The in-memory data type.
 * @param <GPUResourceType> The GPU resource type.
 */
public class AsyncLoadedResource<CPUResourceType, GPUResourceType>
{
    private static final Logger LOG = LoggerFactory.getLogger(AsyncLoadedResource.class);

    /** The resource-specific loader and resource builder. */
    private final LoadableResource<CPUResourceType, GPUResourceType> loadable;

    /** Flag checked by the consumer thread; set by a worker thread in {@link #load(File, ProgressMonitor)}. */
    private volatile boolean newDataAvailable;

    /** Only accessed within {@code synchronized(lock)} blocks. */
    private @Nullable CPUResourceType newData;

    /** Flag checked by the consumer thread; set by a worker thread or caller. */
    private volatile boolean unloadRequested = false;

    /** Only accessed within {@code synchronized(lock)} blocks. */
    private @Nullable File currentFile;

    /** Only accessed within {@code synchronized(lock)} blocks. */
    private long lastModified;

    /** Written before acquiring lock in {@link #load(File, ProgressMonitor)}; read inside {@code synchronized(lock)}. */
    private volatile File desiredFile;

    /** Optional preview image (e.g. panorama image); only accessed within {@code synchronized(lock)} blocks. */
    private @Nullable EncodableColorImage previewImage;

    /** Lock for synchronized access to resource data. */
    private final Object lock = new Object();

    /**
     * Constructs an async-loaded resource handler.
     *
     * @param loadable The resource-specific loader and resource builder.
     */
    public AsyncLoadedResource(LoadableResource<CPUResourceType, GPUResourceType> loadable)
    {
        this.loadable = loadable;
    }

    /**
     * Loads a resource from the given file asynchronously.
     *
     * <p>When {@code file} is {@code null}, clears the current file reference.
     * When {@code file} does not exist, throws {@link FileNotFoundException}.
     *
     * @param file The file to load, or {@code null} to clear the current file reference.
     * @param progressMonitor Optional progress monitor for reporting load progress; may be {@code null}.
     * @return An optional side-effect result produced during loading (e.g. a panorama image for environment maps);
     *         empty if no side-effect was produced or the load was skipped.
     * @throws FileNotFoundException If the file does not exist.
     */
    public final @Nullable EncodableColorImage load(@Nullable File file, @Nullable ProgressMonitor progressMonitor) throws FileNotFoundException
    {
        if (file == null)
        {
            // Unload requested
            if (loadable.isLoaded())
            {
                unloadRequested = true;
            }

            synchronized (lock)
            {
                currentFile = null;
                newData = null;
                previewImage = null;
            }

            return null;
        }
        else if (file.exists())
        {
            LOG.info("Loading new resource: {}", file);

            // Update desired file immediately so concurrent callers can see the latest request
            desiredFile = file;

            long lastModifiedNew = file.lastModified();

            synchronized (lock)
            {
                // Check if this request is still the desired one; if a newer call updated desiredFile, skip loading
                if (Objects.equals(file, desiredFile) &&
                    (!Objects.equals(file, currentFile) || lastModifiedNew != lastModified))
                {
                    // Check for conflicting process only when we are about to do work
                    if (progressMonitor != null && progressMonitor.isConflictingProcess())
                    {
                        return previewImage;
                    }

                    if (progressMonitor != null)
                    {
                        progressMonitor.start();
                        progressMonitor.setMaxProgress(0.0);
                        progressMonitor.setProcessName(loadable.getProcessName());
                    }

                    try
                    {
                        CPUResourceType data = loadable.loadCPU(file);
                        if (data != null)
                        {
                            newData = data;
                            currentFile = file;
                            lastModified = lastModifiedNew;
                            newDataAvailable = true;
                            previewImage = loadable.getPreviewImage(data);
                        }
                        return previewImage;
                    }
                    catch (FileNotFoundException e)
                    {
                        throw e;
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error loading resource:", e);
                        return previewImage;
                    }
                }

                return previewImage;
            }
        }
        else
        {
            throw new FileNotFoundException(file.getPath());
        }
    }

    /**
     * Consumes any newly loaded data by building a GPU resource and applying it via the LoadableResource callback.
     *
     * <p>Must be called from the consumer thread (e.g. the render thread). If new data is available,
     * this method acquires the lock, extracts the data, builds the GPU resource, and invokes the
     * {@link LoadableResource#consume} method with the new resource. The
     * {@code progressMonitor.complete()} is called in the finally block when data was consumed.
     *
     * @param progressMonitor Optional progress monitor; {@code complete()} is called if data was consumed.
     */
    public final void consume(@Nullable ProgressMonitor progressMonitor)
    {
        if (unloadRequested)
        {
            loadable.consume(null);
            unloadRequested = false;
        }

        if (newDataAvailable)
        {
            try
            {
                GPUResourceType newResource = null;

                synchronized (lock)
                {
                    if (newData != null)
                    {
                        CPUResourceType data = newData;
                        newData = null;
                        newResource = loadable.loadGPU(data);
                    }
                }

                if (newResource != null)
                {
                    loadable.consume(newResource);
                }
            }
            catch (RuntimeException e)
            {
                LOG.error("An error has occurred", e);
            }
            finally
            {
                newDataAvailable = false;

                if (progressMonitor != null)
                {
                    progressMonitor.complete();
                }
            }
        }
    }
}
