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

package kintsugi3d.builder.core.viewset;

import kintsugi3d.builder.core.metrics.ReadonlyViewRMSE;
import kintsugi3d.gl.util.ImageHelper;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;
import kintsugi3d.util.ImageFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

/**
 * Container for different data used by ViewSet. Contains cameraPose, cameraPoseInv, cameraProjectionIndex
 * lightIndex, imageFile, maskFile, and viewErrorMetrics.
 */
public class View
{
    private final Matrix4 cameraPose;
    private final Matrix4 cameraPoseInverse;
    private final int cameraProjectionIndex;
    private final int lightIndex;
    private final @NotNull File imageFile;
    private final ReadonlyViewRMSE viewErrorMetric;

    private File maskFile;
    private int gpuViewIndex;
    private boolean isEnabled;

    private final ReadonlyViewSet containingViewSet;


    /**
     * Creates a new view set data object with parameters for each member.
     * @param cameraPose
     * @param cameraPoseInverse
     * @param cameraProjectionIndex
     * @param lightIndex
     * @param gpuViewIndex
     * @param imageFile Cannot be null.
     * @param maskFile
     * @param viewErrorMetric
     * @param containingViewSet
     */
    View(Matrix4 cameraPose, Matrix4 cameraPoseInverse, int cameraProjectionIndex, int lightIndex,
         int gpuViewIndex, @NotNull File imageFile, File maskFile, ReadonlyViewRMSE viewErrorMetric,
         ReadonlyViewSet containingViewSet)
    {
        if (imageFile == null)
        {
            throw new IllegalArgumentException("Image file cannot be null.");
        }

        this.cameraPose = cameraPose;
        this.cameraPoseInverse = cameraPoseInverse;
        this.cameraProjectionIndex = cameraProjectionIndex;
        this.lightIndex = lightIndex;
        this.gpuViewIndex = gpuViewIndex;
        this.imageFile = imageFile;
        this.maskFile = maskFile;
        this.viewErrorMetric = viewErrorMetric;
        this.containingViewSet = containingViewSet;
        isEnabled = true;
    }

    /**
     * A camera pose defining the transformation from object space to camera space for each view.
     * These are necessary to perform projective texture mapping.
     */
    public final Matrix4 getCameraPose()
    {
        return cameraPose;
    }

    /**
     * An inverted camera pose defining the transformation from camera space to object space for each view.
     * (Useful for visualizing the cameras on screen).
     */
    public final Matrix4 getCameraPoseInverse()
    {
        return cameraPoseInverse;
    }

    /**
     * An entry which designates the index of the projection transformation that should be used for each view.
     */
    public final int getCameraProjectionIndex()
    {
        return cameraProjectionIndex;
    }

    public final Projection getCameraProjection()
    {
        return containingViewSet.getCameraProjection(cameraProjectionIndex);
    }

    public final Matrix4 getProjectionMatrix()
    {
        return getCameraProjection().getProjectionMatrix(
            containingViewSet.getRecommendedNearPlane(), containingViewSet.getRecommendedFarPlane());
    }

    /**
     * An entry which designates the index of the light source position and intensity that should be used for each view.
     */
    public final int getLightIndex()
    {
        return lightIndex;
    }

    public final Vector3 getLightPosition()
    {
        return containingViewSet.getLightPosition(lightIndex);
    }

    public final Vector3 getLightIntensity()
    {
        return containingViewSet.getLightIntensity(lightIndex);
    }

    /**
     * The relative path of the image file corresponding to this view.
     * The file paths are relative to the fullResImageDirectory
     */
    public final @NotNull File getImageFile()
    {
        return imageFile;
    }

    /**
     * Only intended to be modified by ViewSet.optimizeGPUIndexing, during initialization by ViewSetBuilder.
     */
    public final int getGPUViewIndex()
    {
        return gpuViewIndex;
    }

    final void setGpuViewIndex(int gpuViewIndex)
    {
        this.gpuViewIndex = gpuViewIndex;
    }

