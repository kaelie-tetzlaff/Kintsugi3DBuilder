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

package kintsugi3d.builder.io;

public class SimpleLoadOptionsModel implements LoadOptionsModel
{
    private boolean colorImagesRequested = true;
    private boolean mipmapsRequested = true;
    private boolean compressionRequested = true;
    private boolean alphaRequested = true;
    private boolean depthImagesRequested = true;
    private int depthImageWidth = 512;
    private int depthImageHeight = 512;
    private int previewImageWidth = 1024;
    private int previewImageHeight = 1024;
    private int maxLoadingThreads = 12;

    @Override
    public final boolean areColorImagesRequested()
    {
        return this.colorImagesRequested;
    }

    @Override
    public final int getPreviewImageWidth()
    {
        return previewImageWidth;
    }

    @Override
    public final void setPreviewImageWidth(int previewImageWidth)
    {
        this.previewImageWidth = previewImageWidth;
    }

    @Override
    public final int getPreviewImageHeight()
    {
        return previewImageHeight;
    }

    @Override
    public final void setColorImagesRequested(boolean colorImagesRequested)
    {
        this.colorImagesRequested = colorImagesRequested;
    }

    @Override
    public final void setMipmapsRequested(boolean mipmapsRequested)
    {
        this.mipmapsRequested = mipmapsRequested;
    }

    @Override
    public final void setCompressionRequested(boolean compressionRequested)
    {
        this.compressionRequested = compressionRequested;
    }

    @Override
    public final void setAlphaRequested(boolean alphaRequested)
    {
        this.alphaRequested = alphaRequested;
    }

    @Override
    public final void setDepthImagesRequested(boolean depthImagesRequested)
    {
        this.depthImagesRequested = depthImagesRequested;
    }

    @Override
    public final void setDepthImageWidth(int depthImageWidth)
    {
        this.depthImageWidth = depthImageWidth;
    }

    @Override
    public final void setDepthImageHeight(int depthImageHeight)
    {
        this.depthImageHeight = depthImageHeight;
    }

    @Override
    public final void setPreviewImageHeight(int previewImageHeight)
    {
        this.previewImageHeight = previewImageHeight;
    }

    public final SimpleLoadOptionsModel requestColorImages(boolean colorImagesRequested)
    {
        this.colorImagesRequested = colorImagesRequested;
        return this;
    }

    @Override
    public final boolean areMipmapsRequested()
    {
        return this.mipmapsRequested;
    }

    public final SimpleLoadOptionsModel requstMipmaps(boolean mipmapsRequested)
    {
        this.mipmapsRequested = mipmapsRequested;
        return this;
    }

    @Override
    public final boolean isCompressionRequested()
    {
        return this.compressionRequested;
    }

    @Override
    public final boolean isAlphaRequested()
    {
        return this.alphaRequested;
    }

    public final SimpleLoadOptionsModel requestAlpha(boolean alphaRequested)
    {
        this.alphaRequested = alphaRequested;
        return this;
    }

    public final SimpleLoadOptionsModel requestCompression(boolean compressionRequested)
    {
        this.compressionRequested = compressionRequested;
        return this;
    }

    @Override
    public final boolean areDepthImagesRequested()
    {
        return this.depthImagesRequested;
    }

    public final SimpleLoadOptionsModel requestDepthImages(boolean depthImagesRequested)
    {
        this.depthImagesRequested = depthImagesRequested;
        return this;
    }

    @Override
    public final int getDepthImageWidth()
    {
        return this.depthImageWidth;
    }

    public final SimpleLoadOptionsModel requestDepthImageWidth(int depthImageWidth)
    {
        this.depthImageWidth = depthImageWidth;
        return this;
    }

    @Override
    public final int getDepthImageHeight()
    {
        return this.depthImageHeight;
    }

    public final SimpleLoadOptionsModel requestDepthImageHeight(int depthImageHeight)
    {
        this.depthImageHeight = depthImageHeight;
        return this;
    }

    @Override
    public final int getMaxLoadingThreads()
    {
        return maxLoadingThreads;
    }

    @Override
    public final void setMaxLoadingThreads(int maxLoadingThreads)
    {
        this.maxLoadingThreads = maxLoadingThreads;
    }
}
