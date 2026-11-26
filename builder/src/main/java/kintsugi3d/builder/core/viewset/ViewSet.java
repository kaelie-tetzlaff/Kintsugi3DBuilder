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

import kintsugi3d.builder.core.metrics.ViewRMSE;
import kintsugi3d.builder.core.viewset.events.LightCalibrationChangedEvent;
import kintsugi3d.builder.core.viewset.events.LightCalibrationChangedListener;
import kintsugi3d.builder.core.viewset.events.LuminanceEncodingChangedEvent;
import kintsugi3d.builder.core.viewset.events.LuminanceEncodingChangedListener;
import kintsugi3d.builder.state.settings.GeneralSettingsModel;
import kintsugi3d.builder.state.settings.SimpleGeneralSettingsModel;
import kintsugi3d.builder.util.events.*;
import kintsugi3d.builder.util.events.MappedChange.Type;
import kintsugi3d.builder.util.events.Observable;
import kintsugi3d.builder.util.events.Observer;
import kintsugi3d.gl.builders.ProgramBuilder;
import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.Program;
import kintsugi3d.gl.nativebuffer.NativeDataType;
import kintsugi3d.gl.nativebuffer.NativeVectorBuffer;
import kintsugi3d.gl.nativebuffer.NativeVectorBufferFactory;
import kintsugi3d.gl.nativebuffer.ReadonlyNativeVectorBuffer;
import kintsugi3d.gl.vecmath.Matrix3;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;
import kintsugi3d.util.ImageFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class representing a collection of photographs, or views.
 *
 */
public final class ViewSet implements ReadonlyViewSet
{
    private static final Logger LOG = LoggerFactory.getLogger(ViewSet.class);

    private final Observable<MappedChange<File, View>> viewsObservable = new SimpleObservable<>();

    private final EventDispatcher<LightCalibrationChangedListener, LightCalibrationChangedEvent> lightCalibrationChanged
        = new EventDispatcher<>(LightCalibrationChangedListener::onLightCalibrationChanged);
    private final EventDispatcher<LuminanceEncodingChangedListener, LuminanceEncodingChangedEvent> luminanceEncodingChanged
        = new EventDispatcher<>(LuminanceEncodingChangedListener::onLuminanceEncodingChanged);

    /**
     * A unique id given to each view set that can be used to prevent cache collisions on disk.
     */
    private UUID uuid = UUID.randomUUID();

    /**
     * Views indexed by nominal file path.
     */
    private final @NotNull Map<File, View> views;

    /**
     * A list of projection transformations defining the intrinsic properties of each camera.
     * This list can be much smaller than the number of views if the same intrinsic properties apply for multiple views.
     * This array may be added to but should never be removed from as indices are expected to be persistent.
     */
    private final @NotNull List<Projection> cameraProjectionList;

    /**
     * A list of light source positions, used only for reflectance fields and illumination-dependent rendering (ignored for light fields).
     * Assumed by convention to be in camera space.
     * This list can be much smaller than the number of views if the same illumination conditions apply for multiple views.
     * This array may be added to but should never be removed from as indices are expected to be persistent.
     */
    private final @NotNull List<Vector3> lightPositionList;

    /**
     * A list of light source intensities, used only for reflectance fields and illumination-dependent rendering (ignored for light fields).
     * This list can be much smaller than the number of views if the same illumination conditions apply for multiple views.
     * This array may be added to but should never be removed from as indices are expected to be persistent.
     */
    private final @NotNull List<Vector3> lightIntensityList;

    /**
     * The reference linear luminance values used for decoding pixel colors.
     */
    private double @Nullable [] linearLuminanceValues;

    /**
     * The reference encoded luminance values used for decoding pixel colors.
     */
    private byte @Nullable [] encodedLuminanceValues;

    /**
     * Synchronizes access to linear and encoded luminance values
     * (i.e. ensures that if they are changed mid-operation we don't have a situation
     * where old linear values are used with new encoded values or vice-versa).
     */
    private final Object luminanceEncodingLock = new Object();

    /**
     * The absolute file path to be used for loading all resources.
     */
    private volatile File rootDirectory;

    /**
     * The directory to be used for loading images. It is an absolute file path.
     */
    private volatile @Nullable File fullResImageDirectory;

    /**
     * The directory to be used for saving preview images.
     */
    private volatile @Nullable File previewImageDirectory;

    /**
     * The directory where the results of the texture / specular fitting are stored
     */
    private volatile @Nullable File supportingFilesDirectory;

    /**
     * The directory where thumbnail images are stored
     */
    private volatile @Nullable File thumbnailImageDirectory;

    /**
     * The directory where the masks are stored, if any are present (null if no masks)
     */
    private volatile @Nullable File masksDirectory;

    /**
     * The mesh file.
     */
    private volatile @Nullable File geometryFile;

    /**
     * The recommended near plane to use when rendering this view set.
     */
    private volatile float recommendedNearPlane = 0.01f;

    /**
     * The recommended far plane to use when rendering this view set.
     */
    private volatile float recommendedFarPlane = 100.0f;

    /**
     * The view used for color calibration
     */
    private volatile @Nullable View primaryView;

    /**
     * The view used to reorient the model
     */
    private volatile @Nullable View orientationView;

    private int gpuBufferSize = 0;

    /**
     * Roll rotation of the orientation view, used to correct sideways or upside down images
     */
    private volatile double orientationViewRotationDegrees = 0;

    /**
     * Orientation imported, to be applied to the model.
     * Should not be changed once object has been created.
     */
    private Matrix3 orientationMatrix;

