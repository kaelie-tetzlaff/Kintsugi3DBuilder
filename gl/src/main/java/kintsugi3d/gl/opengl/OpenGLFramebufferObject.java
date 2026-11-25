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

package kintsugi3d.gl.opengl;

import kintsugi3d.gl.builders.base.FramebufferObjectBuilderBase;
import kintsugi3d.gl.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.text.MessageFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

final class OpenGLFramebufferObject extends OpenGLFramebuffer implements FramebufferObject<OpenGLContext>
{
    private FBOContents contents;
    private final int id;
    private final int width;
    private final int height;
    private final @NotNull AbstractCollection<ManagedResource> ownedAttachments;
    private final OpenGLFramebufferAttachment @NotNull [] colorAttachments;
    private OpenGLFramebufferAttachment depthAttachment;
    private OpenGLFramebufferAttachment stencilAttachment;
    private OpenGLFramebufferAttachment depthStencilAttachment;
    private boolean live = true;

    public static class OpenGLFramebufferObjectBuilder extends FramebufferObjectBuilderBase<OpenGLContext>
    {
        OpenGLFramebufferObjectBuilder(OpenGLContext context, int width, int height)
        {
            super(context, width, height);
        }

        @Override
        public final @NotNull FramebufferObject<OpenGLContext> createFramebufferObject()
        {
            if (getColorAttachmentCount() > GL_MAX_COLOR_ATTACHMENTS)
            {
                throw new IllegalArgumentException("Too many color attachments specified - maximum is " + GL_MAX_COLOR_ATTACHMENTS + '.');
            }

            OpenGLTexture2D[] colorAttachments = new OpenGLTexture2D[getColorAttachmentCount()];
            for (int i = 0; i < getColorAttachmentCount(); i++)
            {
                if (getColorAttachmentBuilder(i) != null)
                {
                    colorAttachments[i] = (OpenGLTexture2D) getColorAttachmentBuilder(i).createTexture();
                }
            }

            OpenGLTexture2D depthAttachment = null;
            OpenGLTexture2D stencilAttachment = null;
            OpenGLTexture2D depthStencilAttachment = null;
            if (hasCombinedDepthStencilAttachment())
            {
                depthStencilAttachment = (OpenGLTexture2D) getDepthStencilAttachmentBuilder().createTexture();
            }
            else
            {
                if (hasDepthAttachment())
                {
                    depthAttachment = (OpenGLTexture2D) getDepthAttachmentBuilder().createTexture();
                }

                if (hasStencilAttachment())
                {
                    stencilAttachment = (OpenGLTexture2D) getStencilAttachmentBuilder().createTexture();
                }
            }

            return new OpenGLFramebufferObject(getContext(), getWidth(), getHeight(), colorAttachments, depthAttachment, stencilAttachment, depthStencilAttachment);
        }
    }

    private static class FBOContents extends Contents
    {
        private final OpenGLFramebufferObject fbo;
        IntBuffer drawBufferList;

        FBOContents(OpenGLFramebufferObject fbo)
        {
            super(fbo);
            this.fbo = fbo;
        }

        @Override
        final int getId()
        {
            return fbo.id;
        }

        @Override
        final void selectColorSourceForRead(int index)
        {
            glReadBuffer(GL_COLOR_ATTACHMENT0 + index);
            OpenGLContext.errorCheck();
        }

        @Override
        final void selectColorDestinationForDraw(int index)
        {
            glDrawBuffer(GL_COLOR_ATTACHMENT0 + index);
            OpenGLContext.errorCheck();
        }

        @Override
        final void useAllColorDestinationsForDraw()
        {
            if (fbo.colorAttachments.length > 0)
            {
                glDrawBuffers(drawBufferList);
                OpenGLContext.errorCheck();
            }
        }
    }

