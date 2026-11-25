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
import kintsugi3d.gl.util.ImageHelper;
import kintsugi3d.gl.util.RadianceImageLoader;
import kintsugi3d.gl.util.RadianceImageLoader.HDRImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.function.Function;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL32.*;

// mipmaps

final class OpenGLTexture2D extends OpenGLTexture implements Texture2D<OpenGLContext>
{
    private final int width;
    private final int height;

    static class OpenGLTexture2DFromFileBuilder extends ColorTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final BufferedImage colorImg;
        private final boolean flipVertical;

        OpenGLTexture2DFromFileBuilder(OpenGLContext context, int textureTarget, BufferedImage colorImg, BufferedImage maskImg, boolean flipVertical)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.colorImg = ImageHelper.wrap(colorImg).withAlphaMask(maskImg).getBufferedImage();
            this.flipVertical = flipVertical;
        }

        OpenGLTexture2DFromFileBuilder(OpenGLContext context, int textureTarget, @NotNull InputStream imageStream, InputStream maskStream, boolean flipVertical) throws IOException
        {
            super(context);
            this.textureTarget = textureTarget;
            colorImg = ImageHelper.read(imageStream).withAlphaMask(maskStream).getBufferedImage();
            this.flipVertical = flipVertical;
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            int width = colorImg.getWidth();
            int height = colorImg.getHeight();
            OpenGLTextureData textureData = bundleTextureData(colorImg, flipVertical);

            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture2D(
                    getContext(),
                        textureTarget,
                        getInternalCompressionFormat(),
                        width,
                        height,
                        textureData,
                        isLinearFilteringEnabled(),
                        areMipmapsEnabled(),
                        getMaxMipmapLevel(),
                        getMaxAnisotropy());
            }
            else
            {
                return new OpenGLTexture2D(
                    getContext(),
                        textureTarget,
                        getInternalColorFormat(),
                        width,
                        height,
                        textureData,
                        isLinearFilteringEnabled(),
                        areMipmapsEnabled(),
                        getMaxMipmapLevel(),
                        getMaxAnisotropy());
            }
        }
    }

    static class OpenGLTexture2DMappedFromFileBuilder<MappedType> extends ColorTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final BufferedImage colorImg;
        private final boolean flipVertical;
        private final AbstractDataType<? super MappedType> mappedType;
        private final Function<Color, MappedType> mappingFunction;

        OpenGLTexture2DMappedFromFileBuilder(OpenGLContext context, int textureTarget, @NotNull BufferedImage colorImg, @Nullable RenderedImage maskImg, boolean flipVertical,
                                             AbstractDataType<? super MappedType> mappedType, Function<Color, MappedType> mappingFunction)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.colorImg = ImageHelper.wrap(colorImg).withAlphaMask(colorImg).getBufferedImage();
            if (maskImg != null)
            {
                if (maskImg.getWidth() != colorImg.getWidth() || maskImg.getHeight() != colorImg.getHeight())
                {
                    throw new IllegalArgumentException("Color image and mask image must have the same dimensions.");
                }
            }
            this.flipVertical = flipVertical;
            this.mappedType = mappedType;
            this.mappingFunction = mappingFunction;
        }

        OpenGLTexture2DMappedFromFileBuilder(OpenGLContext context, int textureTarget, @NotNull InputStream imageStream, InputStream maskStream, boolean flipVertical,
                                             AbstractDataType<? super MappedType> mappedType, Function<Color, MappedType> mappingFunction) throws IOException
        {
            super(context);
            this.textureTarget = textureTarget;
            colorImg = ImageHelper.read(imageStream).withAlphaMask(maskStream).getBufferedImage();
            this.flipVertical = flipVertical;
            this.mappedType = mappedType;
            this.mappingFunction = mappingFunction;
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            int width = colorImg.getWidth();
            int height = colorImg.getHeight();
            ByteBuffer buffer = bufferedImageToNativeBuffer(colorImg, flipVertical, mappedType, mappingFunction);

            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture2D(
                    getContext(),
                    textureTarget,
                    getInternalCompressionFormat(),
                    width,
                    height,
                    bundleTextureData(mappedType, false, buffer),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
            else
            {
                ColorFormat internalColorFormat = getInternalColorFormat();
                assert internalColorFormat != null;
                return new OpenGLTexture2D(
                    getContext(),
                    textureTarget,
                    internalColorFormat,
                    width,
                    height,
                    bundleTextureData(mappedType, internalColorFormat.isInteger(), buffer),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
        }
    }

    static class OpenGLTexture2DFromHDRFileBuilder extends ColorTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final HDRImage colorImg;
        private BufferedImage maskImg;

        OpenGLTexture2DFromHDRFileBuilder(OpenGLContext context, int textureTarget, @NotNull BufferedInputStream imageStream, @Nullable InputStream maskStream, boolean flipVertical) throws IOException
        {
            super(context);
            this.textureTarget = textureTarget;
            colorImg = new RadianceImageLoader().read(imageStream, flipVertical, true);
            if (maskStream != null)
            {
                maskImg = ImageHelper.read(maskStream).scaledToResolution(colorImg.width, colorImg.height).getBufferedImage();
            }
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            int width = colorImg.width;
            int height = colorImg.height;

            OpenGLTextureData textureData =
                maskImg != null ? bundleTextureData(colorImg, maskImg) : bundleTextureData(colorImg);

            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture2D(
                    getContext(),
                        textureTarget,
                        getInternalCompressionFormat(),
                        width,
                        height,
                        textureData,
                        isLinearFilteringEnabled(),
                        areMipmapsEnabled(),
                        getMaxMipmapLevel(),
                        getMaxAnisotropy());
            }
            else
            {
                return new OpenGLTexture2D(
                    getContext(),
                        textureTarget,
                        getInternalColorFormat(),
                        width,
                        height,
                        textureData,
                        isLinearFilteringEnabled(),
                        areMipmapsEnabled(),
                        getMaxMipmapLevel(),
                        getMaxAnisotropy());
            }
        }
    }

    static class OpenGLTexture2DFromBufferBuilder extends ColorTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;
        private final int dimensions;
        private final int type;
        private final ByteBuffer buffer;

        OpenGLTexture2DFromBufferBuilder(OpenGLContext context, int textureTarget, int width, int height, int dimensions, int type, ByteBuffer buffer)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
            this.dimensions = dimensions;
            this.type = type;
            this.buffer = buffer;
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture2D(
                    getContext(),
                        textureTarget,
                        getInternalCompressionFormat(),
                        width,
                        height,
                        bundleTextureData(dimensions, false, type, buffer),
                        isLinearFilteringEnabled(),
                        areMipmapsEnabled(),
                        getMaxMipmapLevel(),
                        getMaxAnisotropy());
            }
            else
            {
                ColorFormat internalColorFormat = getInternalColorFormat();
                assert internalColorFormat != null;
                return new OpenGLTexture2D(
                    getContext(),
                    textureTarget,
                    internalColorFormat,
                    width,
                    height,
                    bundleTextureData(dimensions, internalColorFormat.isInteger(), type, buffer),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
        }
    }

    static class OpenGLTexture2DColorBuilder extends ColorTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;

        OpenGLTexture2DColorBuilder(OpenGLContext context, int textureTarget, int width, int height)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture2D(
                    getContext(),
                        textureTarget,
                        getMultisamples(),
                        getInternalCompressionFormat(),
                        width,
                        height,
                        GL_RGBA,
                        areMultisampleLocationsFixed(),
                        isLinearFilteringEnabled(),
                        areMipmapsEnabled(),
                        getMaxMipmapLevel(),
                        getMaxAnisotropy());
            }
            else
            {
                ColorFormat internalColorFormat = getInternalColorFormat();
                assert internalColorFormat != null;
                return new OpenGLTexture2D(
                    getContext(),
                    textureTarget,
                    getMultisamples(),
                    internalColorFormat,
                    width,
                    height,
                    internalColorFormat.isInteger() ? GL_RGBA_INTEGER : GL_RGBA,
                    areMultisampleLocationsFixed(),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
        }
    }

    static class OpenGLTexture2DDepthBuilder extends DepthTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;

        OpenGLTexture2DDepthBuilder(OpenGLContext context, int textureTarget, int width, int height)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            return new OpenGLTexture2D(
                getContext(),
                    textureTarget,
                    getMultisamples(),
                    isFloatingPointEnabled() ? TextureType.FLOATING_POINT_DEPTH : TextureType.DEPTH,
                    getInternalPrecision(),
                    width,
                    height,
                    GL_DEPTH_COMPONENT,
                    areMultisampleLocationsFixed(),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
        }
    }

    static class OpenGLTexture2DStencilBuilder extends StencilTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;

        OpenGLTexture2DStencilBuilder(OpenGLContext context, int textureTarget, int width, int height)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            return new OpenGLTexture2D(
                getContext(),
                    textureTarget,
                    getMultisamples(),
                    TextureType.STENCIL,
                    getInternalPrecision(),
                    width,
                    height,
                    GL_STENCIL_INDEX,
                    areMultisampleLocationsFixed(),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
        }
    }

    static class OpenGLTexture2DDepthStencilBuilder extends DepthStencilTextureBuilderBase<OpenGLContext, OpenGLTexture2D>
    {
        private final int textureTarget;
        private final int width;
        private final int height;

        OpenGLTexture2DDepthStencilBuilder(OpenGLContext context, int textureTarget, int width, int height)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.height = height;
        }

        @Override
        public final @NotNull OpenGLTexture2D createTexture()
        {
            return new OpenGLTexture2D(
                getContext(),
                    textureTarget,
                    getMultisamples(),
                    isFloatingPointEnabled() ? TextureType.FLOATING_POINT_DEPTH_STENCIL : TextureType.DEPTH_STENCIL,
                    isFloatingPointEnabled() ? 40 : 32,
                    width,
                    height,
                    GL_DEPTH_STENCIL,
                    areMultisampleLocationsFixed(),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
        }
    }

    private OpenGLTexture2D(
        OpenGLContext context, int textureTarget, @NotNull ColorFormat colorFormat, int width, int height, @NotNull OpenGLTextureData textureData,
        boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        super(context, textureTarget, colorFormat,
            new TextureParameters(textureData.getFormat(), useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy));

        this.width = width;
        this.height = height;

        initWithData(textureTarget, OpenGLContext.getInternalColorFormat(colorFormat), textureData, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private OpenGLTexture2D(
        OpenGLContext context, int textureTarget, @NotNull CompressionFormat compressionFormat, int width, int height, @NotNull OpenGLTextureData textureData,
        boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        super(context, textureTarget, compressionFormat,
            new TextureParameters(textureData.getFormat(), useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy));

        this.width = width;
        this.height = height;

        initWithData(textureTarget, OpenGLContext.getCompressionFormat(compressionFormat), textureData, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private OpenGLTexture2D(
        OpenGLContext context, int openGLTextureTarget, int multisamples, @NotNull ColorFormat colorFormat, int width, int height, int format,
        boolean fixedMultisampleLocations, boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        super(context,
            openGLTextureTarget == GL_TEXTURE_2D && multisamples > 1 ? GL_TEXTURE_2D_MULTISAMPLE : openGLTextureTarget,
            colorFormat,
            new TextureParameters(format, useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy, multisamples, fixedMultisampleLocations));

        this.width = width;
        this.height = height;

        initEmpty(openGLTextureTarget, multisamples, OpenGLContext.getInternalColorFormat(colorFormat), format,
                fixedMultisampleLocations, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private OpenGLTexture2D(
        OpenGLContext context, int openGLTextureTarget, int multisamples, @NotNull CompressionFormat compressionFormat, int width, int height, int format,
        boolean fixedMultisampleLocations, boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        super(context,
            openGLTextureTarget == GL_TEXTURE_2D && multisamples > 1 ? GL_TEXTURE_2D_MULTISAMPLE : openGLTextureTarget,
            compressionFormat,
            new TextureParameters(format, useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy, multisamples, fixedMultisampleLocations));

        this.width = width;
        this.height = height;

        initEmpty(openGLTextureTarget, multisamples, OpenGLContext.getCompressionFormat(compressionFormat), format,
                fixedMultisampleLocations, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    // TODO way too many parameters and too much repeated code across various texture subtypes
    private OpenGLTexture2D(
        OpenGLContext context, int openGLTextureTarget, int multisamples, @NotNull TextureType textureType, int precision, int width, int height, int format,
        boolean fixedMultisampleLocations, boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        super(context,
            openGLTextureTarget == GL_TEXTURE_2D && multisamples > 1 ? GL_TEXTURE_2D_MULTISAMPLE : openGLTextureTarget,
            textureType, precision,
            new TextureParameters(format, useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy, multisamples, fixedMultisampleLocations));

        this.width = width;
        this.height = height;

        initEmpty(openGLTextureTarget, multisamples, getSpecialInternalFormat(textureType, precision), format,
                fixedMultisampleLocations, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private void initEmpty(int textureTarget, int multisamples, int internalFormat, int format,
                           boolean fixedMultisampleLocations, boolean useLinearFiltering, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        bind();
        if (textureTarget == GL_TEXTURE_2D && multisamples > 1)
        {
            glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, multisamples, internalFormat, width, height, fixedMultisampleLocations);
            OpenGLContext.errorCheck();

            // linear filtering and mipmaps not allowed with multisampling
            initCommon(GL_TEXTURE_2D, false, 0, maxAnisotropy);
            // TODO: multisample textures don't seem to work correctly
        }
        else
        {
            // Last four parameters are essentially meaningless, but are subject to certain validation conditions
            glTexImage2D(textureTarget, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, 0);
            OpenGLContext.errorCheck();

            initCommon(textureTarget, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
        }
    }

    private void initWithData(int textureTarget, int internalFormat, @NotNull OpenGLTextureData textureData,
                              boolean useLinearFiltering, int maxMipmapLevel, float maxAnisotropy)
    {
        bind();
        glPixelStorei(GL_UNPACK_ALIGNMENT, getUnpackAlignment(textureData.getFormat(), textureData.getType()));
        OpenGLContext.errorCheck();

        glTexImage2D(textureTarget, 0, internalFormat, width, height, 0, textureData.getFormat(), textureData.getType(), textureData.getBuffer());
        OpenGLContext.errorCheck();

        initCommon(textureTarget, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private void initCommon(int textureTarget, boolean useLinearFiltering, int maxMipmapLevel, float maxAnisotropy)
    {
        initFilteringAndMipmaps(useLinearFiltering, maxMipmapLevel);

        glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        OpenGLContext.errorCheck();

        glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        OpenGLContext.errorCheck();

        if (maxAnisotropy > 1.0f)
        {
            glTexParameterf(textureTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
            OpenGLContext.errorCheck();
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
    public void setTextureWrap(@NotNull TextureWrapMode wrapS, @NotNull TextureWrapMode wrapT)
    {
        bind();
        int numericWrapS = translateWrapMode(wrapS);
        int numericWrapT = translateWrapMode(wrapT);

        if (numericWrapS != 0)
        {
            glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, numericWrapS);
            OpenGLContext.errorCheck();
        }

        if (numericWrapT != 0)
        {
            glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_T, numericWrapT);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void load(@NotNull ReadonlyNativeVectorBuffer data)
    {
        if (data.getCount() != width * height)
        {
            throw new IllegalArgumentException(
                String.format("Native vector buffer does not have the required number of elements for this texture.  Expected: %d (%dx%d)  Actual: %d",
                    width * height, width, height, data.getCount()));
        }

        // Send the data to the GPU.
        bind();

        int format = getPixelDataFormatFromDimensions(data.getDimensions(),
            !isInternalFormatCompressed() && getInternalUncompressedColorFormat().isInteger());

        int type = OpenGLContext.getDataTypeConstant(data.getDataType());

        glPixelStorei(GL_UNPACK_ALIGNMENT, getUnpackAlignment(format, type));
        OpenGLContext.errorCheck();

        glTexSubImage2D(getTextureTarget(), 0, 0, 0, width, height, format, type, data.getBuffer());
        OpenGLContext.errorCheck();

        if (parameters.useMipmaps)
        {
            invalidateMipmaps();
        }
    }

    @Override
    public void load(@NotNull InputStream imageStream, boolean flipVertical) throws IOException
    {
        // Load the image
        BufferedImage colorImg = ImageHelper.read(imageStream).getBufferedImage();

        // Dimensions must match to replace the contents of the texture.
        if (colorImg.getWidth() != width || colorImg.getHeight() !=  height)
        {
            throw new IllegalArgumentException(
                String.format("Image does not have the required dimensions for this texture.  Expected: %dx%d  Actual: %dx%d",
                    width, height, colorImg.getWidth(), colorImg.getHeight()));
        }

        // Transfer data to byte buffer.
        ByteBuffer buffer = bufferedImageToNativeBuffer(colorImg, flipVertical);

        // Send the data to the GPU.
        bind();

        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        OpenGLContext.errorCheck();

        glTexSubImage2D(getTextureTarget(), 0, 0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, buffer);
        OpenGLContext.errorCheck();

        if (parameters.useMipmaps)
        {
            invalidateMipmaps();
        }
    }

    @Override
    public void load(@NotNull File imageFile, boolean flipVertical) throws IOException
    {
        load(new FileInputStream(imageFile), flipVertical);
    }

    @Override
    public @NotNull Texture2D<OpenGLContext> createTextureWithMatchingFormat(int newWidth, int newHeight)
    {
        if (getTextureType() == TextureType.COLOR)
        {
            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture2D(getContext(), getTextureTarget(), parameters.multisamples,
                    getInternalCompressedColorFormat(), newWidth, newHeight, parameters.format, parameters.fixedMultisampleLocations,
                    parameters.useLinearFiltering, parameters.useMipmaps, parameters.maxMipmapLevel, parameters.maxAnisotropy);
            }
            else
            {
                return new OpenGLTexture2D(getContext(), getTextureTarget(), parameters.multisamples,
                    getInternalUncompressedColorFormat(), newWidth, newHeight, parameters.format, parameters.fixedMultisampleLocations,
                    parameters.useLinearFiltering, parameters.useMipmaps, parameters.maxMipmapLevel, parameters.maxAnisotropy);
            }
        }
        else
        {
            return new OpenGLTexture2D(getContext(), getTextureTarget(), parameters.multisamples,
                getTextureType(), precision, newWidth, newHeight, parameters.format, parameters.fixedMultisampleLocations,
                parameters.useLinearFiltering, parameters.useMipmaps, parameters.maxMipmapLevel, parameters.maxAnisotropy);
        }
    }

    @Override
    public void blitCroppedAndScaled(int destX, int destY, int destWidth, int destHeight, @NotNull ReadonlyTexture2D<OpenGLContext> readSource,
                                     int srcX, int srcY, int srcWidth, int srcHeight, boolean linearFiltering)
    {
        if (isInternalFormatCompressed())
        {
            throw new UnsupportedOperationException("Cannot blit to a compressed texture.");
        }
        else if (readSource.isInternalFormatCompressed())
        {
            throw new UnsupportedOperationException("Cannot blit from a compressed texture.");
        }
        else if (!(readSource instanceof OpenGLTexture2D))
        {
            throw new UnsupportedOperationException("Incompatible source texture for blit operation.");
        }
        else
        {
            // readSource and this are both uncompressed and instances of OpenGLTexture2D

            FramebufferObjectBuilder<OpenGLContext> fboBuilder = getContext().buildFramebufferObject(readSource.getWidth(), readSource.getHeight());

            if (getTextureType() == TextureType.COLOR)
            {
                // Color attachments need to be declared in advance; depth and stencil are assumed to always be a possibility
                fboBuilder.addEmptyColorAttachment();
            }

            // If readSource is an OpenGLTexture2D, then it can be used as a framebuffer attachment to simplify the blit implementation.
            @SuppressWarnings("unchecked")
            FramebufferAttachment<OpenGLContext> sourceAttachment = (FramebufferAttachment<OpenGLContext>) readSource;

            try (FramebufferObject<OpenGLContext> sourceFBO = fboBuilder.createFramebufferObject();
                 FramebufferObject<OpenGLContext> destFBO = fboBuilder.createFramebufferObject())
            {
                switch (getTextureType())
                {
                    case COLOR:
                        sourceFBO.setColorAttachment(0, sourceAttachment);
                        destFBO.setColorAttachment(0, this);
                        destFBO.getViewport(destX, destY, destWidth, destHeight)
                            .blitColorAttachmentFromFramebuffer(0, sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight), 0);
                        break;
                    case DEPTH:
                    case FLOATING_POINT_DEPTH:
                        sourceFBO.setDepthAttachment(sourceAttachment);
                        destFBO.setDepthAttachment(this);
                        destFBO.getViewport(destX, destY, destWidth, destHeight)
                            .blitDepthAttachmentFromFramebuffer(sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight));
                        break;
                    case STENCIL:
                        sourceFBO.setStencilAttachment(sourceAttachment);
                        destFBO.setStencilAttachment(this);
                        destFBO.getViewport(destX, destY, destWidth, destHeight)
                            .blitStencilAttachmentFromFramebuffer(sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight));
                        break;
                    case DEPTH_STENCIL:
                    case FLOATING_POINT_DEPTH_STENCIL:
                        sourceFBO.setDepthStencilAttachment(sourceAttachment);
                        destFBO.setDepthStencilAttachment(this);
                        destFBO.getViewport(destX, destY, destWidth, destHeight)
                            .blitDepthStencilAttachmentFromFramebuffer(sourceFBO.getViewport(srcX, srcY, srcWidth, srcHeight));
                        break;
                    case NULL:
                        // Do nothing
                        break;
                }
            }
        }
    }
}
