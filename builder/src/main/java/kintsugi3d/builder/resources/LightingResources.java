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

import kintsugi3d.builder.rendering.SceneModel;
import kintsugi3d.gl.builders.framebuffer.DepthAttachmentSpec;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;
import kintsugi3d.gl.vecmath.Vector4;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class LightingResources<ContextType extends Context<ContextType>> implements ManagedResource
{
    private static final Logger LOG = LoggerFactory.getLogger(LightingResources.class);

    /**
     * 0.5 / sqrt(3)
     */
    public static final float CLIP_PLANE_SCALE_MULTIPLIER = 0.28867513f;

    private final ContextType context;
    private final SceneModel sceneModel;

    private @Nullable ProgramObject<ContextType> shadowProgram;

    private @Nullable Texture3D<ContextType> shadowMaps;
    private @Nullable FramebufferObject<ContextType> shadowFramebuffer;
    private @Nullable DrawableResource<ContextType> shadowDrawable;

    // A reference to the position buffer for the object casting shadows.
    // Not managed or released by LightingResources
    private ReadonlyVertexBuffer<ContextType> shadowCastingPositionBuffer;

    private @Nullable Texture2D<ContextType> backplateTexture;
    private @Nullable Cubemap<ContextType> environmentMap;
    private @Nullable FramebufferObject<ContextType> screenSpaceDepthFBO;


    public LightingResources(ContextType context, SceneModel sceneModel)
    {
        this.context = context;
        this.sceneModel = sceneModel;
    }

    public final void initialize()
    {
        shadowMaps = createShadowMaps();
        shadowFramebuffer = context.buildFramebufferObject(2048, 2048)
            .addDepthAttachment()
            .createFramebufferObject();

        screenSpaceDepthFBO = context.buildFramebufferObject(512, 512)
            .addDepthAttachment(DepthAttachmentSpec.createFixedPointWithPrecision(16).setLinearFilteringEnabled(true))
            .createFramebufferObject();

        try
        {
            shadowProgram = context.getShaderProgramBuilder()
                    .addShader(ShaderType.VERTEX, new File(new File(new File("shaders"), "common"), "depth.vert"))
                    .addShader(ShaderType.FRAGMENT, new File(new File(new File("shaders"), "common"), "depth.frag"))
                    .createProgram();
            shadowDrawable = context.createDrawable(shadowProgram);
        }
        catch (IOException e)
        {
            LOG.error("Failed to load shader.", e);
        }
    }

    public final void reloadShadowShader()
    {
        try
        {
            ProgramObject<ContextType> newProgram = context.getShaderProgramBuilder()
                .addShader(ShaderType.VERTEX, new File(new File(new File("shaders"), "common"), "depth.vert"))
                .addShader(ShaderType.FRAGMENT, new File(new File(new File("shaders"), "common"), "depth.frag"))
                .createProgram();

            if (shadowProgram != null)
            {
                shadowProgram.close();
            }

            shadowProgram = newProgram;
            shadowDrawable = context.createDrawable(shadowProgram);

            if (shadowCastingPositionBuffer != null)
            {
                shadowDrawable.addVertexBuffer("position", shadowCastingPositionBuffer);
            }
        }
        catch (IOException|RuntimeException e)
        {
            LOG.error("Failed to load shader.", e);
        }
    }

    public final ReadonlyTexture2D<ContextType> getBackplateTexture()
    {
        return backplateTexture;
    }

    /**
     * Assumes ownership of the backplate texture
     * @param newBackplateTexture
     */
    public final void takeBackplateTexture(Texture2D<ContextType> newBackplateTexture)
    {
        if (backplateTexture != null)
        {
            backplateTexture.close();
        }

        backplateTexture = newBackplateTexture;
    }

    public final @Nullable Cubemap<ContextType> getEnvironmentMap()
    {
        return environmentMap;
    }

    /**
     * Assumes ownership of the environment map resource
     * @param newEnvironmentMap
     */
    public final void takeEnvironmentMap(Cubemap<ContextType> newEnvironmentMap)
    {
        if (environmentMap != null)
        {
            environmentMap.close();
        }

        environmentMap = newEnvironmentMap;
    }

    /**
     * Used for generating shadow maps.
     * Does not take ownership of this buffer.
     * @param positionBuffer
     */
    public final void setShadowCastingPositionBuffer(ReadonlyVertexBuffer<ContextType> positionBuffer)
    {
        shadowDrawable.addVertexBuffer("position", positionBuffer);
        shadowCastingPositionBuffer = positionBuffer;
    }

    public final Matrix4 getLightProjection(int lightIndex)
    {
        Matrix4 lightMatrix = sceneModel.getLightModelViewMatrix(lightIndex);

        Vector4 lightDisplacement = lightMatrix.times(sceneModel.getCentroid().asPosition());
        float lightDist = lightDisplacement.getXYZ().length();
        float lookAtDist = lightDisplacement.getXY().length();

        float radius =
            sceneModel.getOrientation().times(new Vector3(CLIP_PLANE_SCALE_MULTIPLIER * sceneModel.getScale())).length();

        float fov;
        float farPlane;
        float nearPlane;

        if (sceneModel.getLightingModel().isGroundPlaneEnabled())
        {
            fov = 2.0f * (float) StrictMath.asin(Math.min(0.99, (sceneModel.getScale() + lookAtDist) / lightDist));
            farPlane = lightDist + 2 * sceneModel.getScale();
            nearPlane = Math.max((lightDist + radius) / 32.0f, lightDist - 2 * radius);
        }
        else
        {
            fov = 2.0f * (float) StrictMath.asin(Math.min(0.99, (radius + lookAtDist) / lightDist));
            farPlane = lightDist + radius;
            nearPlane = Math.max(farPlane / 1024.0f, lightDist - 2 * radius);
        }

        // Limit fov by the light's spot size.
        float spotFOV = 2.0f * sceneModel.getLightingModel().getLightPrototype(lightIndex).getSpotSize();
        fov = Math.min(fov, spotFOV);

        return Matrix4.perspective(fov, 1.0f, nearPlane, farPlane);
    }

    public final ReadonlyTexture3D<ContextType> getShadowMaps()
    {
        return shadowMaps;
    }

    public final void refreshShadowMaps()
    {
        // Too many lights; need to re-allocate shadow maps
        if (shadowMaps.getDepth() < sceneModel.getLightingModel().getLightCount())
        {
            shadowMaps.close();
            shadowMaps = null;
            shadowMaps = createShadowMaps();
        }

        for (int lightIndex = 0; lightIndex < sceneModel.getLightingModel().getLightCount(); lightIndex++)
        {
            drawShadowMaps(lightIndex);
        }
    }

    private Texture3D<ContextType> createShadowMaps()
    {
        return context.getTextureFactory().build2DDepthTextureArray(2048, 2048, sceneModel.getLightingModel().getLightCount())
                .setInternalPrecision(32)
                .setFloatingPointEnabled(true)
                .createTexture();
    }

    private void drawShadowMaps(int lightIndex)
    {
        Matrix4 lightProj = getLightProjection(lightIndex);

        shadowProgram.setUniform("projection", lightProj);

        FramebufferAttachment<ContextType> attachment = shadowMaps.getLayerAsFramebufferAttachment(lightIndex);

        shadowFramebuffer.setDepthAttachment(attachment);
        shadowFramebuffer.clearDepthBuffer();

        shadowProgram.setUniform("model_view", sceneModel.getLightModelViewMatrix(lightIndex));
        shadowDrawable.draw(PrimitiveMode.TRIANGLES, shadowFramebuffer);
    }

    public final ReadonlyTexture2D<ContextType> getScreenSpaceDepthTexture()
    {
        return screenSpaceDepthFBO.getDepthAttachmentTexture();
    }

    public final void refreshScreenSpaceDepthFBO(Matrix4 view, Matrix4 projection)
    {
        context.getState().disableBackFaceCulling();

        shadowProgram.setUniform("projection", projection);
        screenSpaceDepthFBO.clearDepthBuffer();

        Matrix4 modelView = sceneModel.getModelViewMatrix(view);

        shadowProgram.setUniform("model_view", modelView);
        shadowProgram.setUniform("viewPos", modelView.quickInverse(0.01f).getColumn(3).getXYZ());
        shadowDrawable.draw(PrimitiveMode.TRIANGLES, screenSpaceDepthFBO);

        context.getState().enableBackFaceCulling();
    }

    @Override
    public final void close()
    {
        if (backplateTexture != null)
        {
            backplateTexture.close();
            backplateTexture = null;
        }

        if (environmentMap != null)
        {
            environmentMap.close();
            environmentMap = null;
        }

        if (shadowMaps != null)
        {
            shadowMaps.close();
            shadowMaps = null;
        }

        if (shadowFramebuffer != null)
        {
            shadowFramebuffer.close();
            shadowFramebuffer = null;
        }

        if (shadowProgram != null)
        {
            shadowProgram.close();
            shadowProgram = null;
        }

        if (shadowDrawable != null)
        {
            shadowDrawable.close();
            shadowDrawable = null;
        }

        if (screenSpaceDepthFBO != null)
        {
            screenSpaceDepthFBO.close();
            screenSpaceDepthFBO = null;
        }
    }
}
