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

package kintsugi3d.builder.rendering;

import kintsugi3d.builder.state.ReadonlySelectableViewListModel;
import kintsugi3d.builder.state.scene.ReadonlyLightingEnvironmentModel;
import kintsugi3d.builder.state.scene.ReadonlyObjectPoseModel;
import kintsugi3d.builder.state.scene.ReadonlyViewpointModel;
import kintsugi3d.builder.state.settings.SafeReadonlyGlobalSettingsModel;
import kintsugi3d.gl.core.FramebufferSize;
import kintsugi3d.gl.vecmath.Matrix3;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;

public interface ReadonlySceneModel
{
    ReadonlyObjectPoseModel getObjectModel();

    ReadonlyViewpointModel getCameraModel();

    ReadonlyLightingEnvironmentModel getLightingModel();

    SafeReadonlyGlobalSettingsModel getSettingsModel();

    ReadonlySelectableViewListModel getCameraViewListModel();

    Vector3 getCentroid();

    Matrix3 getOrientation();

    float getScale();

    Vector3 getClearColor();

    Matrix4 getLightViewMatrix(int lightIndex);

    Matrix4 getInverseLightViewMatrix(int lightIndex);

    Matrix4 getLightModelViewMatrix(int lightIndex);

    Matrix4 getEnvironmentMapMatrix(Matrix4 modelMatrix);

    Matrix4 getCurrentViewMatrix();

    Matrix4 getViewMatrixFromCameraPose(Matrix4 cameraPoseMatrix);

    Matrix4 getCameraPoseFromViewMatrix(Matrix4 cameraPoseMatrix);

    Matrix4 getUnscaledMatrix(Matrix4 scaledMatrix);

    /**
     * Recenters and reorients the model using base model matrix, then applies the user-specified model transformation.
     *
     * @return
     */
    Matrix4 getFullModelMatrix();

    /**
     * Just recentering and reorienting the model
     *
     * @return
     */
    Matrix4 getBaseModelMatrix();

    Matrix4 getModelViewMatrix(Matrix4 view);

    Matrix4 getViewFromModelViewMatrix(Matrix4 modelViewMatrix);

    float getVerticalFieldOfView(FramebufferSize size);
}