    private OpenGLFramebufferObject(
        OpenGLContext context,
        int width, int height,
        OpenGLTexture2D @NotNull [] colorAttachments,
        @Nullable OpenGLTexture2D depthAttachment,
        @Nullable OpenGLTexture2D stencilAttachment,
        @Nullable OpenGLTexture2D depthStencilAttachment)
    {
        super(context);

        id = glGenFramebuffers();
        OpenGLContext.errorCheck();

        this.width = width;
        this.height = height;
        ownedAttachments = new ArrayList<>(colorAttachments.length + 1);
        this.colorAttachments = new OpenGLFramebufferAttachment[colorAttachments.length];
        this.depthAttachment = depthAttachment;
        this.stencilAttachment = stencilAttachment;
        this.depthStencilAttachment = depthStencilAttachment;

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
        OpenGLContext.errorCheck();

        for (int i = 0; i < colorAttachments.length; i++)
        {
            if (colorAttachments[i] != null)
            {
                this.colorAttachments[i] = colorAttachments[i];

                colorAttachments[i].attachToDrawFramebuffer(GL_COLOR_ATTACHMENT0 + i, 0);
                ownedAttachments.add(colorAttachments[i]);
            }
        }

        if (depthAttachment != null)
        {
            depthAttachment.attachToDrawFramebuffer(GL_DEPTH_ATTACHMENT, 0);
            ownedAttachments.add(depthAttachment);
        }

        if (stencilAttachment != null)
        {
            stencilAttachment.attachToDrawFramebuffer(GL_STENCIL_ATTACHMENT, 0);
            ownedAttachments.add(stencilAttachment);
        }

        if (depthStencilAttachment != null)
        {
            depthStencilAttachment.attachToDrawFramebuffer(GL_DEPTH_STENCIL_ATTACHMENT, 0);
            ownedAttachments.add(depthStencilAttachment);
        }
    }

    @Override
    public @NotNull FramebufferSize getSize()
    {
        return new FramebufferSize(width, height);
    }

    @Override
    Contents getContents()
    {
        if (contents == null)
        {
            contents = new FBOContents(this);
            contents.drawBufferList = BufferUtils.createIntBuffer(colorAttachments.length);
            for (int i = 0; i < colorAttachments.length; i++)
            {
                contents.drawBufferList.put(i, GL_COLOR_ATTACHMENT0 + i);
            }
        }

        return contents;
    }

    @Override
    public @NotNull FramebufferSize getSizeForRead()
    {
        return getSize();
    }

    @Override
    public FramebufferReadContents<OpenGLContext> getReadContents()
    {
        return getContents();
    }

    @Override
    public @NotNull ColorTextureReader getTextureReaderForColorAttachment(int attachmentIndex)
    {
        return new ColorFBOReader(this, attachmentIndex);
    }

    @Override
    public @NotNull DepthTextureReader getTextureReaderForDepthAttachment()
    {
        return new DepthFBOReader(this);
    }

    @Override
    public OpenGLTexture2D getColorAttachmentTexture(int index)
    {
        if(index < 0)
        {
            throw new IllegalArgumentException("Attachment index cannot be negative.");
        }
        else if (index > colorAttachments.length)
        {
            throw new IllegalArgumentException("Attachment index (" + index +
                    ") exceeded the number of color attachments for the framebufer (" + colorAttachments.length + ").");
        }
        else if (colorAttachments[index] == null || !(colorAttachments[index] instanceof OpenGLTexture2D))
        {
            throw new UnsupportedOperationException("The color attachment for the framebuffer at index " + index + " is not a 2D texture.");
        }
        else
        {
            return (OpenGLTexture2D) colorAttachments[index];
        }
    }

    @Override
    public void setColorAttachment(int index, FramebufferAttachment<OpenGLContext> attachment)
    {
        if (attachment instanceof OpenGLFramebufferAttachment)
        {
            OpenGLFramebufferAttachment attachmentCast = (OpenGLFramebufferAttachment)attachment;

            if (index < colorAttachments.length && colorAttachments[index] != null)
            {
                // Remove the attachment from the list of owned attachments if it existed
                if (ownedAttachments.remove(colorAttachments[index]))
                {
                    // Delete it
                    ((ManagedResource) colorAttachments[index]).close();
                }
            }

            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
            OpenGLContext.errorCheck();
            attachmentCast.attachToDrawFramebuffer(GL_COLOR_ATTACHMENT0 + index, 0);
            colorAttachments[index] = attachmentCast;
        }
        else
        {
            throw new IllegalArgumentException("Attachment must be of type OpenGLFramebufferAttachment.");
        }
    }

    @Override
    public @NotNull OpenGLTexture2D getDepthAttachmentTexture()
    {
        if (depthAttachment == null || !(depthAttachment instanceof OpenGLTexture2D))
        {
            throw new UnsupportedOperationException("The depth attachment for the framebuffer is not a 2D texture.");
        }
        else
        {
            return (OpenGLTexture2D) depthAttachment;
        }
    }

