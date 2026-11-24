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

package kintsugi3d.gl.geometry;

import kintsugi3d.gl.core.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GeometryResources<ContextType extends Context<ContextType>>
    implements ReadonlyGeometryResources<ContextType>, ManagedResource
{
    private static final Logger LOG = LoggerFactory.getLogger(GeometryResources.class);

    private final @Nullable ContextType context;

    private final @Nullable ReadonlyVertexGeometry geometry;

    /**
     * A vertex buffer containing vertex positions.
     */
    private final @Nullable VertexBuffer<ContextType> positionBuffer;

    /**
     * A vertex buffer containing texture coordinates.
     */
    private final @Nullable VertexBuffer<ContextType> texCoordBuffer;

    /**
     * A vertex buffer containing surface normals.
     */
    private final @Nullable VertexBuffer<ContextType> normalBuffer;

    /**
     * A vertex buffer containing tangent vectors.
     */
    private final @Nullable VertexBuffer<ContextType> tangentBuffer;

    /**
     * Default constructor: create null object
     */
    private GeometryResources()
    {
        context = null;
        geometry = null;
        positionBuffer = null;
        texCoordBuffer = null;
        normalBuffer = null;
        tangentBuffer = null;
    }

    public static <ContextType extends Context<ContextType>> GeometryResources<ContextType> createNullResources()
    {
        return new GeometryResources<>();
    }

    /**
     * package-visible constructor, called by VertexGeometry.createGraphicsResources
     */
    GeometryResources(ContextType context, ReadonlyVertexGeometry geometry)
    {
        this.context = context;
        this.geometry = geometry;
        positionBuffer = context.createVertexBuffer().setData(geometry.getVertices());

        if (geometry.hasTexCoords())
        {
            texCoordBuffer = context.createVertexBuffer().setData(geometry.getTexCoords());
        }
        else
        {
            texCoordBuffer = null;
        }

        if (geometry.hasNormals())
        {
            normalBuffer = context.createVertexBuffer().setData(geometry.getNormals());
        }
        else
        {
            normalBuffer = null;
        }

        if (geometry.hasTexCoords() && geometry.hasNormals())
        {
            tangentBuffer = context.createVertexBuffer().setData(geometry.getTangents());
        }
        else
        {
            tangentBuffer = null;
        }
    }

    @Override
    public final @Nullable ContextType getContext()
    {
        return context;
    }

    @Override
    public final @Nullable ReadonlyVertexGeometry getGeometry()
    {
        return geometry;
    }

    @Override
    public final ReadonlyVertexBuffer<ContextType> getPositionBuffer()
    {
        return positionBuffer;
    }

    @Override
    public final DrawableResource<ContextType> createDrawable(Program<ContextType> program)
    {
        DrawableResource<ContextType> drawable = program.getContext().createDrawable(program);
        drawable.addVertexBuffer("position", positionBuffer);
        drawable.addVertexBuffer("texCoord", texCoordBuffer);
        drawable.addVertexBuffer("normal", normalBuffer);
        drawable.addVertexBuffer("tangent", tangentBuffer);
        return drawable;
    }

    @Override
    public final GeometryFramebuffer<ContextType> createGeometryFramebuffer(int width, int height)
    {
        try
        {
            return new GeometryFramebuffer<>(this, width, height);
        }
        catch (IOException e)
        {
            LOG.error("File not found exception while trying to create a geometryFrameBuffer:", e);
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public final boolean isNull()
    {
        return positionBuffer == null;
    }

    @Override
    public final void close()
    {
        if (positionBuffer != null)
        {
            positionBuffer.close();
        }

        if (texCoordBuffer != null)
        {
            texCoordBuffer.close();
        }

        if (normalBuffer != null)
        {
            normalBuffer.close();
        }

        if (tangentBuffer != null)
        {
            tangentBuffer.close();
        }
    }
}