    /**
     * Object translation imported, to be applied to the model.
     * Should not be changed once object has been created.
     */
    private Vector3 objectTranslation;

    /**
     * Object scale imported, to be applied to the model.
     * Should not be changed once object has been created.
     */
    private float objectScale = 1.0f;

    private volatile int previewWidth = 0;
    private volatile int previewHeight = 0;

    private final GeneralSettingsModel projectSettings = new SimpleGeneralSettingsModel();
    private final Map<String, File> resourceMap = new HashMap<>(32);

    /**
     * Should not be changed once object has been created.
     */
    private boolean hasUnsupportedCorrections = false;

    public static @NotNull ViewSetBuilder getBuilder(File rootDirectory, int initialCapacity)
    {
        return new ViewSetBuilder(rootDirectory, initialCapacity);
    }

    public static @NotNull ViewSetBuilder getBuilder(File rootDirectory, @Nullable File supportingFilesDirectory, int initialCapacity)
    {
        return new ViewSetBuilder(rootDirectory, supportingFilesDirectory, initialCapacity);
    }

    /**
     * Creates a new view set object.
     *
     * @param initialCapacity The capacity to use for initializing array-based lists that scale with the number of views
     */
    public ViewSet(int initialCapacity)
    {
        views = Collections.synchronizedMap(new HashMap<>(initialCapacity));

        // Often these lists will have just one element
        cameraProjectionList = Collections.synchronizedList(new ArrayList<>(1));
        lightIntensityList = Collections.synchronizedList(new ArrayList<>(1));
        lightPositionList = Collections.synchronizedList(new ArrayList<>(1));
    }

    /**
     *
     * @return true if the camera file being loaded contains correction flag, false otherwise
     */
    public boolean hasUnsupportedCorrections()
    {
        return hasUnsupportedCorrections;
    }

    /**
     * Set whether the camera file being loaded contains correction flag that is currently unsupported.
     * @param hasUnsupportedCorrections true if the camera file contains correction flag, false otherwise
     * @return ViewSet.Builder instance
     */
    void setHasUnsupportedCorrections(boolean hasUnsupportedCorrections)
    {
        this.hasUnsupportedCorrections = hasUnsupportedCorrections;
    }

    @Override
    public Matrix3 getOrientationMatrix()
    {
        return orientationMatrix;
    }

    void setOrientationMatrix(Matrix3 orientationMatrix)
    {
        this.orientationMatrix = orientationMatrix;
    }

    @Override
    public Vector3 getObjectTranslation()
    {
        return objectTranslation;
    }

    void setObjectTranslation(Vector3 objectTranslation)
    {
        this.objectTranslation = objectTranslation;
    }

    @Override
    public float getObjectScale()
    {
        return objectScale;
    }

    void setObjectScale(float objectScale)
    {
        this.objectScale = objectScale;
    }

    @Override
    public @Nullable ReadonlyNativeVectorBuffer getCameraPoseData()
    {
        synchronized (views)
        {
            // Store the poses in a uniform buffer
            if (views.isEmpty())
            {
                return null;
            }
            else
            {
                // Flatten the camera pose matrices into 16-component vectors and store them in the vertex list data structure.
                NativeVectorBuffer cameraPoseData = NativeVectorBufferFactory.createEmpty(
                    NativeDataType.FLOAT, 16, gpuBufferSize);

                for (View view : views.values())
                {
                    int d = 0;
                    for (int col = 0; col < 4; col++) // column
                    {
                        for (int row = 0; row < 4; row++) // row
                        {
                            cameraPoseData.set(view.getGPUViewIndex(), d, view.getCameraPose().get(row, col));
                            d++;
                        }
                    }
                }

                return cameraPoseData;
            }
        }
    }

    @Override
    public @Nullable ReadonlyNativeVectorBuffer getCameraProjectionData()
    {
        synchronized (cameraProjectionList)
        {
            // Store the camera projections in a uniform buffer
            if (cameraProjectionList.isEmpty())
            {
                return null;
            }
            else
            {
                // Flatten the camera projection matrices into 16-component vectors and store them in the vertex list data structure.
                NativeVectorBuffer cameraProjectionData = NativeVectorBufferFactory.createEmpty(NativeDataType.FLOAT, 16, cameraProjectionList.size());

                for (int k = 0; k < cameraProjectionList.size(); k++)
                {
                    int d = 0;
                    for (int col = 0; col < 4; col++) // column
                    {
                        for (int row = 0; row < 4; row++) // row
                        {
                            Matrix4 projection = cameraProjectionList.get(k).getProjectionMatrix(recommendedNearPlane, recommendedFarPlane);
                            cameraProjectionData.set(k, d, projection.get(row, col));
                            d++;
                        }
                    }
                }
                return cameraProjectionData;
            }
        }
    }

    @Override
    public @Nullable ReadonlyNativeVectorBuffer getCameraProjectionIndexData()
    {
        synchronized (views)
        {
            // Store the camera projection indices in a uniform buffer
            if (views.isEmpty())
            {
                return null;
            }
            else
            {
                int[] indexArray = new int[gpuBufferSize];
                for (View view : views.values())
                {
                    indexArray[view.getGPUViewIndex()] = view.getCameraProjectionIndex();
                }
                return NativeVectorBufferFactory.createFromIntArray(false, 1, gpuBufferSize, indexArray);
            }
        }
    }

