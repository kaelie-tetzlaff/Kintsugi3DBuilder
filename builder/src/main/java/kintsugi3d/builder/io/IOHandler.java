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

package kintsugi3d.builder.io;

import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.fit.settings.ExportSettings;
import kintsugi3d.builder.io.events.ProjectLoadedListener;
import kintsugi3d.builder.io.events.ProjectProcessedListener;
import kintsugi3d.builder.io.metashape.MetashapeModel;
import kintsugi3d.builder.rendering.ReadonlyImageBasedRenderable;
import kintsugi3d.builder.state.shader.ShaderInfo;
import kintsugi3d.builder.util.events.EventListeners;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.geometry.ReadonlyVertexGeometry;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.util.EncodableColorImage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface IOHandler
{
    @Nullable ViewSet getLoadedViewSet();
    @Nullable ReadonlyVertexGeometry getLoadedGeometry();


    boolean isRenderableLoaded();
    ReadonlyImageBasedRenderable<?> getMainRenderable();
    ReadonlyImageBasedRenderable<? extends Context<?>> getRenderableForShader(ShaderInfo shader);

    EventListeners<ProjectLoadedListener> projectLoadedListeners();
    EventListeners<ProjectProcessedListener> projectProcessedListeners();

    /**
     * Must NOT be called on the rendering thread or deadlock will result while generating preview images.
     * @param internalName
     * @param vsetFile
     * @param supportingFilesDirectory
     * @param loadOptions
     */
    void loadFromVSETFile(String internalName, File vsetFile, File supportingFilesDirectory, ReadonlyLoadOptionsModel loadOptions)
        throws Exception;

    /**
     * Must NOT be called on the rendering thread or deadlock will result while generating preview images.
     * @param internalName
     * @param xmlFile
     * @param viewSetLoadOptions
     * @param imageLoadOptions
     */
    void loadFromLooseFiles(File newProjectFile, String internalName, File xmlFile,
                            ViewSetLoadOptions viewSetLoadOptions, ReadonlyLoadOptionsModel imageLoadOptions)
        throws Exception;

    /**
     * Must NOT be called on the rendering thread or deadlock will result while generating preview images.
     * @param model
     * @param loadOptionsModel
     */
    void loadFromMetashapeModel(File newProjectFile, MetashapeModel model, ReadonlyLoadOptionsModel loadOptionsModel)
        throws Exception;

    @Nullable EncodableColorImage loadEnvironmentMap(File environmentMapFile) throws FileNotFoundException;
    void loadBackplate(File backplateFile) throws FileNotFoundException;

    void saveToVSETFile(File vsetFile) throws IOException;

    /**
     *
     * @param materialDirectory
     * @param finishedCallback No guarantees are made about which thread the callback will run on.
     */
    void saveAllMaterialFiles(File materialDirectory, Runnable finishedCallback);

    void saveGLTF(File outputDirectory, ExportSettings settings);

    /**
     *
     * @param onUnloadComplete Whether or not a project needs to be unloaded, will run once the unload process has finished.
     *                         This callback is the point at which it is safe to start loading another project again without a race condition.
     *                         The one exception to this rule is that UI elements may still update after the callable has run,
     *                         but the request to update them will have been submitted so it should usually be fine with FIFO sequencing.
     */
    void unload(Runnable onUnloadComplete);

    void setProgressMonitor(ProgressMonitor progressMonitor);
}
