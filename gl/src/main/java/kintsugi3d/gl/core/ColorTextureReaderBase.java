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

package kintsugi3d.gl.core;

import kintsugi3d.gl.util.BufferedImageBuilder;
import kintsugi3d.gl.vecmath.IntVector4;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Base class for texture readers that contains commonly used implementations for transferring native buffer data to a
 * "normal" Java array, and saving color images to file.
 */
public abstract class ColorTextureReaderBase implements ColorTextureReader
{
    @Override
    public int[] readARGB(int x, int y, int readWidth, int readHeight)
    {
        ByteBuffer pixelBuffer = BufferUtils.createByteBuffer(readWidth * readHeight * 4);

        readARGB(pixelBuffer, x, y, readWidth, readHeight);

        int[] pixelArray = new int[readWidth * readHeight];
        pixelBuffer.asIntBuffer().get(pixelArray);
        return pixelArray;
    }

    @Override
    public float[] readFloatingPointRGBA(int x, int y, int readWidth, int readHeight)
    {
        FloatBuffer pixelBuffer = BufferUtils.createFloatBuffer(readWidth * readHeight * 4);

        readFloatingPointRGBA(pixelBuffer, x, y, readWidth, readHeight);

        float[] pixelArray = new float[readWidth * readHeight * 4];
        pixelBuffer.get(pixelArray);
        return pixelArray;
    }

    @Override
    public int[] readIntegerRGBA(int x, int y, int readWidth, int readHeight)
    {
        IntBuffer pixelBuffer = BufferUtils.createIntBuffer(readWidth * readHeight * 4);

        readIntegerRGBA(pixelBuffer, x, y, readWidth, readHeight);

        int[] pixelArray = new int[readWidth * readHeight * 4];
        pixelBuffer.get(pixelArray);
        return pixelArray;
    }

    @Override
    public void saveToFile(String fileFormat, File file) throws IOException
    {
        int[] pixels = this.readARGB();
        BufferedImage outImg = BufferedImageBuilder.build()
            .setBufferedImageType("JPEG".equals(fileFormat) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB)
            .setDataFromArray(pixels, getWidth(), getHeight())
            .flipVertical()
            .create();
        file.getParentFile().mkdirs(); // make sure the directory structure exists
        ImageIO.write(outImg, fileFormat, file);
    }

    @Override
    public void saveToFile(String fileFormat, File file, Function<IntVector4, IntVector4> tonemapper) throws IOException
    {
        int[] pixels = this.readARGB();
        BufferedImage outImg = BufferedImageBuilder.build()
            .setBufferedImageType("JPEG".equals(fileFormat) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB)
            .setDataFromArray(pixels, getWidth(), getHeight())
            .tonemap(tonemapper)
            .flipVertical()
            .create();
        file.getParentFile().mkdirs(); // make sure the directory structure exists
        ImageIO.write(outImg, fileFormat, file);
    }

    @Override
    public void saveToFile(String fileFormat, File file, BiFunction<IntVector4, Integer, IntVector4> tonemapper) throws IOException
    {
        int[] pixels = this.readARGB();
        BufferedImage outImg = BufferedImageBuilder.build()
            .setBufferedImageType("JPEG".equals(fileFormat) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB)
            .setDataFromArray(pixels, getWidth(), getHeight())
            .tonemap(tonemapper)
            .flipVertical()
            .create();
        file.getParentFile().mkdirs(); // make sure the directory structure exists
        ImageIO.write(outImg, fileFormat, file);
    }

    @Override
    public void saveToFile(int x, int y, int width, int height, String fileFormat, File file) throws IOException
    {
        int[] pixels = this.readARGB(x, y, width, height);
        BufferedImage outImg = BufferedImageBuilder.build()
            .setBufferedImageType("JPEG".equals(fileFormat) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB)
            .setDataFromArray(pixels, width, height)
            .flipVertical()
            .create();
        file.getParentFile().mkdirs(); // make sure the directory structure exists
        ImageIO.write(outImg, fileFormat, file);
    }

    @Override
    public void saveToFile(int x, int y, int width, int height, String fileFormat, File file, Function<IntVector4, IntVector4> tonemapper) throws IOException
    {
        int[] pixels = this.readARGB(x, y, width, height);
        BufferedImage outImg = BufferedImageBuilder.build()
            .setBufferedImageType("JPEG".equals(fileFormat) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB)
            .setDataFromArray(pixels, width, height)
            .tonemap(tonemapper)
            .flipVertical()
            .create();
        file.getParentFile().mkdirs(); // make sure the directory structure exists
        ImageIO.write(outImg, fileFormat, file);
    }

    @Override
    public void saveToFile(int x, int y, int width, int height, String fileFormat, File file, BiFunction<IntVector4, Integer, IntVector4> tonemapper) throws IOException
    {
        int[] pixels = this.readARGB(x, y, width, height);
        BufferedImage outImg = BufferedImageBuilder.build()
            .setBufferedImageType("JPEG".equals(fileFormat) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB)
            .setDataFromArray(pixels, width, height)
            .tonemap(tonemapper)
            .flipVertical()
            .create();
        file.getParentFile().mkdirs(); // make sure the directory structure exists
        ImageIO.write(outImg, fileFormat, file);
    }
}
