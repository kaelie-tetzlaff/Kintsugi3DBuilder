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

import kintsugi3d.gl.builders.base.ColorTextureBuilderBase;
import kintsugi3d.gl.builders.base.DepthStencilTextureBuilderBase;
import kintsugi3d.gl.builders.base.DepthTextureBuilderBase;
import kintsugi3d.gl.builders.base.StencilTextureBuilderBase;
import kintsugi3d.gl.builders.framebuffer.FramebufferObjectBuilder;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.nativebuffer.ReadonlyNativeVectorBuffer;
import kintsugi3d.gl.types.AbstractDataType;
import kintsugi3d.gl.types.AbstractDataTypeFactory;
import kintsugi3d.gl.util.ImageHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL44.*;

final class OpenGLTexture3D extends OpenGLTexture implements Texture3D<OpenGLContext>
{
    private final int width;
    private final int height;
    private final int depth;

    static class ColorBuilder extends ColorTextureBuilderBase<OpenGLContext, OpenGLTexture3D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;
        private final int depth;

        ColorBuilder(OpenGLContext context, int textureTarget, int width, int height, int depth)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        @Override
        public final OpenGLTexture3D createTexture()
        {
            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture3D(
                    getContext(),
                    textureTarget,
                    getMultisamples(),
                    getInternalCompressionFormat(),
                    width,
                    height,
                    depth,
                    GL_RGBA,
                    areMultisampleLocationsFixed(),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
            else
            {
                return new OpenGLTexture3D(
                    getContext(),
                    textureTarget,
                    getMultisamples(),
                    getInternalColorFormat(),
                    width,
                    height,
                    depth,
                    getInternalColorFormat().isInteger() ? GL_RGBA_INTEGER : GL_RGBA,
                    areMultisampleLocationsFixed(),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
        }
    }

    static class DepthBuilder extends DepthTextureBuilderBase<OpenGLContext, OpenGLTexture3D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;
        private final int depth;

        DepthBuilder(OpenGLContext context, int textureTarget, int width, int height, int depth)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        @Override
        public final OpenGLTexture3D createTexture()
        {
            return new OpenGLTexture3D(
                getContext(),
                textureTarget,
                getMultisamples(),
                isFloatingPointEnabled() ? TextureType.FLOATING_POINT_DEPTH : TextureType.DEPTH,
                getInternalPrecision(),
                width,
                height,
                depth,
                GL_DEPTH_COMPONENT,
                areMultisampleLocationsFixed(),
                isLinearFilteringEnabled(),
                areMipmapsEnabled(),
                getMaxMipmapLevel(),
                getMaxAnisotropy());
        }
    }

    static class StencilBuilder extends StencilTextureBuilderBase<OpenGLContext, OpenGLTexture3D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;
        private final int depth;

        StencilBuilder(OpenGLContext context, int textureTarget, int width, int height, int depth)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        @Override
        public final OpenGLTexture3D createTexture()
        {
            return new OpenGLTexture3D(
                getContext(),
                textureTarget,
                getMultisamples(),
                TextureType.STENCIL,
                getInternalPrecision(),
                width,
                height,
                depth,
                GL_STENCIL_INDEX,
                areMultisampleLocationsFixed(),
                isLinearFilteringEnabled(),
                areMipmapsEnabled(),
                getMaxMipmapLevel(),
                getMaxAnisotropy());
        }
    }

    static class DepthStencilBuilder extends DepthStencilTextureBuilderBase<OpenGLContext, OpenGLTexture3D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;
        private final int depth;