    @Override
    public @Nullable ReadonlyNativeVectorBuffer getLightPositionData()
    {
        synchronized (lightPositionList)
        {
            // Store the light positions in a uniform buffer
            if (lightPositionList.isEmpty())
            {
                return null;
            }
            else
            {
                NativeVectorBuffer lightPositionData = NativeVectorBufferFactory.createEmpty(NativeDataType.FLOAT, 4, lightPositionList.size());
                for (int k = 0; k < lightPositionList.size(); k++)
                {
                    lightPositionData.set(k, 0, lightPositionList.get(k).x);
                    lightPositionData.set(k, 1, lightPositionList.get(k).y);
                    lightPositionData.set(k, 2, lightPositionList.get(k).z);
                    lightPositionData.set(k, 3, 1.0f);
                }

                return lightPositionData;
            }
        }
    }

    @Override
    public @Nullable ReadonlyNativeVectorBuffer getLightIntensityData()
    {
        synchronized (lightIntensityList)
        {
            // Store the light positions in a uniform buffer
            if (lightIntensityList.isEmpty())
            {
                return null;
            }
            else
            {
                NativeVectorBuffer lightIntensityData = NativeVectorBufferFactory.createEmpty(NativeDataType.FLOAT, 4, lightIntensityList.size());
                for (int k = 0; k < lightIntensityList.size(); k++)
                {
                    lightIntensityData.set(k, 0, lightIntensityList.get(k).x);
                    lightIntensityData.set(k, 1, lightIntensityList.get(k).y);
                    lightIntensityData.set(k, 2, lightIntensityList.get(k).z);
                    lightIntensityData.set(k, 3, 1.0f);
                }
                return lightIntensityData;
            }
        }
    }

    @Override
    public @Nullable ReadonlyNativeVectorBuffer getLightIndexData()
    {
        synchronized (views)
        {
            // Store the light indices in a uniform buffer
            if (views.isEmpty())
            {
                return null;
            }
            else
            {
                int[] indexArray = new int[gpuBufferSize];
                for (View view : views.values())
                {
                    indexArray[view.getGPUViewIndex()] = view.getLightIndex();
                }
                return NativeVectorBufferFactory.createFromIntArray(false, 1, gpuBufferSize, indexArray);
            }
        }
    }

    public @Nullable ReadonlyNativeVectorBuffer getViewIndexData()
    {
        synchronized (views)
        {
            int[] indexArray = views.values().stream()
                .filter(View::isEnabled)
                .mapToInt(View::getGPUViewIndex)
                .sorted() // Ensure minimal changes to the GPU data -- i.e. if index 0 or ordering in general has particular significance to some shader.
                .toArray();

            // Store the view indices in a uniform buffer
            if (indexArray.length == 0)
            {
                return null;
            }
            else
            {
                return NativeVectorBufferFactory.createFromIntArray(false, 1, indexArray.length, indexArray);
            }
        }
    }

    @Override
    public @NotNull ViewSet copy()
    {
        ViewSet result = new ViewSet(getViewCount());

        result.uuid = uuid;

        synchronized (views)
        {
            for (var entry : views.entrySet())
            {
                result.views.put(entry.getKey(), entry.getValue().copy(result));
            }

            result.gpuBufferSize = gpuBufferSize;

            View currentPrimaryView = primaryView;
            if (currentPrimaryView != null)
            {
                result.primaryView = result.views.get(currentPrimaryView.getImageFile());
            }

            View currentOrientationView = orientationView;
            if (currentOrientationView != null)
            {
                result.orientationView = result.views.get(currentOrientationView.getImageFile());
            }
        }

        synchronized (cameraProjectionList)
        {
            result.cameraProjectionList.addAll(cameraProjectionList);
        }

        synchronized (lightPositionList)
        {
            result.lightPositionList.addAll(lightPositionList);
        }

        synchronized (lightIntensityList)
        {
            result.lightIntensityList.addAll(lightIntensityList);
        }

        synchronized (luminanceEncodingLock)
        {
            if (linearLuminanceValues != null && encodedLuminanceValues != null)
            {
                result.setLuminanceEncoding(
                    Arrays.copyOf(linearLuminanceValues, linearLuminanceValues.length),
                    Arrays.copyOf(encodedLuminanceValues, encodedLuminanceValues.length));
            }
        }

        result.rootDirectory = rootDirectory;
        result.fullResImageDirectory = fullResImageDirectory;
        result.previewImageDirectory = previewImageDirectory;
        result.supportingFilesDirectory = supportingFilesDirectory;
        result.thumbnailImageDirectory = thumbnailImageDirectory;
        result.masksDirectory = masksDirectory;
        result.geometryFile = geometryFile;

        result.recommendedNearPlane = recommendedNearPlane;
        result.recommendedFarPlane = recommendedFarPlane;

        result.orientationViewRotationDegrees = orientationViewRotationDegrees;
        result.orientationMatrix = orientationMatrix;
        result.objectTranslation = objectTranslation;
        result.objectScale = objectScale;

        result.previewWidth = previewWidth;
        result.previewHeight = previewHeight;

        result.projectSettings.copyAllFrom(projectSettings);
        result.resourceMap.putAll(resourceMap);

        return result;
    }

    public static @NotNull ReadonlyViewSet createFromLookAt(@NotNull List<Vector3> viewDir, @NotNull Vector3 center, @NotNull Vector3 up, float distance,
                                                            float nearPlane, float aspect, float sensorWidth, float focalLength)
    {
        ViewSet result = new ViewSet(viewDir.size());

        result.cameraProjectionList.add(new DistortionProjection(sensorWidth, sensorWidth / aspect, focalLength));

        result.recommendedNearPlane = nearPlane;
        result.recommendedFarPlane = 2 * distance - nearPlane;

        result.lightIntensityList.add(new Vector3(distance * distance));
        result.lightPositionList.add(Vector3.ZERO);

        for (int i = 0; i < viewDir.size(); i++)
        {
            File imageFile = new File(String.format("%04d.png", i + 1));
            Matrix4 cameraPose = Matrix4.lookAt(viewDir.get(i).times(-distance).plus(center), center, up);
            Matrix4 cameraPoseInv = cameraPose.quickInverse(0.001f);

            result.addView(new View(cameraPose, cameraPoseInv, 0, 0, i, imageFile, null, new ViewRMSE(), result));
        }

        return result;
    }

