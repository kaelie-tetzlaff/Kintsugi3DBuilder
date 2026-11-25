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

package kintsugi3d.builder.rendering.components.lit;

import kintsugi3d.builder.rendering.CameraViewport;
import kintsugi3d.builder.rendering.RenderedComponent;
import kintsugi3d.builder.rendering.SceneModel;
import kintsugi3d.builder.resources.LightingResources;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.FramebufferObject;
import kintsugi3d.gl.core.ReadonlyVertexBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LitRoot<ContextType extends Context<ContextType>> implements RenderedComponent<ContextType>
{
    private final ContextType context;
    private final @NotNull LightingResources<ContextType> lightingResources;
    private final @NotNull LitContent<ContextType> litContentRoot;

    /**
     * x
     * @param context
     * @param sceneModel
     * @param litContentRoot Transfers ownership of the lit content to this object
     */
    public LitRoot(ContextType context, SceneModel sceneModel, @NotNull LitContent<ContextType> litContentRoot)
    {
        this.context = context;
        lightingResources = new LightingResources<>(context, sceneModel);

        this.litContentRoot = litContentRoot;
        litContentRoot.initWithLightingResources(lightingResources);
    }

    public final @NotNull LightingResources<ContextType> getLightingResources()
    {
        return lightingResources;
    }

    @Override
    public final void initialize()
    {
        // i.e. shadow map, environment map, etc.
        lightingResources.initialize();
        litContentRoot.initialize();
    }

    @Override
    public final void reloadShaders()
    {
        lightingResources.reloadShadowShader();
        litContentRoot.reloadShaders();
    }

    @Override
    public final void update()
    {
        litContentRoot.update();
    }

    @Override
    public final void draw(FramebufferObject<ContextType> framebuffer, @NotNull CameraViewport cameraViewport)
    {
        setupForDraw(cameraViewport);
        litContentRoot.draw(framebuffer, cameraViewport);
    }

    @Override
    public final void drawInSubdivisions(@NotNull FramebufferObject<ContextType> framebuffer, int subdivWidth, int subdivHeight,
                                         @NotNull CameraViewport cameraViewport)
    {
        setupForDraw(cameraViewport);
        litContentRoot.drawInSubdivisions(framebuffer, subdivWidth, subdivHeight, cameraViewport);
    }

    private void setupForDraw(@NotNull CameraViewport cameraViewport)
    {
        lightingResources.refreshShadowMaps();

        // Screen space depth buffer for specular shadows
        lightingResources.refreshScreenSpaceDepthFBO(cameraViewport.getView(), cameraViewport.getFullProjection());

        context.flush();
    }

    @Override
    public final void close()
    {
        lightingResources.close();
        litContentRoot.close();
    }

    public final @NotNull RenderedComponent<ContextType> getLitContentRoot()
    {
        return litContentRoot;
    }

    /**
     * Must be called after initialize
     * @param shadowCaster
     */
    public final void setShadowCaster(@Nullable ReadonlyVertexBuffer<ContextType> shadowCaster)
    {
        lightingResources.setShadowCastingPositionBuffer(shadowCaster);
    }
}
