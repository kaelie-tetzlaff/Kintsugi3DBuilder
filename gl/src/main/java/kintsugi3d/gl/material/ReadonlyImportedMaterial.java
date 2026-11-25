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

package kintsugi3d.gl.material;

import kintsugi3d.gl.vecmath.Vector3;
import org.jetbrains.annotations.Nullable;

public interface ReadonlyImportedMaterial
{
    String getName();

    Vector3 getAmbient();

    Vector3 getDiffuse();

    Vector3 getSpecular();

    float getExponent();

    float getRoughness();

    Vector3 getEmission();

    float getMetallic();

    Vector3 getSheen();

    Vector3 getClearcoat();

    float getClearcoatRoughness();

    float getOpacity();

    float getTransparency();

    float getTranslucency();

    @Nullable ReadonlyMaterialColorMap getAmbientMap();

    @Nullable ReadonlyMaterialColorMap getDiffuseMap();

    @Nullable ReadonlyMaterialColorMap getSpecularMap();

    @Nullable ReadonlyMaterialScalarMap getExponentMap();

    @Nullable ReadonlyMaterialScalarMap getRoughnessMap();

    @Nullable ReadonlyMaterialScalarMap getOpacityMap();

    @Nullable ReadonlyMaterialScalarMap getTransparencyMap();

    @Nullable ReadonlyMaterialScalarMap getTranslucencyMap();

    @Nullable ReadonlyMaterialColorMap getEmissionMap();

    @Nullable ReadonlyMaterialScalarMap getMetallicMap();

    @Nullable ReadonlyMaterialColorMap getSheenMap();

    @Nullable ReadonlyMaterialScalarMap getAnisotropyMap();

    @Nullable ReadonlyMaterialScalarMap getAnisotropyRotationMap();

    @Nullable ReadonlyMaterialBumpMap getBumpMap();

    @Nullable ReadonlyMaterialTextureMap getNormalMap();

    @Nullable ReadonlyMaterialScalarMap getDisplacementMap();

    @Nullable ReadonlyMaterialScalarMap getAmbientOcclusionMap();
}
