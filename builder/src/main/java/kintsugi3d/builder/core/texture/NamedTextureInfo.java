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

package kintsugi3d.builder.core.texture;

import kintsugi3d.builder.core.Global;
import kintsugi3d.builder.resources.project.ImageBasedGraphicsResources;
import kintsugi3d.builder.resources.project.texture.TextureResources;
import kintsugi3d.builder.state.shader.ShaderInfo;
import kintsugi3d.builder.state.shader.TextureVisualizationShaderInfo;
import kintsugi3d.gl.core.Context;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class NamedTextureInfo extends TextureInfo
{
    public NamedTextureInfo(String name, String friendlyName, String purpose)
    {
        super(name, friendlyName, purpose);
    }

    public NamedTextureInfo(String name)
    {
        super(name);
    }

    @Override
    public final @NotNull ShaderInfo getVisualizationShader()
    {
        return new TextureVisualizationShaderInfo(this, "rendermodes/viewTextureSimple.frag");
    }

    @Override
    public final void refresh(@NotNull ImageBasedGraphicsResources<? extends Context<?>> resources) throws IOException
    {
        // TODO switch to observable pattern for textures?
        resources.getTextureResources().replaceTextureWithDefaultFile(this, resources.getViewSet().getSupportingFilesDirectory());
    }

    @Override
    public final @NotNull ImageReplacer getReplaceData(@NotNull ImageBasedGraphicsResources<? extends Context<?>> resources)
    {
        return new NamedTextureReplacer(resources.getTextureResources(), this,
            new File(Global.io().getLoadedViewSetOrThrow().getSupportingFilesDirectory(),
                TextureResources.getTextureFilename(name)));
    }
}
