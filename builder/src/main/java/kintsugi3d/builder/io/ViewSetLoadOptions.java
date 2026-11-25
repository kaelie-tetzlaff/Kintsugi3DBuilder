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

package kintsugi3d.builder.io;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class ViewSetLoadOptions
{
    private ViewSetDirectories mainDirectories = new ViewSetDirectories();
    private File geometryFile;
    private File masksDirectory;
    private String orientationViewName;
    private double orientationViewRotation;
    private UUID uuid;
    private @NotNull Collection<File> disabledImages = new ArrayList<>(8);

    public final ViewSetDirectories getMainDirectories()
    {
        return mainDirectories;
    }

    public final void setMainDirectories(ViewSetDirectories mainDirectories)
    {
        this.mainDirectories = mainDirectories;
    }

    public final File getGeometryFile()
    {
        return geometryFile;
    }

    public final void setGeometryFile(File geometryFile)
    {
        this.geometryFile = geometryFile;
    }

    public final File getMasksDirectory()
    {
        return masksDirectory;
    }

    public final void setMasksDirectory(File masksDirectory)
    {
        this.masksDirectory = masksDirectory;
    }

    public final String getOrientationViewName()
    {
        return orientationViewName;
    }

    public final void setOrientationViewName(String orientationViewName)
    {
        this.orientationViewName = orientationViewName;
    }

    public final double getOrientationViewRotation()
    {
        return orientationViewRotation;
    }

    public final void setOrientationViewRotation(double orientationViewRotation)
    {
        this.orientationViewRotation = orientationViewRotation;
    }

    public final UUID getUUID()
    {
        return uuid;
    }

    public final void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    public final @NotNull Collection<File> getDisabledImages()
    {
        return Collections.unmodifiableCollection(disabledImages);
    }

    public final void setDisabledImages(@NotNull Collection<File> disabledImages)
    {
        this.disabledImages = new ArrayList<>(disabledImages);
    }
}