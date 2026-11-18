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

package kintsugi3d.gl.material;

import kintsugi3d.gl.vecmath.Vector3;

public class MaterialTextureMap implements ReadonlyMaterialTextureMap
{
    private String mapName;

    private boolean clampingRequired;
    private float base;
    private float gain;
    private Vector3 offset;
    private Vector3 scale;

    public MaterialTextureMap()
    {
        clampingRequired = false;
        base = 0.0f;
        gain = 1.0f;
        offset = new Vector3(0.0f);
        scale = new Vector3(1.0f);
    }

    @Override
    public final String getMapName()
    {
        return mapName;
    }

    public final void setMapName(String mapName)
    {
        this.mapName = mapName;
    }

    @Override
    public final boolean isClampingRequired()
    {
        return clampingRequired;
    }

    public final void setClampingRequired(boolean clampingEnabled)
    {
        clampingRequired = clampingEnabled;
    }

    @Override
    public final float getBase()
    {
        return base;
    }

    public final void setBase(float base)
    {
        this.base = base;
    }

    @Override
    public final float getGain()
    {
        return gain;
    }

    public final void setGain(float gain)
    {
        this.gain = gain;
    }

    @Override
    public final Vector3 getOffset()
    {
        return offset;
    }

    public final void setOffset(Vector3 offset)
    {
        this.offset = offset;
    }

    @Override
    public final Vector3 getScale()
    {
        return scale;
    }

    public final void setScale(Vector3 scale)
    {
        this.scale = scale;
    }
}
