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

package kintsugi3d.builder.resources.project;

import kintsugi3d.builder.core.viewset.DistortionProjection;
import kintsugi3d.builder.core.viewset.Projection;
import kintsugi3d.builder.core.viewset.ReadonlyViewSet;
import kintsugi3d.builder.core.viewset.View;
import kintsugi3d.builder.io.ReadonlyLoadOptionsModel;
import kintsugi3d.builder.util.ImageUndistorter;
import kintsugi3d.gl.builders.ProgramBuilder;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.geometry.GeometryResources;
import kintsugi3d.gl.geometry.ReadonlyVertexGeometry;
import kintsugi3d.gl.vecmath.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * For use i.e. with projtex_single.frag
 */
public class SingleCalibratedImageResource<ContextType extends Context<ContextType>> implements ManagedResource
{
    private final @NotNull View view;
    private final @NotNull GeometryResources<ContextType> geometryResources;
    private final boolean geometryResourcesOwned;
    private final @Nullable Texture2D<ContextType> colorTexture;
    private final @Nullable Texture2D<ContextType> depthTexture;
    private final @Nullable Texture2D<ContextType> shadowTexture;
    private final @Nullable Matrix4 shadowMatrix;

    /**
     * Creates a calibrated image resource, loading the geometry onto the GPU as a new resource owned by this resource (if and only if geometry is not null).
     * @param context
     * @param view
     * @param geometry
     * @param loadOptions
     * @throws IOException
     */
    SingleCalibratedImageResource(@NotNull ContextType context, @NotNull View view, @Nullable ReadonlyVertexGeometry geometry,
                                  @NotNull ReadonlyLoadOptionsModel loadOptions) throws IOException
    {
        this(context, view,
            geometry == null ? GeometryResources.createNullResources() : geometry.createGraphicsResources(context),
            true, loadOptions);
    }

    private SingleCalibratedImageResource(@NotNull ContextType context, @NotNull View view,
                                          @NotNull GeometryResources<ContextType> geometryResources, boolean geometryResourcesOwned, @NotNull ReadonlyLoadOptionsModel loadOptions) throws IOException
    {
        this.view = view;
        this.geometryResources = geometryResources;
        this.geometryResourcesOwned = geometryResourcesOwned;

        File imageFile = view.findFullResImageFile();

        // Read the images from a file
        if (loadOptions.areColorImagesRequested())
        {
            File mask = view.getMaskFile();
            var colorTextureBuilder = mask == null ?
                context.getTextureFactory().build2DColorTextureFromFile(imageFile, true) :
                context.getTextureFactory().build2DColorTextureFromFileWithMask(imageFile, mask, true);

            Projection projection = view.getCameraProjection();
            if (projection instanceof DistortionProjection)
            {
                DistortionProjection distortion = (DistortionProjection) projection;

                try (ImageUndistorter<ContextType> undistorter = new ImageUndistorter<>(context);
                     Texture2D<ContextType> distortedTexture = colorTextureBuilder.createTexture())
                {
                    colorTexture = undistorter.undistort(distortedTexture, distortion);
                }
            }
            else
            {
                colorTexture = colorTextureBuilder.createTexture();
            }
        }
        else
        {
            colorTexture = null;
        }

        if (geometryResources.isNull() || colorTexture == null || !loadOptions.areDepthImagesRequested())
        {
            depthTexture = null;
            shadowTexture = null;
            shadowMatrix = null;
        }
        else
        {
            try
            (
                // Don't automatically generate any texture attachments for this framebuffer object
                FramebufferObject<ContextType> depthRenderingFBO =
                    context.buildFramebufferObject(colorTexture.getWidth(), colorTexture.getHeight())
                        .createFramebufferObject();
                DepthMapGenerator<ContextType> depthMapGenerator = DepthMapGenerator.createFromGeometryResources(geometryResources)
            )
            {

                // Build depth texture
                depthTexture = context.getTextureFactory()
                    .build2DDepthTexture(colorTexture.getWidth(), colorTexture.getHeight())
                    .createTexture();

                depthRenderingFBO.setDepthAttachment(depthTexture);
                depthMapGenerator.generateDepthMap(view, depthRenderingFBO);

                // Build shadow texture
                shadowTexture = context.getTextureFactory()
                    .build2DDepthTexture(colorTexture.getWidth(), colorTexture.getHeight())
                    .createTexture();

                depthRenderingFBO.setDepthAttachment(shadowTexture);
                shadowMatrix = depthMapGenerator.generateShadowMap(view, depthRenderingFBO);
            }
        }
    }

    public static <ContextType extends Context<ContextType>> ProgramBuilder<ContextType> getShaderProgramBuilder(
        ContextType context, @NotNull ReadonlyViewSet viewSet, @NotNull ReadonlyLoadOptionsModel loadOptions)
    {
        ProgramBuilder<ContextType> builder = viewSet.getShaderProgramBuilder(context);

        if (viewSet.getGeometryFile() == null || !loadOptions.areDepthImagesRequested())
        {
            // Disable visibility and shadow test if not generating depth images for this resource.
            builder.define("VISIBILITY_TEST_ENABLED", false).define("SHADOW_TEST_ENABLED", false);
        }

        return builder;
    }

    public final void setupShaderProgram(@NotNull Program<ContextType> program)
    {
        view.getContainingViewSet().setupShaderProgram(program);

        if (colorTexture != null)
        {
            program.setTexture("viewImage", colorTexture);
        }

        if (depthTexture != null)
        {
            program.setTexture("depthImage", depthTexture);

            if (shadowTexture != null)
            {
                program.setTexture("shadowImage", shadowTexture);
                program.setUniform("shadowMatrix", shadowMatrix);
            }
        }

        program.setUniform("cameraPose", view.getCameraPose());
        program.setUniform("cameraProjection", view.getProjectionMatrix());
        program.setUniform("lightPosition", view.getLightPosition());
        program.setUniform("lightIntensity", view.getLightIntensity());
    }

    /**
     * Creates a Drawable using this instance's geometry resources, and the specified shader program.
     * @param program The program to use to construct the Drawable.
     * @return A Drawable for rendering this instance using the specified shader program.
     */
    public final @NotNull DrawableResource<ContextType> createDrawable(@NotNull Program<ContextType> program)
    {
        return geometryResources.createDrawable(program);
    }

    @Override
    public final void close()
    {
        if (geometryResourcesOwned)
        {
            geometryResources.close();
        }

        if (colorTexture != null)
        {
            colorTexture.close();
        }

        if (depthTexture != null)
        {
            depthTexture.close();
        }

        if (shadowTexture != null)
        {
            shadowTexture.close();
        }
    }
}
