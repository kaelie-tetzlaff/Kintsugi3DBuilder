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

package kintsugi3d.builder.rendering.components.lightcalibration;

import kintsugi3d.builder.rendering.CameraViewport;
import kintsugi3d.builder.rendering.RenderedComponent;
import kintsugi3d.builder.rendering.SceneModel;
import kintsugi3d.builder.rendering.SceneViewportModel;
import kintsugi3d.builder.rendering.components.snap.ViewSelection;
import kintsugi3d.builder.rendering.components.snap.ViewSnap;
import kintsugi3d.builder.resources.project.ReadonlyImageBasedGraphicsResources;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.FramebufferObject;
import org.jetbrains.annotations.Nullable;

public class LightCalibrationRoot<ContextType extends Context<ContextType>> implements RenderedComponent<ContextType>
{
    private @Nullable ViewSnap<ContextType> viewSnapRoot;

    public LightCalibrationRoot(ReadonlyImageBasedGraphicsResources<ContextType> resources, SceneModel sceneModel,
                                ViewSelection viewSelection, SceneViewportModel sceneViewportModel)
    {
        viewSnapRoot = new ViewSnap<>(sceneModel, viewSelection, new LightCalibrationContent<>(resources, sceneModel, sceneViewportModel));
    }

    @Override
    public final void initialize()
    {
        viewSnapRoot.initialize();
    }

    @Override
    public final void update()
    {
        viewSnapRoot.update();
    }

    @Override
    public final void reloadShaders()
    {
        viewSnapRoot.reloadShaders();
    }

    @Override
    public final void draw(FramebufferObject<ContextType> framebuffer, CameraViewport cameraViewport)
    {
        viewSnapRoot.draw(framebuffer, cameraViewport);
    }

    @Override
    public final void close()
    {
        if (viewSnapRoot != null)
        {
            viewSnapRoot.close();
            viewSnapRoot = null;
        }
    }
}