    @Override
    public UUID getUUID()
    {
        return uuid;
    }

    /**
     *
     * @param uuid
     */
    void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    public void toggleViewEnabled(@NotNull File image)
    {
        View view = findViewByName(image.getName());
        if (view != null)
        {
            setViewEnabled(image, !view.isEnabled());
        }
    }

    private @NotNull View findViewOrThrow(@NotNull File image)
    {
        return Objects.requireNonNull(findViewByName(image.getName()), String.format("View not found: %s", image));
    }

    public void setViewEnabled(@NotNull File image, boolean isEnabled)
    {
        View view = findViewOrThrow(image);

        if (view.isEnabled()) // Currently enabled, so disable camera
        {
            view.setEnabled(isEnabled);
            if (!isEnabled)
            {
                viewsObservable.notify(new MappedChange<>(Type.MODIFIED, view.getImageFile(), view));
            }
            // Else still enabled so nothing needs to change
        }
        else // Currently disabled, so enable camera
        {
            view.setEnabled(isEnabled);
            if (isEnabled)
            {
                viewsObservable.notify(new MappedChange<>(Type.MODIFIED, view.getImageFile(), view));
            }
            // Else still disabled so nothing needs to change
        }
    }

    public void setViewsEnabled(@NotNull Collection<File> images, boolean isEnabled)
    {
        Map<File, View> imageFilesModified = new HashMap<>(images.size());
        synchronized (views)
        {
            for (File image : images)
            {
                View view = findViewOrThrow(image);
                view.setEnabled(isEnabled);
                imageFilesModified.put(view.getImageFile(), view);
            }
        }

        if (!imageFilesModified.isEmpty())
        {
            viewsObservable.notify(new MappedChange<>(Type.MODIFIED, imageFilesModified));
        }
    }

    @Override
    public File getRootDirectory()
    {
        return rootDirectory;
    }

