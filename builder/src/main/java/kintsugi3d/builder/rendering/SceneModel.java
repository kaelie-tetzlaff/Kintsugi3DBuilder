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

package kintsugi3d.builder.rendering;

import kintsugi3d.builder.state.SelectableViewListModel;
import kintsugi3d.builder.state.scene.ReadonlyLightingEnvironmentModel;
import kintsugi3d.builder.state.scene.ReadonlyObjectPoseModel;
import kintsugi3d.builder.state.scene.ReadonlyViewpointModel;
import kintsugi3d.builder.state.settings.DefaultGlobalSettingsModel;
import kintsugi3d.builder.state.settings.ReadonlyGeneralSettingsModel;
import kintsugi3d.builder.state.settings.SafeReadonlyGlobalSettingsModel;
import kintsugi3d.builder.state.settings.SafeSettingsModelWrapperFactory;
import kintsugi3d.gl.core.FramebufferSize;
import kintsugi3d.gl.vecmath.Matrix3;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;

public class SceneModel
{
    private ReadonlyObjectPoseModel objectModel;
    private ReadonlyViewpointModel cameraModel;
    private ReadonlyLightingEnvironmentModel lightingModel;
    private SafeReadonlyGlobalSettingsModel settingsModel = new DefaultGlobalSettingsModel();
    private SelectableViewListModel viewListModel;

    private Vector3 centroid = Vector3.ZERO;
    private Matrix3 orientation = Matrix3.IDENTITY;
    private float scale = 1.0f;

    private Vector3 clearColor = Vector3.ZERO;

    public final ReadonlyObjectPoseModel getObjectModel()
    {
        return objectModel;
    }

    public final ReadonlyViewpointModel getCameraModel()
    {
        return cameraModel;
    }

    public final ReadonlyLightingEnvironmentModel getLightingModel()
    {
        return lightingModel;
    }

    public final SafeReadonlyGlobalSettingsModel getSettingsModel()
    {
        return this.settingsModel;
    }

    public final SelectableViewListModel getCameraViewListModel()
    {
        return viewListModel;
    }

    public final Vector3 getCentroid()
    {
        return centroid;
    }

    public final Matrix3 getOrientation()
    {
        return orientation;
    }

    public final float getScale()
    {
        return this.scale;
    }

    public final Vector3 getClearColor()
    {
        return clearColor;
    }
    public final void setObjectModel(ReadonlyObjectPoseModel objectModel)
    {
        this.objectModel = objectModel;
    }

    public final void setCameraModel(ReadonlyViewpointModel cameraModel)
    {
        this.cameraModel = cameraModel;
    }

    public final void setLightingModel(ReadonlyLightingEnvironmentModel lightingModel)
    {
        this.lightingModel = lightingModel;
    }

    public final void setSettingsModel(ReadonlyGeneralSettingsModel settingsModel)
    {
        this.settingsModel = SafeSettingsModelWrapperFactory.wrapUnsafeModel(settingsModel);
    }

    public final void setCameraViewListModel(SelectableViewListModel viewListModel)
    {
        this.viewListModel = viewListModel;
    }

    public final void setCentroid(Vector3 centroid)
    {
        this.centroid = centroid;
    }

    public final void setOrientation(Matrix3 orientation)
    {
        this.orientation = orientation;
    }

    public final void setScale(float scale)
    {
        this.scale = scale;
    }

    public final void setClearColor(Vector3 clearColor)
    {
        this.clearColor = clearColor;
    }

    public final Matrix4 getLightViewMatrix(int lightIndex)
    {
        return getUnscaledMatrix(lightingModel.getLightMatrix(lightIndex));
    }

    public final Matrix4 getInverseLightViewMatrix(int lightIndex)
    {
        return getUnscaledMatrix(lightingModel.getLightMatrix(lightIndex).quickInverse(0.01f));
    }

    public final Matrix4 getLightModelViewMatrix(int lightIndex)
    {
        return getUnscaledMatrix(
            lightingModel.getLightMatrix(lightIndex)
                .times(objectModel.getTransformationMatrix()))
            .times(getBaseModelMatrix());
    }

    public final Matrix4 getEnvironmentMapMatrix(Matrix4 modelMatrix)
    {
        return getUnscaledMatrix(lightingModel.getEnvironmentMapMatrix()).times(modelMatrix);
    }

    public final Matrix4 getCurrentViewMatrix()
    {
        return getUnscaledMatrix(cameraModel.getLookMatrix());
    }

    public final Matrix4 getViewMatrixFromCameraPose(Matrix4 cameraPoseMatrix)
    {
        return cameraPoseMatrix
                .times(Matrix4.translate(this.centroid))
                .times(orientation.asMatrix4().quickInverse(0.01f));
    }

    public final Matrix4 getCameraPoseFromViewMatrix(Matrix4 cameraPoseMatrix)
    {
        return cameraPoseMatrix
                .times(getBaseModelMatrix());
    }

    public final Matrix4 getUnscaledMatrix(Matrix4 scaledMatrix)
    {
        return Matrix4.scale(scale)
                .times(scaledMatrix)
                .times(Matrix4.scale(1.0f / scale));
    }

    /**
     * Recenters and reorients the model using base model matrix, then applies the user-specified model transformation.
     * @return
     */
    public final Matrix4 getFullModelMatrix()
    {
        return getUnscaledMatrix(this.objectModel.getTransformationMatrix()).times(getBaseModelMatrix());
    }

    /**
     * Just recentering and reorienting the model
     * @return
     */
    public final Matrix4 getBaseModelMatrix()
    {
        return orientation.asMatrix4()
                .times(Matrix4.translate(this.centroid.negated()));
    }

    public final Matrix4 getModelViewMatrix(Matrix4 view)
    {
        return view.times(getFullModelMatrix());
    }

    public final Matrix4 getViewFromModelViewMatrix(Matrix4 modelViewMatrix)
    {
        return modelViewMatrix
                .times(Matrix4.translate(this.centroid))
                .times(orientation.asMatrix4().quickInverse(0.01f))
                .times(getUnscaledMatrix(this.objectModel.getTransformationMatrix().quickInverse(0.01f)));
    }

    public final float getVerticalFieldOfView(FramebufferSize size)
    {
        return 2 * (float) StrictMath.atan(StrictMath.tan(cameraModel.getHorizontalFOV() / 2) * size.height / size.width);
    }
}
