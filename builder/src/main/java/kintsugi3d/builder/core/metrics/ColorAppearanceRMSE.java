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

package kintsugi3d.builder.core.metrics;

/**
 * Bundles linear (physical radiance), sRGB / gamma-encoded, and encoded (tonemapped) RMSE
 */
public class ColorAppearanceRMSE implements ReadonlyColorAppearanceRMSE
{
    private double normalizedLinear = Double.POSITIVE_INFINITY;
    private double normalizedSRGB = Double.POSITIVE_INFINITY;
    private double encodedGroundTruth = Double.POSITIVE_INFINITY;
    private long sampleCount = 0;

    @Override
    public final double getNormalizedLinear()
    {
        return normalizedLinear;
    }

    public final void setNormalizedLinear(double normalizedLinear)
    {
        this.normalizedLinear = normalizedLinear;
    }

    @Override
    public final double getNormalizedSRGB()
    {
        return normalizedSRGB;
    }

    public final void setNormalizedSRGB(double normalizedSRGB)
    {
        this.normalizedSRGB = normalizedSRGB;
    }

    @Override
    public final double getEncodedGroundTruth()
    {
        return encodedGroundTruth;
    }

    public final void setEncodedGroundTruth(double encodedGroundTruth)
    {
        this.encodedGroundTruth = encodedGroundTruth;
    }

    @Override
    public final long getSampleCount()
    {
        return sampleCount;
    }

    public final void setSampleCount(long sampleCount)
    {
        this.sampleCount = sampleCount;
    }
}
