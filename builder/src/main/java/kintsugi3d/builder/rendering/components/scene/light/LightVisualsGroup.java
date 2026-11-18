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

package kintsugi3d.builder.rendering.components.scene.light;

import kintsugi3d.builder.rendering.CameraViewport;
import kintsugi3d.builder.rendering.RenderedComponent;
import kintsugi3d.builder.rendering.SceneModel;
import kintsugi3d.builder.rendering.SceneViewportModel;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.FramebufferObject;

/**
 * Class for drawing the 3D light representations and manipulation widgets.
 * @param <ContextType>
 */
public class LightVisualsGroup<ContextType extends Context<ContextType>> implements RenderedComponent<ContextType>
{
    private final SceneModel sceneModel;
    private final SceneViewportModel sceneViewportModel;

    private final Lights<ContextType> lights;
    private final LightCenters<ContextType> lightCenters;
    private final LightWidgets<ContextType> lightWidgets;
    private final LightWidgetCircles<ContextType> lightWidgetCircle;
    private final LightWidgetLines<ContextType> lightWidgetLines;

    public LightVisualsGroup(ContextType context, SceneModel sceneModel, SceneViewportModel sceneViewportModel)
    {
        this.sceneModel = sceneModel;
        this.sceneViewportModel = sceneViewportModel;

        lights = new Lights<>(context, sceneViewportModel, sceneModel);
        lightCenters = new LightCenters<>(context, sceneViewportModel, sceneModel);
        lightWidgets = new LightWidgets<>(context, sceneViewportModel, sceneModel);
        lightWidgetCircle = new LightWidgetCircles<>(context, sceneViewportModel, sceneModel);
        lightWidgetLines = new LightWidgetLines<>(context, sceneViewportModel, sceneModel);
    }

    @Override
    public final void initialize()
    {
        lights.initialize();
        lightCenters.initialize();
        lightWidgets.initialize();
        lightWidgetCircle.initialize();
        lightWidgetLines.setSolidProgram(lightWidgets.getDrawable().program());
    }

    @Override
    public final void reloadShaders()
    {
        lights.reloadShaders();
        lightCenters.reloadShaders();
        lightWidgets.reloadShaders();
        lightWidgetCircle.reloadShaders();
        lightWidgetLines.setSolidProgram(lightWidgets.getDrawable().program());
    }

    @Override
    public final void draw(FramebufferObject<ContextType> framebuffer, CameraViewport cameraViewport)
    {
        LightWidgetInfo[] widgetInfo = LightWidgetInfo.calculate(sceneViewportModel, sceneModel, cameraViewport, framebuffer.getSize());
        lightWidgets.refreshWidgetInfo(widgetInfo);
        lightWidgetCircle.refreshWidgetInfo(widgetInfo);
        lightWidgetLines.setWidgetInfo(widgetInfo);

        lightCenters.draw(framebuffer, cameraViewport);
        lights.draw(framebuffer, cameraViewport);
        lightWidgets.draw(framebuffer, cameraViewport);
        lightWidgetCircle.draw(framebuffer, cameraViewport);
        lightWidgetLines.draw(framebuffer, cameraViewport);
    }

    @Override
    public final void close()
    {
        lights.close();
        lightCenters.close();
        lightWidgets.close();
        lightWidgetCircle.close();
        lightWidgetLines.close();
    }
}