    @Override
    public void setDepthAttachment(FramebufferAttachment<OpenGLContext> attachment)
    {
        if (attachment instanceof OpenGLFramebufferAttachment)
        {
            OpenGLFramebufferAttachment attachmentCast = (OpenGLFramebufferAttachment)attachment;

            if (depthAttachment != null)
            {
                // Remove the attachment from the list of owned attachments if it existed
                if (ownedAttachments.remove(depthAttachment))
                {
                    // Delete it
                    ((ManagedResource) depthAttachment).close();
                }
            }

            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
            OpenGLContext.errorCheck();
            attachmentCast.attachToDrawFramebuffer(GL_DEPTH_ATTACHMENT, 0);

            depthAttachment = attachmentCast;
        }
        else
        {
            throw new IllegalArgumentException("Attachment must be of type OpenGLFramebufferAttachment.");
        }
    }

    @Override
    public @NotNull OpenGLTexture2D getStencilAttachmentTexture()
    {
        if (stencilAttachment == null || !(stencilAttachment instanceof OpenGLTexture2D))
        {
            throw new UnsupportedOperationException("The stencil attachment for the framebuffer is not a 2D texture.");
        }
        else
        {
            return (OpenGLTexture2D) stencilAttachment;
        }
    }


    @Override
    public void setStencilAttachment(FramebufferAttachment<OpenGLContext> attachment)
    {
        if (attachment instanceof OpenGLFramebufferAttachment)
        {
            OpenGLFramebufferAttachment attachmentCast = (OpenGLFramebufferAttachment)attachment;

            if (stencilAttachment != null)
            {
                // Remove the attachment from the list of owned attachments if it existed
                if (ownedAttachments.remove(stencilAttachment))
                {
                    // Delete it
                    ((ManagedResource) stencilAttachment).close();
                }
            }

            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
            OpenGLContext.errorCheck();
            attachmentCast.attachToDrawFramebuffer(GL_STENCIL_ATTACHMENT, 0);

            stencilAttachment = attachmentCast;
        }
        else
        {
            throw new IllegalArgumentException("Attachment must be of type OpenGLFramebufferAttachment.");
        }
    }

    @Override
    public @NotNull OpenGLTexture2D getDepthStencilAttachmentTexture()
    {
        if (depthStencilAttachment == null || !(depthStencilAttachment instanceof OpenGLTexture2D))
        {
            throw new UnsupportedOperationException("The depth/stencil attachment for the framebuffer is not a 2D texture.");
        }
        else
        {
            return (OpenGLTexture2D) depthStencilAttachment;
        }
    }


    @Override
    public void setDepthStencilAttachment(FramebufferAttachment<OpenGLContext> attachment)
    {
        if (attachment instanceof OpenGLFramebufferAttachment)
        {
            OpenGLFramebufferAttachment attachmentCast = (OpenGLFramebufferAttachment)attachment;

            if (depthStencilAttachment != null)
            {
                // Remove the attachment from the list of owned attachments if it existed
                if (ownedAttachments.remove(depthStencilAttachment))
                {
                    // Delete it
                    ((ManagedResource) depthStencilAttachment).close();
                }
            }

            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
            OpenGLContext.errorCheck();
            attachmentCast.attachToDrawFramebuffer(GL_DEPTH_STENCIL_ATTACHMENT, 0);

            depthStencilAttachment = attachmentCast;
        }
        else
        {
            throw new IllegalArgumentException("Attachment must be of type OpenGLFramebufferAttachment.");
        }
    }

    @Override
    public int getColorAttachmentCount()
    {
        return colorAttachments.length;
    }

    @Override
    public void close()
    {
        if (live)
        {
            glDeleteFramebuffers(id);
            OpenGLContext.errorCheck();
            for (ManagedResource attachment : ownedAttachments)
            {
                attachment.close();
            }
            live = false;
        }
    }

    private static class ColorFBOReader extends ColorTextureReaderBase
    {
        private final OpenGLFramebufferObject fbo;
        private final int attachmentIndex;

        ColorFBOReader(OpenGLFramebufferObject fbo, int attachmentIndex)
        {
            this.fbo = fbo;
            this.attachmentIndex = attachmentIndex;
        }

