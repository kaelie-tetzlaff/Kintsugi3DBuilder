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

package kintsugi3d.gl.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class OptionalManagedResource<ResourceType extends ManagedResource> implements ManagedResource
{
    private final @Nullable ResourceType resource;

    private OptionalManagedResource(@Nullable ResourceType resource)
    {
        this.resource = resource;
    }

    private OptionalManagedResource()
    {
        resource = null;
    }

    public boolean isPresent()
    {
        return resource != null;
    }

    public @Nullable ResourceType get()
    {
        return resource;
    }

    public static <ResourceType extends ManagedResource> @NotNull OptionalManagedResource<ResourceType> empty()
    {
        return new OptionalManagedResource<>();
    }

    public static <ResourceType extends ManagedResource>
    @NotNull OptionalManagedResource<ResourceType> ofNullable(ResourceType resource)
    {
        return new OptionalManagedResource<>(resource);
    }

    public static <ResourceType extends ManagedResource>
    @NotNull OptionalManagedResource<ResourceType> of(ResourceType resource)
    {
        return new OptionalManagedResource<>(Objects.requireNonNull(resource, "Specified managed resource is null."));
    }

    @Override
    public void close()
    {
        if (resource != null)
        {
            resource.close();
        }
    }
}
