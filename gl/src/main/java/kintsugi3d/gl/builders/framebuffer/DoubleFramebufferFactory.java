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

package kintsugi3d.gl.builders.framebuffer;

import kintsugi3d.gl.core.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public final class DoubleFramebufferFactory
{
    private DoubleFramebufferFactory()
    {
    }

    public static <ContextType extends Context<ContextType>> DoubleFramebufferObject<ContextType> create(
        ContextType context, int initWidth, int initHeight)
    {
        return new DoubleFramebufferObjectImpl<>(context, initWidth, initHeight);
    }

    private static class DoubleFramebufferObjectImpl<ContextType extends Context<ContextType>>
        implements DoubleFramebufferObject<ContextType>
    {
        private final ContextType context;
        private FramebufferObject<ContextType> fbo1;
        private FramebufferObject<ContextType> fbo2;

        private ReadableFramebuffer<ContextType> frontFBO;
        private ReadableFramebuffer<ContextType> backFBO;

        private int width;
        private int height;

        private int newWidth;
        private int newHeight;

        private final Collection<Consumer<ReadableFramebuffer<ContextType>>> swapListeners = new ArrayList<>(1);
        private final Collection<Consumer<Framebuffer<ContextType>>> resizeListeners = new ArrayList<>(1);

        DoubleFramebufferObjectImpl(ContextType context, int initWidth, int initHeight)
        {
            this.context = context;

            fbo1 = context.buildFramebufferObject(initWidth, initHeight)
                .addColorAttachment(ColorFormat.RGBA8)
                .addDepthAttachment()
                .createFramebufferObject();

            fbo2 = context.buildFramebufferObject(initWidth, initHeight)
                .addColorAttachment(ColorFormat.RGBA8)
                .addDepthAttachment()
                .createFramebufferObject();

            frontFBO = fbo1;
            backFBO = fbo2;

            width = initWidth;
            height = initHeight;

            newWidth = initWidth;
            newHeight = initHeight;
        }

        private void resize()
        {
            if (width != backFBO.getSize().width || height !=  backFBO.getSize().height)
            {
                if (backFBO == fbo1)
                {
                    fbo1.close();

                    fbo1 = context.buildFramebufferObject(newWidth, newHeight)
                        .addColorAttachment(ColorFormat.RGBA8)
                        .addDepthAttachment()
                        .createFramebufferObject();

                    backFBO = fbo1;
                }
                else
                {
                    fbo2.close();

                    fbo2 = context.buildFramebufferObject(newWidth, newHeight)
                        .addColorAttachment(ColorFormat.RGBA8)
                        .addDepthAttachment()
                        .createFramebufferObject();

                    backFBO = fbo2;
                }

                // Notify listeners with the resized back buffer
                for (Consumer<Framebuffer<ContextType>> l : resizeListeners)
                {
                    l.accept(backFBO);
                }
            }
        }

        @Override
        public final FramebufferSize getSizeForRead()
        {
            return frontFBO.getSize();
        }

        @Override
        public final FramebufferReadContents<ContextType> getReadContents()
        {
            return frontFBO.getReadContents();
        }

        @Override
        public final FramebufferDrawContents<ContextType> getDrawContents()
        {
            return backFBO.getDrawContents();
        }

        @Override
        public final ContextType getContext()
        {
            return context;
        }

        @Override
        public final FramebufferSize getSize()
        {
            return backFBO.getSize();
        }

        @Override
        public final int getColorAttachmentCount()
        {
            return 1;
        }

        @Override
        public final ColorTextureReader getTextureReaderForColorAttachment(int attachmentIndex)
        {
            return frontFBO.getTextureReaderForColorAttachment(attachmentIndex);
        }

        @Override
        public final DepthTextureReader getTextureReaderForDepthAttachment()
        {
            return frontFBO.getTextureReaderForDepthAttachment();
        }

        @Override
        public final void clearColorBuffer(int attachmentIndex, float r, float g, float b, float a, int x, int y, int width, int height)
        {
            backFBO.clearColorBuffer(attachmentIndex, r, g, b, a, x, y, width, height);
        }

        @Override
        public final void clearIntegerColorBuffer(int attachmentIndex, int r, int g, int b, int a, int x, int y, int width, int height)
        {
            backFBO.clearIntegerColorBuffer(attachmentIndex, r, g, b, a, x, y, width, height);
        }

        @Override
        public final void clearDepthBuffer(float depth, int x, int y, int width, int height)
        {
            backFBO.clearDepthBuffer(depth, x, y, width, height);
        }

        @Override
        public final void clearStencilBuffer(int stencilIndex, int x, int y, int width, int height)
        {
            backFBO.clearStencilBuffer(stencilIndex, x, y, width, height);
        }

        @Override
        public final void blitColorAttachmentFromFramebufferViewport(
            int drawAttachmentIndex, int destX, int destY, int destWidth, int destHeight,
            FramebufferViewport<ContextType> readFramebuffer, int readAttachmentIndex, boolean linearFiltering)
        {
            backFBO.blitColorAttachmentFromFramebufferViewport(drawAttachmentIndex, destX, destY, destWidth, destHeight,
                readFramebuffer, readAttachmentIndex, linearFiltering);
        }

        @Override
        public final void blitDepthAttachmentFromFramebufferViewport(
            int destX, int destY, int destWidth, int destHeight, FramebufferViewport<ContextType> readFramebuffer)
        {
            backFBO.blitDepthAttachmentFromFramebufferViewport(destX, destY, destWidth, destHeight, readFramebuffer);
        }

        @Override
        public final void blitStencilAttachmentFromFramebufferViewport(
            int destX, int destY, int destWidth, int destHeight, FramebufferViewport<ContextType> readFramebuffer)
        {
            backFBO.blitStencilAttachmentFromFramebufferViewport(destX, destY, destWidth, destHeight, readFramebuffer);
        }

        @Override
        public final void blitDepthStencilAttachmentFromFramebufferViewport(
            int destX, int destY, int destWidth, int destHeight, FramebufferViewport<ContextType> readFramebuffer)
        {
            backFBO.blitDepthStencilAttachmentFromFramebufferViewport(destX, destY, destWidth, destHeight, readFramebuffer);
        }

        @Override
        public final void clearColorBuffer(int attachmentIndex, float r, float g, float b, float a)
        {
            backFBO.clearColorBuffer(attachmentIndex, r, g, b, a);
        }

        @Override
        public final void clearIntegerColorBuffer(int attachmentIndex, int r, int g, int b, int a)
        {
            backFBO.clearIntegerColorBuffer(attachmentIndex, r, g, b, a);
        }

        @Override
        public final void clearDepthBuffer(float depth)
        {
            backFBO.clearDepthBuffer(depth);
        }

        @Override
        public final void clearStencilBuffer(int stencilIndex)
        {
            backFBO.clearStencilBuffer(stencilIndex);
        }

        @Override
        public final void requestResize(int width, int height)
        {
            // Framebuffer size cannot be smaller than 1x1.
            newWidth = Math.max(1, width);
            newHeight = Math.max(1, height);
        }

        @Override
        public final void swapBuffers()
        {
            // Swap the buffers
            ReadableFramebuffer<ContextType> tmp = backFBO;
            backFBO = frontFBO;
            frontFBO = tmp;

            // Update size
            width = newWidth;
            height = newHeight;

            // Resize the new back buffer if requested
            resize();

            // Notify listeners with the new front buffer
            for (Consumer<ReadableFramebuffer<ContextType>> l : swapListeners)
            {
                l.accept(frontFBO);
            }
        }

        @Override
        public final void addSwapListener(Consumer<ReadableFramebuffer<ContextType>> listener)
        {
            swapListeners.add(listener);
        }

        @Override
        public final void addResizeListener(Consumer<Framebuffer<ContextType>> listener)
        {
            resizeListeners.add(listener);
        }

        @Override
        public final void close()
        {
            fbo1.close();
            fbo2.close();
        }
    }
}
