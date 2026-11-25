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

package kintsugi3d.builder.state.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class ProjectDataCard
{
    private final String internalName;
    private final @Nullable String fullResImageFilePath;
    private final @NotNull UUID cardId;
    private final String title;
    private final String thumbnailPath;
    private final @NotNull Map<String, String> textFields;
    private final @NotNull Collection<? extends Map<String, Runnable>> actionGroups;
    private boolean isEnabled;

    public ProjectDataCard(
        String internalName, String title, @Nullable String fullResImageFilePath, String thumbnailPath,
        @NotNull Map<String, String> textFields, @NotNull Collection<? extends Map<String, Runnable>> actionGroups, boolean isEnabled)
    {
        this.internalName = internalName;
        cardId = UUID.randomUUID();
        this.title = title;
        this.fullResImageFilePath = fullResImageFilePath;
        this.thumbnailPath = thumbnailPath;
        this.textFields = Collections.unmodifiableMap(textFields);
        this.actionGroups = Collections.unmodifiableCollection(actionGroups);
        this.isEnabled = isEnabled;
    }

    public final String getInternalName()
    {
        return internalName;
    }

    public final @NotNull UUID getCardId()
    {
        return cardId;
    }

    public final String getTitle()
    {
        if (!isEnabled)
        {
            return String.format("%s - DISABLED", title);
        }
        return title;
    }

    public final String getThumbnailPath()
    {
        return thumbnailPath;
    }

    public final String getValue(String key)
    {
        if (!textFields.containsKey(key))
        {
            throw new IllegalArgumentException("Key does not exist.");
        }
        return textFields.get(key);
    }

    public final @NotNull Map<String, String> getTextContent()
    {
        return textFields;
    }

    public final @NotNull Collection<? extends Map<String, Runnable>> getActions()
    {
        return actionGroups;
    }

    public final boolean isEnabled() { return isEnabled; }

    public final void setIsEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }

    public final @Nullable String getFullResImageFilePath()
    {
        return fullResImageFilePath;
    }
}