        @Override
        public final int getWidth()
        {
            return fbo.getSizeForRead().width;
        }

        @Override
        public final int getHeight()
        {
            return fbo.getSizeForRead().height;
        }

        @Override
        public final void readARGB(@NotNull ByteBuffer destination, int x, int y, int readWidth, int readHeight)
        {
            if (destination.remaining() < readWidth * readHeight * 4)
            {
                throw new IllegalArgumentException(
                    MessageFormat.format(
                        "The destination buffer (capacity: {0}, limit: {1}, position: {2}, remaining: {3}) " +
                            "is not big enough to hold the requested data (width: {4}, height: {5}).",
                        destination.capacity(), destination.limit(),  destination.position(), destination.remaining(),
                        readWidth, readHeight));
            }

            fbo.getReadContents().bindForRead(attachmentIndex);

            glPixelStorei(GL_PACK_ALIGNMENT, 4);
            OpenGLContext.errorCheck();

            // use BGRA because due to byte order differences it ends up being ARGB
            glReadPixels(x, y, readWidth, readHeight, GL_BGRA, GL_UNSIGNED_BYTE, destination);
            OpenGLContext.errorCheck();
        }

        @Override
        public final void readFloatingPointRGBA(@NotNull FloatBuffer destination, int x, int y, int readWidth, int readHeight)
        {
            if (destination.remaining() < readWidth * readHeight * 4)
            {
                throw new IllegalArgumentException(
                    MessageFormat.format(
                        "The destination buffer (capacity: {0}, limit: {1}, position: {2}, remaining: {3}) " +
                            "is not big enough to hold the requested data (width: {4}, height: {5}).",
                        destination.capacity(), destination.limit(),  destination.position(), destination.remaining(),
                        readWidth, readHeight));
            }

            fbo.getReadContents().bindForRead(attachmentIndex);

            glPixelStorei(GL_PACK_ALIGNMENT, 4);
            OpenGLContext.errorCheck();

            glReadPixels(x, y, readWidth, readHeight, GL_RGBA, GL_FLOAT, destination);
            OpenGLContext.errorCheck();
        }

        @Override
        public final void readIntegerRGBA(@NotNull IntBuffer destination, int x, int y, int readWidth, int readHeight)
        {
            if (destination.remaining() < readWidth * readHeight * 4)
            {
                throw new IllegalArgumentException(
                    MessageFormat.format(
                        "The destination buffer (capacity: {0}, limit: {1}, position: {2}, remaining: {3}) " +
                            "is not big enough to hold the requested data (width: {4}, height: {5}).",
                        destination.capacity(), destination.limit(),  destination.position(), destination.remaining(),
                        readWidth, readHeight));
            }

            fbo.getReadContents().bindForRead(attachmentIndex);

            glPixelStorei(GL_PACK_ALIGNMENT, 4);
            OpenGLContext.errorCheck();

            glReadPixels(x, y, readWidth, readHeight, GL_RGBA_INTEGER, GL_INT, destination);
            OpenGLContext.errorCheck();
        }
    }

    private static class DepthFBOReader extends DepthTextureReaderBase
    {
        private final OpenGLFramebufferObject fbo;

        DepthFBOReader(OpenGLFramebufferObject fbo)
        {
            this.fbo = fbo;
        }

        @Override
        public final int getWidth()
        {
            return fbo.getSizeForRead().width;
        }

        @Override
        public final int getHeight()
        {
            return fbo.getSizeForRead().height;
        }

        @Override
        public final void read(@NotNull ShortBuffer destination, int x, int y, int width, int height)
        {
            if (destination.remaining() < width * height)
            {
                throw new IllegalArgumentException(
                    MessageFormat.format(
                        "The destination buffer (capacity: {0}, limit: {1}, position: {2}, remaining: {3}) " +
                            "is not big enough to hold the requested data (width: {4}, height: {5}).",
                        destination.capacity(), destination.limit(),  destination.position(), destination.remaining(),
                        width, height));
            }

            fbo.getReadContents().bindForRead(0);

            glPixelStorei(GL_PACK_ALIGNMENT, 2);
            OpenGLContext.errorCheck();

            glReadPixels(x, y, width, height, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, destination);
            OpenGLContext.errorCheck();
        }
    }
}