        DepthStencilBuilder(OpenGLContext context, int textureTarget, int width, int height, int depth)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        @Override
        public final OpenGLTexture3D createTexture()
        {
            return new OpenGLTexture3D(
                getContext(),
                textureTarget,
                getMultisamples(),
                isFloatingPointEnabled() ? TextureType.FLOATING_POINT_DEPTH_STENCIL : TextureType.DEPTH_STENCIL,
                isFloatingPointEnabled() ? 40 : 32,
                width,
                height,
                depth,
                GL_DEPTH_STENCIL,
                areMultisampleLocationsFixed(),
                isLinearFilteringEnabled(),
                areMipmapsEnabled(),
                getMaxMipmapLevel(),
                getMaxAnisotropy());
        }
    }

    private OpenGLTexture3D(OpenGLContext context, int textureTarget, int multisamples, ColorFormat colorFormat,
                            int width, int height, int layerCount, int format, boolean fixedSampleLocations, boolean useLinearFiltering,
                            boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create and allocate a 3D texture or 2D texture array
        super(context, textureTarget, colorFormat,
            new TextureParameters(format, useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy, multisamples, fixedSampleLocations));

        this.width = width;
        this.height = height;
        depth = layerCount;

        init(textureTarget, multisamples, OpenGLContext.getInternalColorFormat(colorFormat), layerCount, format,
            fixedSampleLocations, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private OpenGLTexture3D(OpenGLContext context, int textureTarget, int multisamples, CompressionFormat compressionFormat,
                            int width, int height, int layerCount, int format, boolean fixedSampleLocations, boolean useLinearFiltering,
                            boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create and allocate a 3D texture or 2D texture array
        super(context, textureTarget, compressionFormat,
            new TextureParameters(format, useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy, multisamples, fixedSampleLocations));

        this.width = width;
        this.height = height;
        depth = layerCount;

        init(textureTarget, multisamples, OpenGLContext.getCompressionFormat(compressionFormat), layerCount, format,
            fixedSampleLocations, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private OpenGLTexture3D(OpenGLContext context, int textureTarget, int multisamples, TextureType textureType,
                            int precision, int width, int height, int layerCount, int format, boolean fixedSampleLocations,
                            boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create and allocate a 3D texture or 2D texture array
        super(context, textureTarget, textureType, precision,
            new TextureParameters(format, useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy, multisamples, fixedSampleLocations));

        this.width = width;
        this.height = height;
        depth = layerCount;

        init(textureTarget, multisamples, getSpecialInternalFormat(textureType, precision), layerCount, format,
            fixedSampleLocations, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private void init(int textureTarget, int multisamples, int internalFormat, int layerCount, int format,
                      boolean fixedSampleLocations, boolean useLinearFiltering, int maxMipmapLevel, float maxAnisotropy)
    {
        bind();

        if (textureTarget == GL_TEXTURE_2D && multisamples > 1)
        {
            glTexImage3DMultisample(getTextureTarget(), multisamples, internalFormat, width, height, layerCount, fixedSampleLocations);
            OpenGLContext.errorCheck();
        }
        else
        {
            // Last four parameters are meaningless, but are subject to certain validation conditions
            glTexImage3D(getTextureTarget(), 0, internalFormat, width, height, layerCount, 0, format, GL_UNSIGNED_BYTE, 0);
            OpenGLContext.errorCheck();
        }

        calculateMipmapLevelCount(Math.max(width, height), maxMipmapLevel);
        initFilteringAndMipmaps(useLinearFiltering, maxMipmapLevel, false);

        glTexParameteri(textureTarget, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        OpenGLContext.errorCheck();

        glTexParameteri(textureTarget, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        OpenGLContext.errorCheck();

        glTexParameteri(textureTarget, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        OpenGLContext.errorCheck();

        if (maxAnisotropy > 1.0f)
        {
            glTexParameterf(textureTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
            OpenGLContext.errorCheck();
        }
    }

    private void validateLayerIndex(int layerIndex)
    {
        if (layerIndex < 0 || layerIndex >= depth)
        {
            throw new IllegalArgumentException("The layer index specified (" + layerIndex + ") is out of bounds (layer count: " + depth + ").");
        }
    }

    @Override
    public void loadLayer(int layerIndex, File file, boolean flipVertical) throws IOException
    {
        loadLayer(layerIndex, new FileInputStream(file), flipVertical);
    }

    @Override
    public void loadLayer(int layerIndex, InputStream fileStream, boolean flipVertical) throws IOException
    {
        validateLayerIndex(layerIndex);

        // Load the image
        BufferedImage colorImg = ImageHelper.read(fileStream).scaledToResolution(width, height).getBufferedImage();

        // Transfer data to byte buffer.
        ByteBuffer buffer = bufferedImageToNativeBuffer(colorImg, flipVertical);

        // Send the data to the GPU.
        bind();

        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        OpenGLContext.errorCheck();

        glTexSubImage3D(getTextureTarget(), 0, 0, 0, layerIndex, width, height, 1,
            GL_BGRA, GL_UNSIGNED_BYTE, buffer);
        OpenGLContext.errorCheck();

        if (parameters.useMipmaps)
        {
            invalidateMipmaps();
        }
    }

    @Override
    public void loadLayer(int layerIndex, File imageFile, File maskFile, boolean flipVertical) throws IOException
    {
        loadLayer(layerIndex, new FileInputStream(imageFile), new FileInputStream(maskFile), flipVertical);
    }

    @Override
    public void loadLayer(int layerIndex, InputStream imageStream, InputStream maskStream, boolean flipVertical) throws IOException
    {
        validateLayerIndex(layerIndex);

        // Load the image
        BufferedImage colorImg =
            ImageHelper.read(imageStream).scaledToResolution(width, height).withAlphaMask(maskStream).getBufferedImage();

        // Transfer data to byte buffer.
        ByteBuffer buffer = bufferedImageToNativeBuffer(colorImg, flipVertical);

        // Send the data to the GPU.
        bind();

        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        OpenGLContext.errorCheck();

        glTexSubImage3D(getTextureTarget(), 0, 0, 0, layerIndex, width, height, 1,
            GL_BGRA, GL_UNSIGNED_BYTE, buffer);
        OpenGLContext.errorCheck();

        if (parameters.useMipmaps)
        {
            invalidateMipmaps();
        }
    }

    @Override
    public <MappedType> void loadLayer(int layerIndex, File imageFile, File maskFile, boolean flipVertical,
                                       AbstractDataType<? super MappedType> mappedType, Function<Color, MappedType> mappingFunction) throws IOException
    {
        loadLayer(layerIndex, new FileInputStream(imageFile), new FileInputStream(maskFile), flipVertical, mappedType, mappingFunction);
    }

    @Override
    public <MappedType> void loadLayer(int layerIndex, InputStream imageStream, InputStream maskStream, boolean flipVertical,
                                       AbstractDataType<? super MappedType> mappedType, Function<Color, MappedType> mappingFunction) throws IOException
    {
        validateLayerIndex(layerIndex);

        // Load RGB and alpha separately and rescale to dimensions of texture array.
        BufferedImage rescaledImage =
            ImageHelper.read(imageStream).scaledToResolution(width, height).withAlphaMask(maskStream).getBufferedImage();

        // Transfer to ByteBuffer and send to GPU.
        ByteBuffer buffer = bufferedImageToNativeBuffer(rescaledImage, flipVertical, mappedType, mappingFunction);
        loadLayer(layerIndex, bundleTextureData(mappedType, hasIntegerInternalFormat(), buffer));
    }

    @Override
    public <MappedType> void loadLayer(int layerIndex, File file, boolean flipVertical,
                                       AbstractDataType<? super MappedType> mappedType, Function<Color, MappedType> mappingFunction) throws IOException
    {
        loadLayer(layerIndex, new FileInputStream(file), flipVertical, mappedType, mappingFunction);
    }

    @Override
    public <MappedType> void loadLayer(int layerIndex, InputStream fileStream, boolean flipVertical,
                                       AbstractDataType<? super MappedType> mappedType, Function<Color, MappedType> mappingFunction) throws IOException
    {
        validateLayerIndex(layerIndex);

        // Load image and rescale to dimensions of texture array.
        BufferedImage colorImg = ImageHelper.read(fileStream).scaledToResolution(width, height).getBufferedImage();

        // Transfer to ByteBuffer and send to GPU.
        ByteBuffer buffer = bufferedImageToNativeBuffer(colorImg, flipVertical, mappedType, mappingFunction);
        loadLayer(layerIndex, bundleTextureData(mappedType, hasIntegerInternalFormat(), buffer));
    }

    @Override
    public void loadLayer(int layerIndex, ReadonlyNativeVectorBuffer data)
    {
        if (data.getCount() != width * height)
        {
            throw new IllegalArgumentException(
                String.format("Native vector buffer does not have the required number of elements for this texture.  Expected: %d (%dx%d)  Actual: %d",
                    width * height, width, height, data.getCount()));
        }

        loadLayer(layerIndex,
            bundleTextureData(
                data.getDimensions() > 1 ?
                    AbstractDataTypeFactory.getMultiComponentDataType(data.getDataType(), data.getDimensions())
                    : AbstractDataTypeFactory.getSingleComponentDataType(data.getDataType()),
                hasIntegerInternalFormat(),
                data.getBuffer()));
    }

    private void loadLayer(int layerIndex, OpenGLTextureData textureData)
    {
        bind();

        glPixelStorei(GL_UNPACK_ALIGNMENT, getUnpackAlignment(textureData.getFormat(), textureData.getType()));
        OpenGLContext.errorCheck();

        glTexSubImage3D(getTextureTarget(), 0, 0, 0, layerIndex,
            width, height, 1, textureData.getFormat(), textureData.getType(), textureData.getBuffer());
        OpenGLContext.errorCheck();

        if (parameters.useMipmaps)
        {
            invalidateMipmaps();
        }
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public int getDepth()
    {
        return depth;
    }

    @Override
    public OpenGLFramebufferAttachment getLayerAsFramebufferAttachment(int layerIndex)
    {
        int textureId = getTextureId();
        return new TextureLayer(this, textureId, layerIndex);
    }

    @Override
    public void setTextureWrap(TextureWrapMode wrapS, TextureWrapMode wrapT, TextureWrapMode wrapR)
    {
        bind();
        switch (wrapS)
        {
            case NONE:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                OpenGLContext.errorCheck();
                break;
            case MIRROR_ONCE:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_MIRROR_CLAMP_TO_EDGE);
                OpenGLContext.errorCheck();
                break;
            case REPEAT:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_REPEAT);
                OpenGLContext.errorCheck();
                break;
            case MIRRORED_REPEAT:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
                OpenGLContext.errorCheck();
                break;
        }
        switch (wrapT)
        {
            case NONE:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                OpenGLContext.errorCheck();
                break;
            case MIRROR_ONCE:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_T, GL_MIRROR_CLAMP_TO_EDGE);
                OpenGLContext.errorCheck();
                break;
            case REPEAT:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_T, GL_REPEAT);
                OpenGLContext.errorCheck();
                break;
            case MIRRORED_REPEAT:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
                OpenGLContext.errorCheck();
                break;
        }
        switch (wrapR)
        {
            case NONE:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
                OpenGLContext.errorCheck();
                break;
            case MIRROR_ONCE:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_R, GL_MIRROR_CLAMP_TO_EDGE);
                OpenGLContext.errorCheck();
                break;
            case REPEAT:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_R, GL_REPEAT);
                OpenGLContext.errorCheck();
                break;
            case MIRRORED_REPEAT:
                glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_R, GL_MIRRORED_REPEAT);
                OpenGLContext.errorCheck();
                break;
        }
    }

    @Override
    public Texture3D<OpenGLContext> createTextureWithMatchingFormat(int newWidth, int newHeight, int newDepth)
    {
        if (getTextureType() == TextureType.COLOR)
        {
            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture3D(getContext(), getTextureTarget(), parameters.multisamples,
                    getInternalCompressedColorFormat(), newWidth, newHeight, newDepth, parameters.format, parameters.fixedMultisampleLocations,
                    parameters.useLinearFiltering, parameters.useMipmaps, parameters.maxMipmapLevel, parameters.maxAnisotropy);
            }
            else
            {
                return new OpenGLTexture3D(getContext(), getTextureTarget(), parameters.multisamples,
                    getInternalUncompressedColorFormat(), newWidth, newHeight, newDepth, parameters.format, parameters.fixedMultisampleLocations,
                    parameters.useLinearFiltering, parameters.useMipmaps, parameters.maxMipmapLevel, parameters.maxAnisotropy);
            }
        }
        else
        {
            return new OpenGLTexture3D(getContext(), getTextureTarget(), parameters.multisamples,
                getTextureType(), precision, newWidth, newHeight, newDepth, parameters.format, parameters.fixedMultisampleLocations,
                parameters.useLinearFiltering, parameters.useMipmaps, parameters.maxMipmapLevel, parameters.maxAnisotropy);
        }
    }

    @Override
    public void blitCroppedAndScaled(int destX, int destY, int destZ, int destWidth, int destHeight,
                                     ReadonlyTexture3D<OpenGLContext> readSource, int srcX, int srcY, int srcZ, int srcWidth, int srcHeight, int srcDepth,
                                     boolean linearFiltering)
    {
        if (isInternalFormatCompressed())
        {
            throw new UnsupportedOperationException("Cannot blit to a compressed texture.");
        }
        else if (readSource.isInternalFormatCompressed())
        {
            throw new UnsupportedOperationException("Cannot blit from a compressed texture.");
        }
        else if (!(readSource instanceof OpenGLTexture3D))
        {
            throw new UnsupportedOperationException("Incompatible source texture for blit operation.");
        }
        else
        {
            // readSource and this are both uncompressed and instances of OpenGLTexture3D

            FramebufferObjectBuilder<OpenGLContext> fboBuilder = getContext().buildFramebufferObject(readSource.getWidth(), readSource.getHeight());

            if (getTextureType() == TextureType.COLOR)
            {
                // Color attachments need to be declared in advance; depth and stencil are assumed to always be a possibility
                fboBuilder.addEmptyColorAttachment();
            }

            try (FramebufferObject<OpenGLContext> sourceFBO = fboBuilder.createFramebufferObject();
                 FramebufferObject<OpenGLContext> destFBO = fboBuilder.createFramebufferObject())
            {
                int effectiveDepth = Math.min(Math.min(depth - destZ, readSource.getDepth() - srcZ), srcDepth);

                // Use framebuffer blitting on each depth layer requested
                for (int i = 0; i < effectiveDepth; i++)
                {
                    // If readSource is an OpenGLTexture3D, then it can be used as a framebuffer attachment to simplify the blit implementation.
                    FramebufferAttachment<OpenGLContext> sourceAttachment =
                        ((OpenGLTexture3D)readSource).getLayerAsFramebufferAttachment(srcZ + i);

                    switch (getTextureType())
                    {
                        case COLOR:
                            sourceFBO.setColorAttachment(0, sourceAttachment);
                            destFBO.setColorAttachment(0, getLayerAsFramebufferAttachment(destZ + i));
                            destFBO.blitColorAttachmentFromFramebufferViewport(0, destX, destY, destWidth, destHeight,
                                sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight), 0, linearFiltering);
                            break;
                        case DEPTH:
                        case FLOATING_POINT_DEPTH:
                            sourceFBO.setDepthAttachment(sourceAttachment);
                            destFBO.setDepthAttachment(getLayerAsFramebufferAttachment(destZ + i));
                            destFBO.blitDepthAttachmentFromFramebufferViewport(destX, destY, destWidth, destHeight,
                                sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight));
                            break;
                        case STENCIL:
                            sourceFBO.setStencilAttachment(sourceAttachment);
                            destFBO.setStencilAttachment(getLayerAsFramebufferAttachment(destZ + i));
                            destFBO.blitStencilAttachmentFromFramebufferViewport(destX, destY, destWidth, destHeight,
                                sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight));
                            break;
                        case DEPTH_STENCIL:
                        case FLOATING_POINT_DEPTH_STENCIL:
                            sourceFBO.setDepthStencilAttachment(sourceAttachment);
                            destFBO.setDepthStencilAttachment(getLayerAsFramebufferAttachment(destZ + i));
                            destFBO.blitDepthStencilAttachmentFromFramebufferViewport(destX, destY, destWidth, destHeight,
                                sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight));
                            break;
                        case NULL:
                            // Do nothing
                            break;
                    }
                }
            }
        }
    }

    private static class TextureLayer implements OpenGLFramebufferAttachment
    {
        private final OpenGLTexture3D texture;
        private final int textureId;
        private final int layerIndex;

        TextureLayer(OpenGLTexture3D texture, int textureId, int layerIndex)
        {
            this.texture = texture;
            this.textureId = textureId;
            this.layerIndex = layerIndex;
        }

        @Override
        public final OpenGLContext getContext()
        {
            return texture.getContext();
        }

        @Override
        public final void attachToDrawFramebuffer(int attachment, int level)
        {
            glFramebufferTextureLayer(GL_DRAW_FRAMEBUFFER, attachment, textureId, level, layerIndex);
            OpenGLContext.errorCheck();
        }

        @Override
        public final void attachToReadFramebuffer(int attachment, int level)
        {
            glFramebufferTextureLayer(GL_READ_FRAMEBUFFER, attachment, textureId, level, layerIndex);
            OpenGLContext.errorCheck();
        }
    }
}
