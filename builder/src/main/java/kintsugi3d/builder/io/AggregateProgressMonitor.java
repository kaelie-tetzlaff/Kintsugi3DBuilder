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

import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.UserCancellationException;

import java.util.ArrayList;
import java.util.Collection;

class AggregateProgressMonitor implements ProgressMonitor
{
    private final Collection<ProgressMonitor> subMonitors = new ArrayList<>(8);

    final void addSubMonitor(ProgressMonitor monitor)
    {
        subMonitors.add(monitor);
    }

    @Override
    public final void allowUserCancellation() throws UserCancellationException
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.allowUserCancellation();
        }
    }

    @Override
    public final void cancelComplete(UserCancellationException e)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.cancelComplete(e);
        }
    }

    @Override
    public final void start()
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.start();
        }
    }

    @Override
    public final void setProcessName(String processName)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.setProcessName(processName);
        }
    }

    @Override
    public final void setStageCount(int count)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.setStageCount(count);
        }
    }

    @Override
    public final void setStage(int stage, String message)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.setStage(stage, message);
        }
    }

    @Override
    public final void advanceStage(String message)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.advanceStage(message);
        }
    }

    @Override
    public final void setMaxProgress(double maxProgress)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.setMaxProgress(maxProgress);
        }
    }

    @Override
    public final void setProgress(double progress, String message)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.setProgress(progress, message);
        }
    }

    @Override
    public final void complete()
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.complete();
        }
    }

    @Override
    public final void fail(Throwable e)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.fail(e);
        }
    }

    @Override
    public final void warn(Throwable e)
    {
        for (ProgressMonitor monitor : subMonitors)
        {
            monitor.warn(e);
        }
    }

    @Override
    public final boolean isConflictingProcess()
    {
        boolean processing = false;
        for (ProgressMonitor monitor : subMonitors)
        {
            if (monitor.isConflictingProcess())
            {
                processing = true;
            }
        }

        return processing;
    }
}