    public final ReadonlyViewRMSE getViewErrorMetric()
    {
        return viewErrorMetric;
    }

    public final boolean isEnabled()
    {
        return isEnabled;
    }

    final void setEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }

    public final ReadonlyViewSet getContainingViewSet()
    {
        return containingViewSet;
    }

    public final @NotNull File getFullResImageFile()
    {
        return new File(containingViewSet.getFullResImageDirectory(), imageFile.getPath());
    }

    public final @NotNull File findFullResImageFile() throws FileNotFoundException
    {
        return ImageFinder.findImageFile(getFullResImageFile());
    }

    public final @Nullable File tryFindFullResImageFile()
    {
        return ImageFinder.tryFindImageFile(getFullResImageFile());
    }

    public final @NotNull File getPreviewImageFile(@NotNull String extension)
    {
        return new File(containingViewSet.getPreviewImageDirectory(),
            ImageFinder.getImageFileNameWithExtension(imageFile.getName(), extension));
    }

    public final @NotNull File getPreviewImageFile()
    {
        // Use PNG for preview images by default (TODO: make this a configurable setting?)
        return getPreviewImageFile("png");
    }

    public final @NotNull File findPreviewImageFile() throws FileNotFoundException
    {
        return ImageFinder.findImageFile(getPreviewImageFile());
    }

    public final @Nullable File tryFindPreviewImageFile()
    {
        return ImageFinder.tryFindImageFile(getPreviewImageFile());
    }

    public final @NotNull File getThumbnailImageFile(@NotNull String extension)
    {
        return new File(containingViewSet.getThumbnailImageDirectory(),
            ImageFinder.getImageFileNameWithExtension(imageFile.getName(), extension));
    }

    public final @NotNull File getThumbnailImageFile()
    {
        return getThumbnailImageFile("png");
    }

    public final @NotNull File findThumbnailImageFile() throws FileNotFoundException
    {
        return ImageFinder.findImageFile(getThumbnailImageFile());
    }

    public final @Nullable File tryFindThumbnailImageFile()
    {
        return ImageFinder.tryFindImageFile(getThumbnailImageFile());
    }

    public final @Nullable File getMaskFile()
    {
        if (maskFile == null || containingViewSet.getMasksDirectory() == null)
        {
            // Not all images have masks, so this file may still not exist
            return null;
        }
        else
        {
            return new File(containingViewSet.getMasksDirectory(), maskFile.getName());
        }
    }

    final void setMaskFile(File maskFile)
    {
        this.maskFile = maskFile;
    }

    public final ImageHelper loadFullResMaskedImage() throws IOException
    {
        return ImageHelper.read(findFullResImageFile()).withAlphaMask(getMaskFile());
    }

    @Override
    public final @NotNull String toString()
    {
        return imageFile.getName();
    }

    public final @NotNull View copy(ViewSet newContainingViewSet)
    {
        // TODO do we need to also do a deep copy of viewErrorMetric?
        return new View(cameraPose, cameraPoseInverse, cameraProjectionIndex, lightIndex,
            gpuViewIndex, imageFile, maskFile, viewErrorMetric, newContainingViewSet);
    }

    @Override
    public final boolean equals(Object obj)
    {
        // Two views are equal either if they have the same memory address / identity OR
        // if they are distinct objects with the same, non-null image file path.
        return obj == this || ((obj instanceof View) && imageFile.equals(((View) obj).imageFile));
    }

    @Override
    public final int hashCode()
    {
        // If imageFile is not null, use it as a hash code.
        // This ensures that two objects that are equals() will have the same hashCode()
        // either when they are the same object (with a non-null image file path)
        // or if they are different objects with the same, non-null image file path.
        // Otherwise, use the default Object hashCode which covers the case when this object has a null imageFile
        // (and thus is only equal to another reference to itself).
        return Objects.hashCode(imageFile);
    }
}
