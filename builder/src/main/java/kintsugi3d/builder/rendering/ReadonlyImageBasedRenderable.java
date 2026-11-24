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

import kintsugi3d.builder.core.viewset.ReadonlyViewSet;
import kintsugi3d.builder.fit.settings.ExportSettings;
import kintsugi3d.builder.resources.project.ReadonlyGraphicsResourcesImageSpace;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.interactive.SubdivisionRenderable;

import java.io.File;

public interface ReadonlyImageBasedRenderable<ContextType extends Context<ContextType>>
    extends ViewportRenderable<ContextType>, SubdivisionRenderable<ContextType>
{
    /**
     * Gets the view set for the currently loaded object.
     *
     * @return The view set.
     */
    ReadonlyViewSet getViewSet();

    /**
     * Gets the resources used by this project instance.
     * These resources can be used to accomplish other shading tasks other than the built-in image-based renderer.
     * The resources are automatically destroyed when this implementation closes.
     * @return The project resources.
     */
    @Override
    ReadonlyGraphicsResourcesImageSpace<ContextType> getResources();

    /**
     * Gets the scene model (object, camera, and lights)
     * Upon modifying the object, camera, or lights models,
     * subsequent changes to the model will be reflected in the behavior of this implementation.
     *
     * @return The scene model.
     */
    ReadonlySceneModel getSceneModel();

    /**
     * Gets the ID of the project instance.
     *
     * @return
     */
    String getInternalName();

    void saveGLTF(File outputDirectory, ExportSettings settings);

    void saveGLTF(File outputDirectory, String filename, ExportSettings settings);

    /**
     * Saves the glTF file.
     * Various export settings are available, including the ability to toggle whether textures are saved automatically.
     * Typically, there are three scenarios in which this might be called.<br><br>
     * 1) When specular fit / process textures finishes, export the glTF model so that it can be previewed in Kintsugi 3D Viewer.
     * In this scenario, typically it should be configured to not re-export the textures
     * since the specular fit process saves textures automatically as it processes them.<br><br>
     * 2) When the project is saved, similarly export the glTF model so that it can be previewed in Kintsugi 3D Viewer.
     * In this scenario, texture saving should be the responsibility of the code module that also handles loading
     * so that projects can successfully find the texture files.
     * Ideally this matches the texture filenames specified in the glTF model
     * so that the Kintsugi 3D Viewer can load them successfully --
     * but this is a lower priority than ensuring that Kintsugi 3D Builder projects open correctly.<br><br>
     * 3) When exporting the project, save the glTF model for use in Kintsugi 3D Viewer, Sketchfab, etc.
     * This is the one scenario in which the glTF model export should also handle texture export --
     * since the primary purpose is for transmission to Kintsugi 3D Viewer, Sketchfab and so on,
     * so it is critical that the textures can be found by the glTF model
     * (more so than the convenience feature of opening in Kintsugi 3D Viewer;
     * this is the primary export mechanism and must work correctly.)
     * This also the only scenario in which exporting LODs is relevant since they aren't really needed
     * or desirable for the Kintsugi 3D Viewer preview, or used by Builder itself.
     *
     * @param outputDirectory
     * @param filename
     * @param settings
     * @param finishedCallback
     */
    void saveGLTF(File outputDirectory, String filename, ExportSettings settings, Runnable finishedCallback);
}
