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

import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.ShaderBasedRenderable;
import kintsugi3d.gl.interactive.UserCancellationException;
import org.jetbrains.annotations.NotNull;

class BackendProgressMonitor implements ProgressMonitor
{
    private final ShaderBasedRenderable<?> instance;
    private final @NotNull ProgressMonitor base;

    BackendProgressMonitor(ShaderBasedRenderable<?> instance, @NotNull ProgressMonitor base)
    {
        this.instance = instance;
        this.base = base;
    }

    @Override
    public final void allowUserCancellation() throws UserCancellationException
    {
        base.allowUserCancellation();
    }

    @Override
    public final void cancelComplete(UserCancellationException e)
    {
        base.cancelComplete(e);
    }

    @Override
    public final void start()
    {
        base.start();
    }

    @Override
    public final void setProcessName(String processName)
    {
        base.setProcessName(processName);
    }

    @Override
    public final void setStageCount(int count)
    {
        // Add one for the preview image generation step already completed.
        base.setStageCount(count + 1);
    }

    @Override
    public final void setStage(int stage, String message)
    {
        // Add one for the preview image generation step already completed.
        base.setStage(stage + 1, message);
    }

    @Override
    public final void advanceStage(String message)
    {
        // Add one for the preview image generation step already completed.
        base.advanceStage(message);
    }

    @Override
    public final void setMaxProgress(double maxProgress)
    {
        base.setMaxProgress(maxProgress);
    }

    @Override
    public final void setProgress(double progress, String message)
    {
        base.setProgress(progress, message);
    }

    @Override
    public final void complete()
    {
        instance.reloadShaders();

        base.complete();
    }

    @Override
    public final void fail(Throwable e)
    {
        base.fail(e);
    }

    @Override
    public final void warn(Throwable e)
    {
        base.warn(e);
    }

    @Override
    public final void blocked()
    {
        base.blocked();
    }
}
