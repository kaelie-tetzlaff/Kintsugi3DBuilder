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
import kintsugi3d.builder.rendering.SceneModel;
import kintsugi3d.builder.rendering.SceneViewportModel;
import kintsugi3d.builder.rendering.components.ShaderComponent;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.core.BlendFunction.Weight;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;
import kintsugi3d.util.RadialTextureGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LightCenters<ContextType extends Context<ContextType>> extends ShaderComponent<ContextType>
{
    private static final String SCENE_OBJECT_TAG_FORMAT = "Light.%d.Center";

    private final SceneViewportModel sceneViewportModel;
    private final SceneModel sceneModel;

    private Texture2D<ContextType> lightCenterTexture;

    public LightCenters(ContextType context, @NotNull SceneViewportModel sceneViewportModel, @NotNull SceneModel sceneModel)
    {
        super(context);
        this.sceneViewportModel = sceneViewportModel;
        this.sceneModel = sceneModel;

        for (int i = 0; i < sceneModel.getLightingModel().getMaxLightCount(); i++)
        {
            sceneViewportModel.addSceneObjectType(getSceneObjectTag(i));
        }
    }

    private static @NotNull String getSceneObjectTag(int i)
    {
        return String.format(SCENE_OBJECT_TAG_FORMAT, i);
    }

    @Override
    protected final void onInitialize()
    {
        lightCenterTexture = resource(new RadialTextureGenerator<>(getContext())
            .buildCircleTexture(64)
            .setLinearFilteringEnabled(true)
            .setMipmapsEnabled(true)
            .createTexture());
    }

    @Override
    protected final ProgramObject<ContextType> createProgram() throws IOException
    {
        return getContext().getShaderProgramBuilder()
            .addShader(ShaderType.VERTEX, new File(new File(new File("shaders"), "common"), "imgspace.vert"))
            .addShader(ShaderType.FRAGMENT, new File(new File(new File("shaders"), "scene"), "grayscaleTexture.frag"))
            .createProgram();
    }

    @Override
    protected final @NotNull Map<String, VertexBuffer<ContextType>> createVertexBuffers()
    {
        return Map.of("position", getContext().createRectangle());
    }

    @Override
    public final void draw(@NotNull FramebufferObject<ContextType> framebuffer, @NotNull CameraViewport cameraViewport)
    {
        if (sceneModel.getSettingsModel().getBoolean("relightingEnabled")
            && sceneModel.getSettingsModel().getBoolean("visibleLightsEnabled")
            && sceneModel.getSettingsModel().getBoolean("lightWidgetsEnabled"))
        {
            FramebufferSize size = framebuffer.getSize();

            getContext().getState().disableDepthWrite();
            getContext().getState().disableDepthTest();
            getContext().getState().setBlendFunction(new BlendFunction(Weight.ONE, Weight.ONE));

            getDrawable().program().setUniform("projection", cameraViewport.getViewportProjection());
            getDrawable().program().setTexture("grayscaleTex", lightCenterTexture);

            // Draw lights
            for (int i = 0; i < sceneModel.getLightingModel().getLightCount(); i++)
            {
                // Draw "center" point for light widget
                if (sceneModel.getLightingModel().isLightWidgetEnabled(i)
                    && sceneModel.getLightingModel().getLightWidgetModel(i).isCenterWidgetVisible())
                {
                    getDrawable().program().setUniform("objectID", sceneViewportModel.lookupSceneObjectID(getSceneObjectTag(i)));

                    Vector3 lightCenter = cameraViewport.getView().times(sceneModel.getLightingModel().getLightCenter(i).times(sceneModel.getScale()).asPosition()).getXYZ();

                    getDrawable().program().setUniform("model_view",
                        Matrix4.translate(lightCenter)
                            .times(Matrix4.scale(
                                -lightCenter.z * sceneModel.getVerticalFieldOfView(size) / 64.0f,
                                -lightCenter.z * sceneModel.getVerticalFieldOfView(size) / 64.0f,
                                1.0f)));

                    getDrawable().program().setUniform("color",
                        new Vector3(sceneModel.getLightingModel().getLightWidgetModel(i).isCenterWidgetSelected() ? 1.0f : 0.5f));
                    getDrawable().draw(PrimitiveMode.TRIANGLE_FAN, cameraViewport.ofFramebuffer(framebuffer));
                }
            }

            getContext().getState().disableBlending();
            getContext().getState().enableDepthWrite();
            getContext().getState().enableDepthTest();
        }
    }
}
