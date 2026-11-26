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

import kintsugi3d.builder.core.texture.TextureResolution;
import kintsugi3d.builder.core.viewset.View;
import kintsugi3d.builder.io.TextureLoadOptions;
import kintsugi3d.gl.builders.ColorTextureBuilder;
import kintsugi3d.gl.builders.ProgramBuilder;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.geometry.GeometryMode;
import kintsugi3d.gl.geometry.GeometryTextures;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.UserCancellationException;
import kintsugi3d.util.ImageFinder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.function.Supplier;

public class GraphicsResourcesTextureSpace<ContextType extends Context<ContextType>> extends ImageBasedGraphicsResourcesBase<ContextType>
{
    private static final Logger LOG = LoggerFactory.getLogger(GraphicsResourcesTextureSpace.class);
    /**
     * Array of image data pre-projected into texture space
     */
    private final Texture3D<ContextType> textureArray;

    /**
     * Simple rectangle for draw calls
     */
    private final VertexBuffer<ContextType> rectangle;

    /**
     * Textures storing pre-computed geometry information per-texel
     */
    private final GeometryTextures<ContextType> geometryTextures;

    private final int texWidth;
    private final int texHeight;

    /**
     *
     * @param sharedResources
     * @param geometryTextureFactory This instance will take ownership of the textures produced
     * @param textureDirectory
     * @param loadOptions
     * @param texWidth
     * @param texHeight
     * @param progressMonitor
     * @throws IOException
     */
    GraphicsResourcesTextureSpace(@NotNull GraphicsResourcesCommon<ContextType> sharedResources,
                                  @NotNull Supplier<GeometryTextures<ContextType>> geometryTextureFactory, File textureDirectory,
                                  @NotNull TextureLoadOptions loadOptions, int texWidth, int texHeight, @NotNull ProgressMonitor progressMonitor)
            throws IOException, UserCancellationException
    {
        super(sharedResources, false);

        this.texWidth = texWidth;
        this.texHeight = texHeight;

        Date timestamp = new Date();

        ColorTextureBuilder<ContextType, ? extends Texture3D<ContextType>> builder = getContext().getTextureFactory()
                .build2DColorTextureArray(texWidth, texHeight, getViewSet().getGPUBufferSize())
                .setLinearFilteringEnabled(loadOptions.isLinearFilteringRequested())
                .setMipmapsEnabled(loadOptions.areMipmapsRequested());

        if (loadOptions.isCompressionRequested())
        {
            textureArray = builder
                .setInternalFormat(CompressionFormat.RGB_PUNCHTHROUGH_ALPHA1_4BPP)
                .createTexture();
        }
        else
        {
            textureArray = builder
                .setInternalFormat(ColorFormat.RGBA8)
                .createTexture();
        }

        progressMonitor.setMaxProgress(getViewSet().getViewCount());
        progressMonitor.setStage(0, "Loading textures...");

        try
        {
            // Iterate over the layers to load in the texture array.
            // Includes both enabled and disabled views which will all be allocated on the GPU.
            int progressCount = 0;
            for (View view : getViewSet().getViews())
            {
                progressMonitor.setProgress(progressCount, MessageFormat.format("{0} ({1}/{2})",
                    view, progressCount + 1, getViewSet().getViewCount()));
                progressMonitor.allowUserCancellation();

                textureArray.loadLayer(view.getGPUViewIndex(),
                    ImageFinder.findImageFile(new File(textureDirectory, view.getImageFile().getName())),
                    true);

                progressCount++;
            }

            progressMonitor.setProgress(getViewSet().getViewCount(), "All images loaded.");
        }
        catch (IOException e)
        {
            // Release any resources prior to rethrowing the exception
            super.close();
            textureArray.close();
            throw e;
        }

        LOG.info("View Set textures loaded in {} milliseconds.", new Date().getTime() - timestamp.getTime());

        progressMonitor.setStage(1, "Finished loading textures.");

        geometryTextures = geometryTextureFactory.get();
        rectangle = sharedResources.getContext().createRectangle();
    }

    @Override
    public final ProgramBuilder<ContextType> getShaderProgramBuilder()
    {
        // Determine shader defines here that do not typically change
        // and should apply globally as defaults, but only for texture-space source photos.
        // The shader will not reload automatically when these change.
        // The defines can be overridden by the actual shader.
        return getCommonResources().getShaderProgramBuilder()
            .define("GEOMETRY_TEXTURES_ENABLED", true)
            .define("GEOMETRY_MODE", GeometryMode.RECTANGLE)
            .define("COLOR_APPEARANCE_MODE", ColorAppearanceMode.TEXTURE_SPACE);
    }

    @Override
    public final void setupShaderProgram(@NotNull Program<ContextType> program)
    {
        getCommonResources().setupShaderProgram(program);
        geometryTextures.setupShaderProgram(program);
        program.setTexture("viewImages", textureArray);
    }

    @Override
    public final @NotNull DrawableResource<ContextType> createDrawable(Program<ContextType> program)
    {
        DrawableResource<ContextType> drawable = getCommonResources().getContext().createDrawable(program);
        drawable.addVertexBuffer("position", rectangle);
        drawable.setDefaultPrimitiveMode(PrimitiveMode.TRIANGLE_FAN);
        return drawable;
    }

    public final @NotNull TextureResolution getTextureResolution()
    {
        return new TextureResolution(texWidth, texHeight);
    }

    @Override
    public final void close()
    {
        super.close();

        geometryTextures.close();
        rectangle.close();
        textureArray.close();
    }
}
