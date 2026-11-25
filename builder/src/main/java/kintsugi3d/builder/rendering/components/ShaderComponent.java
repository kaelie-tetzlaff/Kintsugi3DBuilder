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

package kintsugi3d.builder.rendering.components;

import kintsugi3d.builder.rendering.RenderedComponent;
import kintsugi3d.builder.rendering.SceneViewportModel;
import kintsugi3d.gl.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public abstract class ShaderComponent<ContextType extends Context<ContextType>> implements RenderedComponent<ContextType>
{
    private static final Logger LOG = LoggerFactory.getLogger(ShaderComponent.class);

    private final ContextType context;
    private Map<String, VertexBuffer<ContextType>> vertexBuffers;
    private final Collection<ManagedResource> otherManagedResources = new ArrayList<>(8);
    private @Nullable ProgramObject<ContextType> program;
    private @Nullable DrawableResource<ContextType> drawable;

    private final SceneViewportModel sceneViewportModel;

    private final String sceneObjectTag;

    /**
     * Creates a shader component using object ID managed by scene viewport model and scene object tag for framebuffer writes
     * @param context
     * @param sceneViewportModel
     * @param sceneObjectTag
     */
    protected ShaderComponent(ContextType context, @Nullable SceneViewportModel sceneViewportModel, @Nullable String sceneObjectTag)
    {
        this.context = context;
        this.sceneViewportModel = sceneViewportModel;
        this.sceneObjectTag = sceneObjectTag;

        if (this.sceneViewportModel != null && this.sceneObjectTag != null)
        {
            this.sceneViewportModel.addSceneObjectType(sceneObjectTag);
        }
    }

    /**
     * Creates a shader component using object ID 0 for framebuffer writes
     * @param context
     */
    protected ShaderComponent(ContextType context)
    {
        this(context, null, null);
    }

    protected final String getSceneObjectTag()
    {
        return sceneObjectTag;
    }

    @Override
    public final void initialize()
    {
        vertexBuffers = createVertexBuffers();
        reloadShaders();
        onInitialize();
    }

    /**
     * Override to specify behavior that should happen immediately after initialization
     * (i.e. after vertex buffer construction and shader compilation).
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void onInitialize()
    {
    }

    @Override
    public final void reloadShaders()
    {
        try
        {
            ProgramObject<ContextType> newProgram = createProgram();

            if (program != null)
            {
                program.close();
            }

            program = newProgram;

            if (sceneViewportModel != null && sceneObjectTag != null)
            {
                program.setUniform("objectID", sceneViewportModel.lookupSceneObjectID(sceneObjectTag));
            }
            else
            {
                program.setUniform("objectID", 0);
            }

            if (drawable != null)
            {
                drawable.close();
            }

            drawable = createDrawable(program);

            for (var entry : vertexBuffers.entrySet())
            {
                drawable.addVertexBuffer(entry.getKey(), entry.getValue());
            }
        }
        catch (IOException | RuntimeException e)
        {
            LOG.error("Failed to load shader.", e);
        }
    }

    @Override
    public final void close()
    {
        if (program != null)
        {
            program.close();
            program = null;
        }

        if (drawable != null)
        {
            drawable.close();
            drawable = null;
        }

        for (VertexBuffer<ContextType> vertexBuffer : vertexBuffers.values())
        {
            vertexBuffer.close();
        }

        for (ManagedResource managedResource : otherManagedResources)
        {
            managedResource.close();
        }
    }

    public final ContextType getContext()
    {
        return context;
    }

    public final @NotNull DrawableResource<ContextType> getDrawable()
    {
        return Objects.requireNonNull(drawable, "Shader component does not have a valid drawable.");
    }

    public final @Nullable DrawableResource<ContextType> getDrawableOrNull()
    {
        return drawable;
    }

    public final Program<ContextType> getProgram()
    {
        return getDrawable().program();
    }

    /**
     * Override to provide shader program.
     * Shader program is automatically managed and does not need to manually be added as a resource.
     * @return
     */
    protected abstract ProgramObject<ContextType> createProgram() throws IOException;

    /**
     * Override to provide vertex buffers.
     * Vertex buffers are automatically managed and do not need to manually be added as resources.
     * @return
     */
    protected abstract Map<String, VertexBuffer<ContextType>> createVertexBuffers();

    /**
     * Override to customize drawable creation.
     */
    @SuppressWarnings("DesignForExtension")
    protected @NotNull DrawableResource<ContextType> createDrawable(Program<ContextType> drawableProgram)
    {
        return context.createDrawable(drawableProgram);
    }

    protected final <ResourceType extends ManagedResource> ResourceType resource(ResourceType resource)
    {
        otherManagedResources.add(resource);
        return resource;
    }
}
