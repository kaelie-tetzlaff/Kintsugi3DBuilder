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

package kintsugi3d.fx.internal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import kintsugi3d.builder.io.LoadOptionsModel;

public class ObservableLoadOptionsModel implements LoadOptionsModel
{
    private final BooleanProperty colorImagesRequested = new SimpleBooleanProperty(true);
    private final BooleanProperty mipmapsRequested = new SimpleBooleanProperty(true);
    private final BooleanProperty compressionRequested = new SimpleBooleanProperty(true);
    private final BooleanProperty alphaRequested = new SimpleBooleanProperty(true);
    private final BooleanProperty depthImagesRequested = new SimpleBooleanProperty(true);
    private final IntegerProperty depthImageWidth = new SimpleIntegerProperty(512);
    private final IntegerProperty depthImageHeight = new SimpleIntegerProperty(512);
    private final IntegerProperty previewImageWidth = new SimpleIntegerProperty(1024);
    private final IntegerProperty previewImageHeight = new SimpleIntegerProperty(1024);
    private final IntegerProperty maxLoadingThreads = new SimpleIntegerProperty(12);

    @Override
    public boolean areColorImagesRequested()
    {
        return colorImagesRequested.get();
    }

    @Override
    public boolean areMipmapsRequested()
    {
        return mipmapsRequested.get();
    }

    @Override
    public boolean isCompressionRequested()
    {
        return compressionRequested.get();
    }

    @Override
    public boolean isAlphaRequested()
    {
        return alphaRequested.get();
    }

    @Override
    public int getMaxLoadingThreads()
    {
        return maxLoadingThreads.get();
    }

    @Override
    public boolean areDepthImagesRequested()
    {
        return depthImagesRequested.get();
    }

    @Override
    public int getDepthImageWidth()
    {
        return depthImageWidth.get();
    }

    @Override
    public int getDepthImageHeight()
    {
        return depthImageHeight.get();
    }

    @Override
    public int getPreviewImageWidth()
    {
        return previewImageWidth.get();
    }

    @Override
    public int getPreviewImageHeight()
    {
        return previewImageHeight.get();
    }

    @Override
    public void setColorImagesRequested(boolean colorImagesRequested)
    {
        this.colorImagesRequested.setValue(colorImagesRequested);
    }

    @Override
    public void setMipmapsRequested(boolean mipmapsRequested)
    {
        this.mipmapsRequested.setValue(mipmapsRequested);
    }

    @Override
    public void setCompressionRequested(boolean compressionRequested)
    {
        this.compressionRequested.setValue(compressionRequested);
    }

    @Override
    public void setAlphaRequested(boolean alphaRequested)
    {
        this.alphaRequested.setValue(alphaRequested);
    }

    @Override
    public void setMaxLoadingThreads(int maxLoadingThreads)
    {
        this.maxLoadingThreads.setValue(maxLoadingThreads);
    }

    @Override
    public void setDepthImagesRequested(boolean depthImagesRequested)
    {
        this.depthImagesRequested.setValue(depthImagesRequested);
    }

    @Override
    public void setDepthImageWidth(int depthImageWidth)
    {
        this.depthImageWidth.setValue(depthImageWidth);
    }

    @Override
    public void setDepthImageHeight(int depthImageHeight)
    {
        this.depthImageHeight.setValue(depthImageHeight);
    }

    @Override
    public void setPreviewImageHeight(int previewImageHeight)
    {
        this.previewImageHeight.setValue(previewImageHeight);
    }

    @Override
    public void setPreviewImageWidth(int previewImageWidth)
    {
        this.previewImageWidth.setValue(previewImageWidth);
    }

    public BooleanProperty colorImagesRequestedProperty()
    {
        return colorImagesRequested;
    }
    public BooleanProperty mipmapsRequestedProperty()
    {
        return mipmapsRequested;
    }

    public BooleanProperty compressionRequestedProperty()
    {
        return compressionRequested;
    }

    public BooleanProperty alphaRequestedProperty()
    {
        return alphaRequested;
    }

    public BooleanProperty depthImagesRequestedProperty()
    {
        return depthImagesRequested;
    }

    public IntegerProperty depthImageWidthProperty()
    {
        return depthImageWidth;
    }

    public IntegerProperty depthImageHeightProperty()
    {
        return depthImageHeight;
    }

    public IntegerProperty previewImageWidthProperty()
    {
        return previewImageWidth;
    }

    public IntegerProperty previewImageHeightProperty()
    {
        return previewImageHeight;
    }

    public IntegerProperty maxLoadingThreadsProperty()
    {
        return maxLoadingThreads;
    }
}
