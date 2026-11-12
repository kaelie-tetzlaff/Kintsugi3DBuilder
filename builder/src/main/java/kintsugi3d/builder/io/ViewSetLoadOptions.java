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
    private Collection<File> disabledImages = new ArrayList<>(8);

    public ViewSetDirectories getMainDirectories()
    {
        return mainDirectories;
    }

    public void setMainDirectories(ViewSetDirectories mainDirectories)
    {
        this.mainDirectories = mainDirectories;
    }

    public File getGeometryFile()
    {
        return geometryFile;
    }

    public void setGeometryFile(File geometryFile)
    {
        this.geometryFile = geometryFile;
    }

    public File getMasksDirectory()
    {
        return masksDirectory;
    }

    public void setMasksDirectory(File masksDirectory)
    {
        this.masksDirectory = masksDirectory;
    }

    public String getOrientationViewName()
    {
        return orientationViewName;
    }

    public void setOrientationViewName(String orientationViewName)
    {
        this.orientationViewName = orientationViewName;
    }

    public double getOrientationViewRotation()
    {
        return orientationViewRotation;
    }

    public void setOrientationViewRotation(double orientationViewRotation)
    {
        this.orientationViewRotation = orientationViewRotation;
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    public Collection<File> getDisabledImages()
    {
        return Collections.unmodifiableCollection(disabledImages);
    }

    public void setDisabledImages(Collection<File> disabledImages)
    {
        this.disabledImages = new ArrayList<>(disabledImages);
    }
}