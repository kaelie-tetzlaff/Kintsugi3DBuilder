/*
 * Copyright (c) 2019 - 2026 Seth Berrier, Kaelie Tetzlaff, Jacob Buelow, Luke Denney, Ian Anderson, Zoe Cuthrell, Blane Suess, Isaac Tesch, Nathaniel Willius, Atlas Collins, Simon Cao, Joe Luther, Jakob Schmucki, Nathan Sunday, Joseph O'Connor
 * Copyright (c) 2019 The Regents of the University of Minnesota
 *
 * Licensed under GPLv3
 * ( http://www.gnu.org/licenses/gpl-3.0.html )
 *
 * This code is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package kintsugi3d.gl.opengl;

import java.nio.ByteBuffer;

public class OpenGLTextureData
{
    private final int format;
    private final int type;
    private final ByteBuffer buffer;

    public OpenGLTextureData(int format, int type, ByteBuffer buffer)
    {
        this.format = format;
        this.type = type;
        this.buffer = buffer;
    }

    public int getFormat()
    {
        return format;
    }

    public int getType()
    {
        return type;
    }

    public ByteBuffer getBuffer()
    {
        return buffer;
    }
}