    /**
     * Sets the root directory for this view set, while leaving other file paths unmodified.
     *
     * @param rootDirectory The root directory.
     */
    public void setRootDirectory(File rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public File getSupportingFilesDirectory()
    {
        // Fallback to root directory if no supporting files defined
        return Optional.ofNullable(supportingFilesDirectory).orElse(rootDirectory);
    }

    @Override
    public @Nullable String getRelativeSupportingFilesPathName()
    {
        File effectiveSupportingFilesDirectory = getSupportingFilesDirectory();
        try
        {
            return getAbsoluteRootDirectory().relativize(effectiveSupportingFilesDirectory.toPath().toAbsolutePath()).toString();
        }
        catch (RuntimeException e) // If the root and other directories are located under different drive letters on Windows
        {
            LOG.warn("Could not relativize supporting files directory ({}) using root directory {}",
                effectiveSupportingFilesDirectory, getAbsoluteRootDirectory(), e);
            return effectiveSupportingFilesDirectory == null ? null : effectiveSupportingFilesDirectory.toString();
        }
    }

    /**
     * Sets the absolute file path of the supporting files (i.e. texture fit results) associated with this view set.
     *
     * @param supportingFilesDirectory The file path of the supporting files directory.
     */
    public void setSupportingFilesDirectory(@Nullable File supportingFilesDirectory)
    {
        this.supportingFilesDirectory = supportingFilesDirectory;

        // TODO would this work?
        thumbnailImageDirectory = new File(supportingFilesDirectory, "thumbnails");
    }

    @Override
    public File getFullResImageDirectory()
    {
        return Optional.ofNullable(fullResImageDirectory)
            .orElse(Optional.ofNullable(previewImageDirectory)
                .orElse(rootDirectory));
    }

    /**
     * Sets the absolute image file directory associated with this view set.
     *
     * @param absoluteImageDirectory The image file path.
     */
    public void setFullResImageDirectory(@Nullable File absoluteImageDirectory)
    {
        fullResImageDirectory = absoluteImageDirectory;
    }

    @Override
    public @Nullable String getRelativeFullResImagePathName()
    {
        File effectiveFullResImageDirectory = getFullResImageDirectory();

        try
        {
            return getAbsoluteRootDirectory().relativize(effectiveFullResImageDirectory.toPath().toAbsolutePath()).toString();
        }
        catch (RuntimeException e) // If the root and other directories are located under different drive letters on Windows
        {
            LOG.warn("Could not relativize full resolution image directory ({}) using root directory {}",
                effectiveFullResImageDirectory, getAbsoluteRootDirectory(), e);
            return effectiveFullResImageDirectory == null ? null : effectiveFullResImageDirectory.toString();
        }
    }

    private @NotNull Path getAbsoluteRootDirectory()
    {
        return rootDirectory.toPath().toAbsolutePath();
    }

    /**
     * Sets the image file path associated with this view set from a path relative to the root directory.
     *
     * @param relativeImagePath The image file path.
     */
    public void setRelativeFullResImagePathName(@NotNull String relativeImagePath)
    {
        fullResImageDirectory = getAbsoluteRootDirectory().resolve(relativeImagePath).toFile();
    }

    @Override
    public File getPreviewImageDirectory()
    {
        // If no preview images, default to just using full res images, or root directory as last fallback
        return Optional.ofNullable(previewImageDirectory)
            .orElse(Optional.ofNullable(fullResImageDirectory)
                .orElse(rootDirectory));
    }

    public void setPreviewImageDirectory(@Nullable File previewImageDirectory)
    {
        this.previewImageDirectory = previewImageDirectory;
    }

    @Override
    public File getThumbnailImageDirectory()
    {
        // If no thumbnail images, default to just using full res images, or preview images, or root directory as last fallback
        return Optional.ofNullable(thumbnailImageDirectory).orElse(getFullResImageDirectory());
    }

    @Override
    public @Nullable String getRelativePreviewImagePathName()
    {
        File effectivePreviewImageDirectory = getPreviewImageDirectory();

        try
        {
            return getAbsoluteRootDirectory().relativize(effectivePreviewImageDirectory.toPath().toAbsolutePath()).toString();
        }
        catch (RuntimeException e) // If the root and other directories are located under different drive letters on Windows
        {
            LOG.warn("Could not relativize preview image directory ({}) using root directory {}",
                effectivePreviewImageDirectory, getAbsoluteRootDirectory(), e);
            return effectivePreviewImageDirectory == null ? null : effectivePreviewImageDirectory.toString();
        }
    }

    /**
     * Sets the image file path associated with this view set.
     *
     * @param relativeImagePath The image file path.
     */
    public void setRelativePreviewImagePathName(@NotNull String relativeImagePath)
    {
        previewImageDirectory = getAbsoluteRootDirectory().resolve(relativeImagePath).toFile();
    }

    public void setRelativeThumbnailImagePathName(@NotNull String relativeImagePath)
    {
        thumbnailImageDirectory = getAbsoluteRootDirectory().resolve(relativeImagePath).toFile();
    }

    public void setRelativeMasksPathName(@NotNull String relativeMasksPath)
    {
        masksDirectory = getAbsoluteRootDirectory().resolve(relativeMasksPath).toFile();
    }

    @Override
    public int getPreviewWidth()
    {
        return previewWidth;
    }

    @Override
    public int getPreviewHeight()
    {
        return previewHeight;
    }

    public void setPreviewImageResolution(int width, int height)
    {
        previewWidth = width;
        previewHeight = height;
    }


    @Override
    public @Nullable View getPrimaryView()
    {
        return primaryView;
    }

    public void setPrimaryView(@NotNull View primaryView)
    {
        synchronized (views)
        {
            if (Objects.equals(views.get(primaryView.getImageFile()), primaryView))
            {
                this.primaryView = primaryView;
            }
            else
            {
                viewNotFoundException(primaryView);
            }
        }
    }

    @Override
    public @Nullable View getOrientationView()
    {
        return orientationView;
    }

    /**
     * Set the view to use as a reference pose to reorient the model
     *
     * @param orientationView Can be null to use default orientation.
     */
    public void setOrientationView(@Nullable View orientationView)
    {
        synchronized (views)
        {
            if (orientationView != null)
            {
                if (Objects.equals(views.get(orientationView.getImageFile()), orientationView))
                {
                    this.orientationView = orientationView;
                }
                else
                {
                    viewNotFoundException(orientationView);
                }
            }
            else
            {
                this.orientationView = null;
            }
        }
    }

    /**
     * Set the view to use as a reference pose to reorient the model
     *
     * @param viewName Can be null to use default orientation.
     */
    public void setOrientationViewByName(@Nullable String viewName)
    {
        synchronized (views)
        {
            if (viewName != null)
            {
                View newView = findViewByName(viewName);

                if (newView != null)
                {
                    orientationView = newView;
                }
                else
                {
                    viewNotFoundException(viewName);
                }
            }
            else
            {
                orientationView = null;
            }
        }
    }

    /**
     *
     * @param viewRef can be a string containing the name or the view itself.
     */
    private static void viewNotFoundException(Object viewRef)
    {
        throw new IllegalArgumentException(String.format("View %s is not in the view set", viewRef));
    }

    @Override
    public double getOrientationViewRotationDegrees()
    {
        return orientationViewRotationDegrees;
    }

    public void setOrientationViewRotationDegrees(double rotation)
    {
        orientationViewRotationDegrees = rotation;
    }

    @Override
    public @Nullable View getRepresentativeView()
    {
        synchronized (views)
        {
            View representativeView = orientationView;

            if (representativeView == null)
            {
                // First fallback if no orientation view has been set: primary view for tone calibration
                representativeView = primaryView;

                if (representativeView == null && !views.isEmpty())
                {
                    // Second fallback if primary view is not set:
                    // grab the view with the lowest GPU index (typically the first loaded), if it exists.
                    representativeView = views.values().stream().min(Comparator.comparing(View::getGPUViewIndex)).orElse(null);
                }
            }

            return representativeView;
        }
    }

    public @Nullable View findViewByName(@Nullable String viewName)
    {
        // Treat null as "not found" or "not present"
        // Important for allowing for orientation pose to remain unset.
        if (viewName == null)
        {
            return null;
        }

        synchronized (views)
        {
            // Try simple file comparison with full paths
            File key = new File(viewName);
            View mappedView = views.get(key);

            if (mappedView != null)
            {
                return mappedView;
            }

            // Try just checking file name in case there were parent files
            // i.e. target file is photo314.jpg and imageFiles contains myPhotos/photo314.jpg

            // Also check for extension mismatch
            // i.e. the camera label is photo314.jpg, other times just photo314

            // This is all necessary due to inconsistencies with camera labels in frame.zip and chunk.zip XMLs
            for (View view : views.values())
            {
                String imgName = view.getImageFile().getName();
                String shortenedImgName = removeExt(imgName);
                String shortenedViewName = removeExt(viewName);

                if (shortenedImgName.equals(shortenedViewName) || shortenedImgName.equals(viewName) || imgName.equals(shortenedViewName))
                {
                    return view;
                }
            }

            return null;
        }
    }

    private static @NotNull String removeExt(@NotNull String fileName)
    {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    @Override
    public @NotNull Set<View> getViews()
    {
        // Make a copy to ensure that the list is not changed while another object is using it.
        synchronized (views)
        {
            return Set.copyOf(views.values());
        }
    }

    @Override
    public @NotNull List<View> getViewsSorted()
    {
        // Make a copy to ensure that the list is not changed while another object is using it.
        synchronized (views)
        {
            return views.values().stream().sorted(Comparator.comparing(View::getGPUViewIndex)).collect(Collectors.toList());
        }
    }

    @Override
    public @NotNull Set<View> getEnabledViews()
    {
        synchronized (views)
        {
            return views.values().stream().filter(View::isEnabled).collect(Collectors.toSet());
        }
    }

    @Override
    public @NotNull Set<View> getDisabledViews()
    {
        synchronized (views)
        {
            return views.values().stream().filter(Predicate.not(View::isEnabled)).collect(Collectors.toSet());
        }
    }

    @Override
    public @NotNull List<View> getEnabledViewsSorted()
    {
        synchronized (views)
        {
            return views.values().stream()
                .filter(View::isEnabled)
                .sorted(Comparator.comparing(View::getGPUViewIndex))
                .collect(Collectors.toList());
        }
    }

    @Override
    public @NotNull List<View> getDisabledViewsSorted()
    {
        synchronized (views)
        {
            return views.values().stream()
                .filter(Predicate.not(View::isEnabled))
                .sorted(Comparator.comparing(View::getGPUViewIndex))
                .collect(Collectors.toList());
        }
    }

    void addView(@NotNull View view)
    {
        synchronized (views)
        {
            views.put(view.getImageFile(), view);
            gpuBufferSize = Math.max(gpuBufferSize, view.getGPUViewIndex() + 1);

            if (primaryView == null)
            {
                // Set default primary view if none has been specified.
                primaryView = view;
            }
        }

        viewsObservable.notify(new MappedChange<>(Type.ADDED, view.getImageFile(), view));
    }

    public void removeViewByImageFilename(@NotNull File image)
    {
        View removed;

        synchronized (views)
        {
            removed = views.remove(image);

            // Check to see if we just removed the tone calibration primary or orientation views.
            View currentPrimaryView = primaryView;
            if (currentPrimaryView != null && Objects.equals(image.getName(), currentPrimaryView.getImageFile().getName()))
            {
                // Primary view should always be non-null, if possible.
                // NOTE: This will not automatically re-calculate light intensity;
                // to do that, the user will have to go through tone calibration again.
                // This is intentional in case of a workflow where the user performs tone calibration on a photo
                // then deletes it entirely if it is not needed for texture processing -- we want the tone calibration
                // to still be in terms of the light intensity derived from that deleted view!
                primaryView = views.isEmpty() ? null : getRepresentativeView();
            }

            View currentOrientationView = orientationView;
            if (currentOrientationView != null)
            {
                if (Objects.equals(image.getName(), currentOrientationView.getImageFile().getName()))
                {
                    // Orientation view can be null.
                    // TODO notify renderer that the orientation has changed.
                    orientationView = null;
                }
            }
        }

        if (removed != null)
        {
            viewsObservable.notify(new MappedChange<>(Type.REMOVED, removed.getImageFile(), removed));
        }
    }

    @Override
    public Projection getCameraProjection(int projectionIndex)
    {
        synchronized (cameraProjectionList)
        {
            return cameraProjectionList.get(projectionIndex);
        }
    }

    void addCameraProjection(Projection projection)
    {
        synchronized (cameraProjectionList)
        {
            cameraProjectionList.add(projection);
        }
    }

    @Override
    public Vector3 getLightPosition(int lightIndex)
    {
        synchronized (lightPositionList)
        {
            return lightPositionList.get(lightIndex);
        }
    }

    @Override
    public Vector3 getLightIntensity(int lightIndex)
    {
        synchronized (lightIntensityList)
        {
            return lightIntensityList.get(lightIndex);
        }
    }

    public void setLightPosition(int lightIndex, Vector3 lightPosition)
    {
        LightCalibrationChangedEvent event;

        synchronized (lightPositionList)
        {
            lightPositionList.set(lightIndex, lightPosition);
            event = new LightCalibrationChangedEvent(lightPositionList, null);
        }

        lightCalibrationChanged.notify(event);
    }

    public void setAllLightPositions(Vector3 lightPosition)
    {
        LightCalibrationChangedEvent event;
        synchronized (lightPositionList)
        {
            for (int i = 0; i < getLightCount(); i++)
            {
                lightPositionList.set(i, lightPosition);
            }

            event = new LightCalibrationChangedEvent(lightPositionList, null);
        }

        lightCalibrationChanged.notify(event);
    }

    public void setLightIntensity(int lightIndex, Vector3 lightIntensity)
    {
        LightCalibrationChangedEvent event;
        synchronized (lightIntensityList)
        {
            lightIntensityList.set(lightIndex, lightIntensity);
            event = new LightCalibrationChangedEvent(null, lightIntensityList);
        }

        lightCalibrationChanged.notify(event);
    }

    public void setAllLightIntensities(Vector3 lightIntensity)
    {
        LightCalibrationChangedEvent event;
        synchronized (lightIntensityList)
        {
            for (int i = 0; i < getLightCount(); i++)
            {
                lightIntensityList.set(i, lightIntensity);
            }

            event = new LightCalibrationChangedEvent(null, lightIntensityList);
        }

        lightCalibrationChanged.notify(event);
    }

    void addLight(Vector3 position, Vector3 intensity)
    {
        List<Vector3> lightIntensityListCopy;

        synchronized (lightIntensityList)
        {
            lightIntensityList.add(intensity);

            // Copy here just in case for synchronization.
            lightIntensityListCopy = new ArrayList<>(lightIntensityList);
        }

        // Do lightPositionList last since that is what getLightCount is based on.
        // That way it should be less likely that we get an index out of bounds due to concurrency issues.
        LightCalibrationChangedEvent event;
        synchronized (lightPositionList)
        {
            lightPositionList.add(position);
            event = new LightCalibrationChangedEvent(lightPositionList, lightIntensityListCopy);
        }

        lightCalibrationChanged.notify(event);
    }

    @Override
    public int getViewCount()
    {
        synchronized (views)
        {
            return views.size();
        }
    }

    @Override
    public int getEnabledViewCount()
    {
        synchronized (views)
        {
            return (int) views.values().stream().filter(View::isEnabled).count();
        }
    }

    @Override
    public int getDisabledViewCount()
    {
        synchronized (views)
        {
            return (int) views.values().stream().filter(Predicate.not(View::isEnabled)).count();
        }
    }

    @Override
    public int getGPUBufferSize()
    {
        synchronized (views)
        {
            return gpuBufferSize;
        }
    }

    /**
     * Only intended to be used by ViewSetBuilder during initialization.
     */
    void optimizeGPUIndexing()
    {
        synchronized (views)
        {
            // Keep views ordered by original GPU index as much as possible.
            List<View> sortedViews = getViewsSorted();

            // Reassign view indices for smaller data set.
            for (int i = 0; i < sortedViews.size(); ++i)
            {
                sortedViews.get(i).setGpuViewIndex(i);
            }
            gpuBufferSize = views.size();
        }
    }

    @Override
    public int getCameraProjectionCount()
    {
        synchronized (cameraProjectionList)
        {
            return cameraProjectionList.size();
        }
    }

    @Override
    public int getLightCount()
    {
        synchronized (lightPositionList)
        {
            return lightPositionList.size();
        }
    }

    @Override
    public float getRecommendedNearPlane()
    {
        return recommendedNearPlane;
    }

    @Override
    public float getRecommendedFarPlane()
    {
        return recommendedFarPlane;
    }

    void setRecommmendedClipPlanes(float near, float far)
    {
        recommendedNearPlane = near;
        recommendedFarPlane = far;
    }

    @Override
    public boolean hasCustomLuminanceEncoding()
    {
        synchronized (luminanceEncodingLock)
        {
            return linearLuminanceValues != null && encodedLuminanceValues != null
                && linearLuminanceValues.length > 0 && encodedLuminanceValues.length > 0;
        }
    }

    @Override
    public @NotNull SampledLuminanceEncoding getLuminanceEncoding()
    {
        synchronized (luminanceEncodingLock)
        {
            if (hasCustomLuminanceEncoding())
            {
                assert linearLuminanceValues != null;
                assert encodedLuminanceValues != null;
                return new SampledLuminanceEncoding(linearLuminanceValues, encodedLuminanceValues);
            }
            else
            {
                return new SampledLuminanceEncoding();
            }
        }
    }

    @Override
    public double @Nullable [] getLinearLuminanceValues()
    {
        synchronized (luminanceEncodingLock)
        {
            double[] currentValues = linearLuminanceValues;
            return currentValues == null ? null : Arrays.copyOf(currentValues, currentValues.length);
        }
    }

    @Override
    public byte @Nullable [] getEncodedLuminanceValues()
    {
        synchronized (luminanceEncodingLock)
        {
            byte[] currentValues = encodedLuminanceValues;
            return currentValues == null ? null : Arrays.copyOf(currentValues, currentValues.length);
        }
    }

    public void setLuminanceEncoding(double @NotNull [] linearLuminanceValues, byte @NotNull [] encodedLuminanceValues)
    {
        if (linearLuminanceValues.length != encodedLuminanceValues.length)
        {
            throw new IllegalArgumentException("Arrays must be of equal length.");
        }

        LuminanceEncodingChangedEvent event;

        synchronized (luminanceEncodingLock)
        {
            this.linearLuminanceValues = linearLuminanceValues.clone();
            this.encodedLuminanceValues = encodedLuminanceValues.clone();

            event = new LuminanceEncodingChangedEvent(this.linearLuminanceValues, this.encodedLuminanceValues);
        }

        luminanceEncodingChanged.notify(event);
    }

    public void clearLuminanceEncoding()
    {
        synchronized (luminanceEncodingLock)
        {
            linearLuminanceValues = null;
            encodedLuminanceValues = null;
        }

        luminanceEncodingChanged.notify(new LuminanceEncodingChangedEvent(null, null));
    }

    @Override
    public boolean hasMasks()
    {
        return masksDirectory != null;
    }

    @Override
    public @Nullable File getMasksDirectory()
    {
        return masksDirectory;
    }

    public void setMasksDirectory(@Nullable File dir)
    {
        masksDirectory = dir;
    }

    @Override
    public @Nullable String getRelativeMaskPathName()
    {
        File masksDirectoryRef = masksDirectory;
        if (masksDirectoryRef == null)
        {
            return null;
        }
        else
        {
            try
            {
                return getAbsoluteRootDirectory().relativize(masksDirectoryRef.toPath().toAbsolutePath()).toString();
            }
            catch (RuntimeException e) // If the root and other directories are located under different drive letters on Windows
            {
                LOG.warn("Could not relativize masks directory ({}) using root directory {}", masksDirectoryRef, getAbsoluteRootDirectory(), e);
                return masksDirectoryRef.toString();
            }
        }
    }

    /**
     * Checks that all mask files exist.  In doing so, it tries several variations (i.e. mask filename vs. photo filename,
     * _mask vs. no _mask, various file extensions) and changes the recorded mask filename for each view to an image that was found
     * (or eliminating the mask if the file is missing).
     */
    public void validateMasks()
    {
        synchronized (views)
        {
            for (View view : views.values())
            {
                File maskFile = view.getMaskFile();
                if (maskFile != null)
                {
                    File originalMaskFile = maskFile; // remember the original filename for logging

                    // Could set maskFile to null if it doesn't actually exist,
                    // or change the file extension if it exists with a different file extension.
                    maskFile = ImageFinder.tryFindImageFile(maskFile);

                    if (maskFile == null)
                    {
                        LOG.warn("Specified mask file not found: {}", originalMaskFile.getPath());
                    }
                }

                if (maskFile == null)
                {
                    // Search for the name of the photo in the masks directory
                    // Will check both with and without _mask suffix
                    maskFile = ImageFinder.tryFindImageFile(
                        new File(masksDirectory, view.getFullResImageFile().getName()),
                        "_mask");
                }

                // Remove if no mask file was found, otherwise overwrite based on the file that was found
                view.setMaskFile(maskFile);
            }
        }
    }

    @Override
    public @Nullable String getGeometryFileName()
    {
        // Grab reference for thread synchronization, just in case.
        File geometryFileRef = geometryFile;
        if (geometryFileRef == null)
        {
            return null;
        }
        else
        {
            try
            {
                return getAbsoluteRootDirectory().relativize(geometryFileRef.toPath().toAbsolutePath()).toString();
            }
            catch (RuntimeException e) // If the root and other directories are located under different drive letters on windows
            {
                LOG.warn("Could not relativize geometry file ({}) using root directory {}", geometryFileRef, getAbsoluteRootDirectory(), e);
                return geometryFileRef.toString();
            }
        }
    }

    @Override
    public @Nullable File getGeometryFile()
    {
        return geometryFile;
    }

    /**
     * Sets the absolute path of the geometry file associated with this view set.
     *
     * @param geometryFile The geometry file.
     */
    public void setGeometryFile(@Nullable File geometryFile)
    {
        this.geometryFile = geometryFile;
    }

    @Override
    public @NotNull GeneralSettingsModel getProjectSettings()
    {
        return projectSettings;
    }

    @Override
    public @NotNull Map<String, File> getResourceMap()
    {
        return Collections.unmodifiableMap(resourceMap);
    }

    void putResources(@NotNull Map<String, File> newResourceMap)
    {
        resourceMap.putAll(newResourceMap);
    }

    @Override
    public <ContextType extends Context<ContextType>> ProgramBuilder<ContextType> getShaderProgramBuilder(@NotNull ContextType context)
    {
        // Determine shader defines here that should apply globally as defaults without require specific resources other than view set data.
        // The defines can be overridden by the actual shader.
        return context.getShaderProgramBuilder()
            .define("VIEW_COUNT", getEnabledViewCount())
            .define("CAMERA_POSE_COUNT", getGPUBufferSize())
            .define("CAMERA_PROJECTION_COUNT", getCameraProjectionCount())
            .define("LIGHT_COUNT", getLightCount())
            .define("INFINITE_LIGHT_SOURCES", projectSettings.getBoolean("infiniteLightSources"))
            .define("FLATFIELD_CORRECTED", projectSettings.getBoolean("flatfieldCorrected"))
            .define("LUMINANCE_MAP_ENABLED", hasCustomLuminanceEncoding())
            .define("INVERSE_LUMINANCE_MAP_ENABLED", hasCustomLuminanceEncoding())
            .define("VISIBILITY_TEST_ENABLED", projectSettings.getBoolean("occlusionEnabled"))
            .define("SHADOW_TEST_ENABLED", projectSettings.getBoolean("occlusionEnabled"))
            .define("EDGE_PROXIMITY_WEIGHT_ENABLED", projectSettings.getBoolean("edgeProximityWeightEnabled"));
    }

    @Override
    public <ContextType extends Context<ContextType>> void setupShaderProgram(@NotNull Program<ContextType> program)
    {
        // Determine shader uniforms here that should apply globally as defaults without require specific resources other than view set data.
        // The uniforms can be overridden by the actual shader.
        program.setUniform("occlusionBias", projectSettings.getFloat("occlusionBias"));
        program.setUniform("edgeProximityMargin", projectSettings.getFloat("edgeProximityMargin"));
        program.setUniform("edgeProximityCutoff", projectSettings.getFloat("edgeProximityCutoff"));
    }

    public @NotNull EventListeners<Observer<MappedChange<File, View>>> viewsChangedListeners()
    {
        return viewsObservable;
    }

    public @NotNull EventListeners<LightCalibrationChangedListener> lightCalibrationChangedListeners()
    {
        return lightCalibrationChanged;
    }

    public @NotNull EventListeners<LuminanceEncodingChangedListener> luminanceEncodingChangedListeners()
    {
        return luminanceEncodingChanged;
    }
}