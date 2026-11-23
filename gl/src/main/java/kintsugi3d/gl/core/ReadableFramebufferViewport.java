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

package kintsugi3d.gl.core;

import kintsugi3d.gl.vecmath.IntVector2;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

class ReadableFramebufferViewport<ContextType extends Context<ContextType>>
    extends FramebufferViewportBase<ContextType>
    implements FramebufferViewport<ContextType>
{
    private static class ReadContents<ContextType extends Context<ContextType>> implements FramebufferReadContents<ContextType>
    {
        private final ReadableFramebufferViewport<ContextType> framebufferViewport;

        private final FramebufferReadContents<ContextType> fullFramebufferContents;

        ReadContents(ReadableFramebufferViewport<ContextType> framebufferViewport, FramebufferReadContents<ContextType> fullFramebufferContents)
        {
            this.framebufferViewport = framebufferViewport;
            this.fullFramebufferContents = fullFramebufferContents;
        }

        @Override
        public final ContextType getContext()
        {
            return framebufferViewport.getContext();
        }

        @Override
        public final FramebufferSize getSize()
        {
            return framebufferViewport.getSizeForRead();
        }

        @Override
        public final void bindForRead(int attachmentIndex)
        {
            fullFramebufferContents.bindForRead(attachmentIndex);
        }

        @Override
        public final void bindNonColorAttachmentForRead()
        {
            fullFramebufferContents.bindNonColorAttachmentForRead();
        }
    }

    private final ReadableFramebuffer<ContextType> framebuffer;

    ReadableFramebufferViewport(
        ReadableFramebuffer<ContextType> fullFramebuffer, IntVector2 viewportOffset, FramebufferSize viewportSize)
    {
        super(fullFramebuffer, viewportOffset, viewportSize);
        framebuffer = fullFramebuffer;
    }

    @Override
    public final FramebufferSize getSizeForRead()
    {
        return getSize();
    }

    @Override
    public final FramebufferReadContents<ContextType> getReadContents()
    {
        return new ReadContents<>(this, framebuffer.getReadContents());
    }

    @Override
    public final ColorTextureReader getTextureReaderForColorAttachment(int attachmentIndex)
    {
        return new FramebufferViewportColorTextureReader<>(this, attachmentIndex);
    }

    @Override
    public final DepthTextureReader getTextureReaderForDepthAttachment()
    {
        return new FramebufferViewportDepthTextureReader<>(this);
    }

    /**
     * Gets an object that encapsulates a viewport within this framebuffer that can be drawn to.
     * @param x The left edge of the viewport
     * @param y The bottom edge of the viewport
     * @param viewportWidth The width of the viewport
     * @param viewportHeight The height of the viewport
     * @return
     */
    @Override
    public final FramebufferViewport<ContextType> getViewport(int x, int y, int viewportWidth, int viewportHeight)
    {
        return new ReadableFramebufferViewport<>(this,
            new IntVector2(x + getOffset().x, y + getOffset().y), new FramebufferSize(viewportWidth, viewportHeight));
    }

    private static class FramebufferViewportColorTextureReader<ContextType extends Context<ContextType>>
        extends ColorTextureReaderBase
    {
        private final int attachmentIndex;
        private final ReadableFramebufferViewport<ContextType> readableFramebufferViewport;

        FramebufferViewportColorTextureReader(
            ReadableFramebufferViewport<ContextType> readableFramebufferViewport, int attachmentIndex)
        {
            this.attachmentIndex = attachmentIndex;
            this.readableFramebufferViewport = readableFramebufferViewport;
        }

        @Override
        public final int getWidth()
        {
            return readableFramebufferViewport.getSizeForRead().width;
        }

        @Override
        public final int getHeight()
        {
            return readableFramebufferViewport.getSizeForRead().height;
        }

        @Override
        public final void readARGB(ByteBuffer destination, int x, int y, int readWidth, int readHeight)
        {
            readableFramebufferViewport.framebuffer.getTextureReaderForColorAttachment(attachmentIndex)
                .readARGB(destination, x + readableFramebufferViewport.getOffset().x, y + readableFramebufferViewport.getOffset().y, readWidth, readHeight);
        }

        @Override
        public final void readFloatingPointRGBA(FloatBuffer destination, int x, int y, int readWidth, int readHeight)
        {
            readableFramebufferViewport.framebuffer.getTextureReaderForColorAttachment(attachmentIndex)
                .readFloatingPointRGBA(destination, x + readableFramebufferViewport.getOffset().x, y + readableFramebufferViewport.getOffset().y, readWidth, readHeight);
        }

        @Override
        public final void readIntegerRGBA(IntBuffer destination, int x, int y, int readWidth, int readHeight)
        {
            readableFramebufferViewport.framebuffer.getTextureReaderForColorAttachment(attachmentIndex)
                .readIntegerRGBA(destination, x + readableFramebufferViewport.getOffset().x, y + readableFramebufferViewport.getOffset().y, readWidth, readHeight);
        }
    }

    private static class FramebufferViewportDepthTextureReader<ContextType extends Context<ContextType>> extends DepthTextureReaderBase
    {
        private final ReadableFramebufferViewport<ContextType> readableFramebufferViewport;

        FramebufferViewportDepthTextureReader(ReadableFramebufferViewport<ContextType> readableFramebufferViewport)
        {
            this.readableFramebufferViewport = readableFramebufferViewport;
        }

        @Override
        public final int getWidth()
        {
            return readableFramebufferViewport.getSizeForRead().width;
        }

        @Override
        public final int getHeight()
        {
            return readableFramebufferViewport.getSizeForRead().height;
        }

        @Override
        public final void read(ShortBuffer destination, int x, int y, int width, int height)
        {
            readableFramebufferViewport.framebuffer.getTextureReaderForDepthAttachment()
                .read(destination, x + readableFramebufferViewport.getOffset().x, y + readableFramebufferViewport.getOffset().y, width, height);
        }
    }
}
