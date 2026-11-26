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

import kintsugi3d.builder.core.texture.NamedTextureInfo;
import kintsugi3d.builder.core.texture.StandardTexture;
import kintsugi3d.builder.core.texture.TextureInfo;
import kintsugi3d.builder.core.texture.TextureResolution;
import kintsugi3d.builder.fit.SpecularFitOptimizable;
import kintsugi3d.gl.interactive.ShaderResources;
import kintsugi3d.gl.interactive.ShaderProgramFactory;
import kintsugi3d.gl.builders.framebuffer.FramebufferObjectBuilder;
import kintsugi3d.gl.core.*;
import kintsugi3d.util.ShaderHoleFill;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class FinalDiffuseOptimization<ContextType extends Context<ContextType>> implements ManagedResource
{
    private static final Logger LOG = LoggerFactory.getLogger(FinalDiffuseOptimization.class);

    public static final TextureInfo CONSTANT_TRANSLUCENCY_MAP =
        new NamedTextureInfo("constant", "Constant translucency map",
            "An experimental texture for translucency fitting.");

    // Graphics context
    private final @NotNull ContextType context;

    // Final diffuse estimation program
    private final ProgramObject<ContextType> estimationProgram;
    private final @NotNull TextureResolution textureResolution;

    private final boolean includeConstant;

    // Framebuffer for storing the diffuse solution
    private FramebufferObject<ContextType> framebuffer;

    private final DrawableResource<ContextType> drawable;

    public FinalDiffuseOptimization(
        @NotNull ShaderResources<ContextType> resources,
        @NotNull TextureResolution textureResolution, boolean includeConstant)
        throws IOException
    {
        context = resources.getContext();
        estimationProgram = includeConstant ?
            createDiffuseTranslucentEstimationProgram(resources)
            : createDiffuseEstimationProgram(resources);
        this.textureResolution = textureResolution;
        this.includeConstant = includeConstant;

        framebuffer = createFramebuffer(context, textureResolution, includeConstant);
        drawable = resources.createDrawable(estimationProgram);
    }

    private static <ContextType extends Context<ContextType>> FramebufferObject<ContextType> createFramebuffer(
        @NotNull ContextType context, @NotNull TextureResolution texSettings, boolean includeConstant)
    {
        FramebufferObjectBuilder<ContextType> builder = context
            .buildFramebufferObject(texSettings.width, texSettings.height)
            .addColorAttachment(ColorFormat.RGBA8);

        if (includeConstant)
        {
            builder.addColorAttachment(ColorFormat.RGBA8); // Add attachment for storing constant term texture
//            builder.addColorAttachment(ColorFormat.RGBA8); // Add attachment for storing quadratic term texture
        }

        return builder.createFramebufferObject();
    }

    public final void execute(@NotNull SpecularFitOptimizable<ContextType> specularFit)
    {
        // Set up diffuse estimation shader program
        specularFit.validateAndUseAllBasisResources(estimationProgram);
        estimationProgram.setTexture("tex_normal", specularFit.getTexture(StandardTexture.NORMAL_MAP));
        estimationProgram.setTexture("tex_roughness", specularFit.getTexture(StandardTexture.ROUGHNESS));

        // Second framebuffer for filling holes (used to double-buffer the first framebuffer)
        // Placed outside of try-with-resources since it might end up being the primary framebuffer after filling holes.
        FramebufferObject<ContextType> framebuffer2 = createFramebuffer(context, textureResolution, includeConstant);

        // Will reference the framebuffer that is in front after hole filling if everything is successful.
        FramebufferObject<ContextType> finalDiffuse = null;

        framebuffer.clearColorBuffer(0, 0.0f, 0.0f, 0.0f, 0.0f);

        if (includeConstant)
        {
            framebuffer.clearColorBuffer(1, 0.0f, 0.0f, 0.0f, 0.0f);
            framebuffer.clearColorBuffer(2, 0.0f, 0.0f, 0.0f, 0.0f);
        }

        // Perform diffuse fit
        drawable.draw(framebuffer);

        try (ShaderHoleFill<ContextType> holeFill = new ShaderHoleFill<>(context))
        {
            // Fill holes
            finalDiffuse = holeFill.execute(framebuffer, framebuffer2);
        }
        catch (IOException e)
        {
            LOG.error("An error occurred while filling holes:", e);
        }
        finally
        {
            if (Objects.equals(finalDiffuse, framebuffer2))
            {
                // New framebuffer is the front framebuffer after filling holes;
                // close the old one and make the new one the primary framebuffer
                framebuffer.close();
                framebuffer = framebuffer2;
            }
            else
            {
                // New framebuffer is the back framebuffer after filling holes (or an exception occurred);
                // either way; just close it and leave the primary framebuffer the same.
                framebuffer2.close();
            }
        }
    }

    @Override
    public final void close()
    {
        estimationProgram.close();
        drawable.close();
        framebuffer.close();
    }

    public final Texture2D<ContextType> getDiffuseMap()
    {
        return framebuffer.getColorAttachmentTexture(0);
    }

    public final @NotNull Map<TextureInfo, Texture2D<ContextType>> getNonStandardTextures()
    {
        return includeConstant ? Map.of(CONSTANT_TRANSLUCENCY_MAP, framebuffer.getColorAttachmentTexture(1)) : Map.of();
    }

    private static <ContextType extends Context<ContextType>>
    ProgramObject<ContextType> createDiffuseEstimationProgram(@NotNull ShaderProgramFactory<ContextType> programFactory) throws IOException
    {
        return programFactory.createProgram(
            new File("shaders/common/texspace_dynamic.vert"), new File("shaders/specularfit/estimateDiffuse.frag"));
    }
    private static <ContextType extends Context<ContextType>>
    ProgramObject<ContextType> createDiffuseTranslucentEstimationProgram(@NotNull ShaderProgramFactory<ContextType> programFactory) throws IOException
    {
        return programFactory.createProgram(
            new File("shaders/common/texspace_dynamic.vert"), new File("shaders/specularfit/estimateDiffuseTranslucent.frag"));
    }
}
