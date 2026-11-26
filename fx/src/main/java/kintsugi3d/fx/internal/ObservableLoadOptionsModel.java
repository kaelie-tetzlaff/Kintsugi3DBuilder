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

package kintsugi3d.fx.internal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import kintsugi3d.builder.io.LoadOptionsModel;
import org.jetbrains.annotations.NotNull;

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
    public final boolean areColorImagesRequested()
    {
        return colorImagesRequested.get();
    }

    @Override
    public final boolean areMipmapsRequested()
    {
        return mipmapsRequested.get();
    }

    @Override
    public final boolean isCompressionRequested()
    {
        return compressionRequested.get();
    }

    @Override
    public final boolean isAlphaRequested()
    {
        return alphaRequested.get();
    }

    @Override
    public final int getMaxLoadingThreads()
    {
        return maxLoadingThreads.get();
    }

    @Override
    public final boolean areDepthImagesRequested()
    {
        return depthImagesRequested.get();
    }

    @Override
    public final int getDepthImageWidth()
    {
        return depthImageWidth.get();
    }

    @Override
    public final int getDepthImageHeight()
    {
        return depthImageHeight.get();
    }

    @Override
    public final int getPreviewImageWidth()
    {
        return previewImageWidth.get();
    }

    @Override
    public final int getPreviewImageHeight()
    {
        return previewImageHeight.get();
    }

    @Override
    public final void setColorImagesRequested(boolean colorImagesRequested)
    {
        this.colorImagesRequested.setValue(colorImagesRequested);
    }

    @Override
    public final void setMipmapsRequested(boolean mipmapsRequested)
    {
        this.mipmapsRequested.setValue(mipmapsRequested);
    }

    @Override
    public final void setCompressionRequested(boolean compressionRequested)
    {
        this.compressionRequested.setValue(compressionRequested);
    }

    @Override
    public final void setAlphaRequested(boolean alphaRequested)
    {
        this.alphaRequested.setValue(alphaRequested);
    }

    @Override
    public final void setMaxLoadingThreads(int maxLoadingThreads)
    {
        this.maxLoadingThreads.setValue(maxLoadingThreads);
    }

    @Override
    public final void setDepthImagesRequested(boolean depthImagesRequested)
    {
        this.depthImagesRequested.setValue(depthImagesRequested);
    }

    @Override
    public final void setDepthImageWidth(int depthImageWidth)
    {
        this.depthImageWidth.setValue(depthImageWidth);
    }

    @Override
    public final void setDepthImageHeight(int depthImageHeight)
    {
        this.depthImageHeight.setValue(depthImageHeight);
    }

    @Override
    public final void setPreviewImageHeight(int previewImageHeight)
    {
        this.previewImageHeight.setValue(previewImageHeight);
    }

    @Override
    public final void setPreviewImageWidth(int previewImageWidth)
    {
        this.previewImageWidth.setValue(previewImageWidth);
    }

    public final @NotNull BooleanProperty colorImagesRequestedProperty()
    {
        return colorImagesRequested;
    }
    public final @NotNull BooleanProperty mipmapsRequestedProperty()
    {
        return mipmapsRequested;
    }

    public final @NotNull BooleanProperty compressionRequestedProperty()
    {
        return compressionRequested;
    }

    public final @NotNull BooleanProperty alphaRequestedProperty()
    {
        return alphaRequested;
    }

    public final @NotNull BooleanProperty depthImagesRequestedProperty()
    {
        return depthImagesRequested;
    }

    public final @NotNull IntegerProperty depthImageWidthProperty()
    {
        return depthImageWidth;
    }

    public final @NotNull IntegerProperty depthImageHeightProperty()
    {
        return depthImageHeight;
    }

    public final @NotNull IntegerProperty previewImageWidthProperty()
    {
        return previewImageWidth;
    }

    public final @NotNull IntegerProperty previewImageHeightProperty()
    {
        return previewImageHeight;
    }

    public final @NotNull IntegerProperty maxLoadingThreadsProperty()
    {
        return maxLoadingThreads;
    }
}
