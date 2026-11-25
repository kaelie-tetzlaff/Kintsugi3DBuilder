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

package kintsugi3d.builder.fit.finalize;

import kintsugi3d.builder.core.texture.StandardTexture;
import kintsugi3d.builder.core.texture.TextureInfo;
import kintsugi3d.builder.core.texture.TextureResolution;
import kintsugi3d.builder.resources.project.texture.ReadonlyTextureSet;
import kintsugi3d.builder.resources.project.texture.TextureResources;
import kintsugi3d.builder.resources.project.texture.TextureSet;
import kintsugi3d.gl.builders.framebuffer.ColorAttachmentSpec;
import kintsugi3d.gl.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class AlbedoORMOptimization<ContextType extends Context<ContextType>> implements AutoCloseable, TextureSet<ContextType>
{
    // Estimation program
    private ProgramObject<ContextType> estimationProgram;

    // Framebuffer for storing the diffuse solution
    private FramebufferObject<ContextType> framebuffer;

    private VertexBuffer<ContextType> rect;
    private final @Nullable DrawableResource<ContextType> drawable;
    private final @Nullable Texture2D<ContextType> occlusionMap;

    private boolean optimizedAlbedo;
    private boolean optimizedMetallicRoughness;

    public static <ContextType extends Context<ContextType>> @NotNull AlbedoORMOptimization<ContextType> createWithOcclusion(
        @NotNull Texture2D<ContextType> occlusionMap, @NotNull TextureResolution settings)
        throws IOException
    {
        return new AlbedoORMOptimization<>(occlusionMap.getContext(), occlusionMap, settings);
    }

    public static <ContextType extends Context<ContextType>> @NotNull AlbedoORMOptimization<ContextType> createWithoutOcclusion(
        @NotNull ContextType context, @NotNull TextureResolution resolution)
        throws IOException
    {
        return new AlbedoORMOptimization<>(context, null, resolution);
    }

    private AlbedoORMOptimization(@NotNull ContextType context, @Nullable Texture2D<ContextType> occlusionMap, @NotNull TextureResolution settings)
        throws IOException
    {
        this.occlusionMap = occlusionMap;
        estimationProgram = createProgram(context, occlusionMap != null);
        framebuffer = context.buildFramebufferObject(settings.width, settings.height)
            .addColorAttachment(ColorAttachmentSpec.createWithInternalFormat(ColorFormat.RGBA8)
                .setLinearFilteringEnabled(true)) // total albedo
            .addColorAttachment(ColorAttachmentSpec.createWithInternalFormat(ColorFormat.RGBA8)
                .setLinearFilteringEnabled(true)) // ORM
            .addColorAttachment(ColorAttachmentSpec.createWithInternalFormat(ColorFormat.RGBA8)
                .setLinearFilteringEnabled(true)) // metallic
            .createFramebufferObject();

        // Create basic rectangle vertex buffer
        rect = context.createRectangle();
        drawable = context.createDrawable(estimationProgram);
        drawable.setDefaultPrimitiveMode(PrimitiveMode.TRIANGLE_FAN);
        drawable.addVertexBuffer("position", rect);
    }

    public static <ContextType extends Context<ContextType>> @NotNull AlbedoORMOptimization<ContextType> loadFromPriorSolution(
        @NotNull ContextType context, File priorSolutionDirectory) throws IOException
    {
        return new AlbedoORMOptimization<>(context, priorSolutionDirectory);
    }

    private AlbedoORMOptimization(@NotNull ContextType context, File priorSolutionDirectory)
        throws IOException
    {
        // Load albedo and ORM maps
        Texture2D<ContextType> albedoMap = TextureResources.loadTexture(StandardTexture.ALBEDO, priorSolutionDirectory, context);
        Texture2D<ContextType> ormMap = TextureResources.loadTexture(StandardTexture.ORM, priorSolutionDirectory, context);
        Texture2D<ContextType> metallicMap = TextureResources.loadTexture(StandardTexture.METALLIC, priorSolutionDirectory, context);

        if (TextureResources.getTextureFile(StandardTexture.OCCLUSION, priorSolutionDirectory).exists())
        {
            // Load occlusion map if it exists (to pack it into the red channel of ORM if reoptimizing)
            occlusionMap = TextureResources.loadTexture(StandardTexture.OCCLUSION, priorSolutionDirectory, context);
        }
        else
        {
            occlusionMap = null;
        }

        if (albedoMap != null)
        {
            optimizedAlbedo = true;
            framebuffer =
                context.buildFramebufferObject(albedoMap.getWidth(), albedoMap.getHeight())
                    .addEmptyColorAttachment() // Will copy in albedo map after FBO is created
                    .addColorAttachment(ColorAttachmentSpec.createWithInternalFormat(ColorFormat.RGBA8)
                        .setLinearFilteringEnabled(true)) // Will blit in ORM map after FBO is created
                    .addColorAttachment(ColorAttachmentSpec.createWithInternalFormat(ColorFormat.RGBA8)
                        .setLinearFilteringEnabled(true)) // metallic
                    .createFramebufferObject();
            framebuffer.setColorAttachment(0, albedoMap);

            if (ormMap != null)
            {
                optimizedMetallicRoughness = true;
                framebuffer.getColorAttachmentTexture(1).blitScaled(ormMap, true);
                if (metallicMap != null)
                {
                    framebuffer.getColorAttachmentTexture(2).blitScaled(metallicMap, true);
                }
                else
                {
                    extractMetallicFromOrm(context, ormMap);

                    // Save to disk so that it's accessible for the details panel.
                    framebuffer.getColorAttachmentTexture(2).getColorTextureReader().saveToFile(
                        "PNG", TextureResources.getTextureFile(StandardTexture.METALLIC, priorSolutionDirectory));
                }
            }

            estimationProgram = createProgram(context, occlusionMap != null);

            // Create basic rectangle vertex buffer
            rect = context.createRectangle();
            drawable = context.createDrawable(estimationProgram);
            drawable.setDefaultPrimitiveMode(PrimitiveMode.TRIANGLE_FAN);
            drawable.addVertexBuffer("position", rect);
        }
        else
        {
            drawable = null;
        }
    }

    public void execute(@NotNull ReadonlyTextureSet<ContextType> specularFit)
    {
        // Set up shader program
        estimationProgram.setTexture("diffuseEstimate", specularFit.getTexture(StandardTexture.DIFFUSE_COLOR));
        estimationProgram.setTexture("tex_specular", specularFit.getTexture(StandardTexture.SPECULAR_COLOR));
        estimationProgram.setTexture("roughnessEstimate", specularFit.getTexture(StandardTexture.ROUGHNESS));

        if (specularFit.getTexture("constant") != null)
        {
            estimationProgram.setTexture("constantEstimate", specularFit.getTexture("constant"));
        }

        if (occlusionMap != null)
        {
            estimationProgram.setTexture("occlusionTexture", occlusionMap);
        }

        // Estimate albedo and roughness; passthrough occlusion if it is present
        framebuffer.clearColorBuffer(0, 0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clearColorBuffer(1, 0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clearColorBuffer(2, 0.0f, 0.0f, 0.0f, 0.0f);
        Objects.requireNonNull(drawable).draw(framebuffer);

        optimizedAlbedo = true;
        optimizedMetallicRoughness = true;
    }

    @Override
    public void close()
    {
        if (estimationProgram != null)
        {
            estimationProgram.close();
        }

        if (drawable != null)
        {
            drawable.close();
        }

        if (framebuffer != null)
        {
            framebuffer.close();
        }

        if (rect != null)
        {
            rect.close();
        }

        if (occlusionMap != null)
        {
            occlusionMap.close();
        }
    }

    public @Nullable Texture2D<ContextType> getAlbedoMap()
    {
        return framebuffer == null ? null : framebuffer.getColorAttachmentTexture(0);
    }

    public @Nullable Texture2D<ContextType> getORMMap()
    {
        return framebuffer == null ? null : framebuffer.getColorAttachmentTexture(1);
    }

    public int getTextureCount()
    {
        return getStandardTextures().size();
    }

    @Override
    public @NotNull Map<StandardTexture, Texture2D<ContextType>> getStandardTextures()
    {
        Map<StandardTexture, Texture2D<ContextType>> textures = new EnumMap<>(StandardTexture.class);

        if (framebuffer != null)
        {
            if (optimizedAlbedo)
            {
                textures.put(StandardTexture.ALBEDO, framebuffer.getColorAttachmentTexture(0));
            }

            if (optimizedMetallicRoughness)
            {
                textures.put(StandardTexture.ORM, framebuffer.getColorAttachmentTexture(1));
                textures.put(StandardTexture.METALLIC, framebuffer.getColorAttachmentTexture(2));
            }
        }

        if (occlusionMap != null)
        {
            textures.put(StandardTexture.OCCLUSION, occlusionMap);
        }

        return Collections.unmodifiableMap(textures);
    }

    @Override
    public @NotNull Map<TextureInfo, Texture2D<ContextType>> getTextures()
    {
        return StandardTexture.convertEnumMapToObjectMap(getStandardTextures());
    }

    private Texture2D<ContextType> extractMetallicFromOrm(@NotNull ContextType context, ReadonlyTexture2D<ContextType> orm) throws IOException
    {
        ProgramObject<ContextType> program = context.getShaderProgramBuilder()
            .addShader(ShaderType.VERTEX, new File("shaders/common/texture.vert"))
            .addShader(ShaderType.FRAGMENT, new File("shaders/specularfit/extractMetallic.frag"))
            .createProgram();

        program.setTexture("orm", orm);

        VertexBuffer<ContextType> metallicRect = context.createRectangle();
        DrawableResource<ContextType> metallicDrawable = context.createDrawable(program);
        metallicDrawable.setDefaultPrimitiveMode(PrimitiveMode.TRIANGLE_FAN);
        metallicDrawable.addVertexBuffer("position", metallicRect);
        framebuffer.clearColorBuffer(2, 0.0f, 0.0f, 0.0f, 0.0f);
        metallicDrawable.draw(framebuffer);

        return framebuffer.getColorAttachmentTexture(2);
    }

    private static <ContextType extends Context<ContextType>>
    ProgramObject<ContextType> createProgram(@NotNull ContextType context, boolean occlusionTextureEnabled) throws IOException
    {
        return context.getShaderProgramBuilder()
            .addShader(ShaderType.VERTEX, new File("shaders/common/texture.vert"))
            .addShader(ShaderType.FRAGMENT, new File("shaders/specularfit/estimateAlbedoORM.frag"))
            .define("TEXTURE_OCCLUSION", occlusionTextureEnabled)
            .createProgram();
    }
}
