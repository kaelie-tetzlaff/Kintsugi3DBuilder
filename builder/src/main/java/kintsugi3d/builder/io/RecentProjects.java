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

import kintsugi3d.builder.util.ApplicationFolders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public final class RecentProjects
{
    private static final Logger LOG = LoggerFactory.getLogger(RecentProjects.class);

    public static final int MAX_RECENT_PROJECTS = 20;
    public static final String RECENT_PROJECTS_FILENAME = "recentFiles.txt";

    private static final RecentProjects INSTANCE = new RecentProjects();

    private final File recentProjectsFile;
    private final Collection<Runnable> recentFilesChangedListeners = new ArrayList<>(1);

    private File recentDirectory;

    private RecentProjects()
    {
        this.recentProjectsFile = new File(ApplicationFolders.getUserAppDirectory().toFile(), RECENT_PROJECTS_FILENAME);
    }

    public static RecentProjects getInstance()
    {
        return INSTANCE;
    }

    public List<String> getRecentProjectFilenames()
    {
        List<String> projectItems = List.of();

        if (recentProjectsFile.exists())
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(recentProjectsFile.getAbsolutePath(), StandardCharsets.UTF_8)))
            {
                projectItems = reader.lines().limit(MAX_RECENT_PROJECTS).collect(Collectors.toUnmodifiableList());
            }
            catch (IOException e)
            {
                LOG.error("Could not get items from recent files list", e);
            }
        }

        //remove duplicates while maintaining the same order (regular HashSet does not maintain order)
        return new ArrayList<>(new LinkedHashSet<>(projectItems));
    }

    public static String shortenedPath(String path)
    {
        File file = new File(path);
        File ancestorFile = getAncestorFile(file);

        return String.format("%s...%s%s", ancestorFile.getAbsolutePath(), File.separator, file.getName());
    }

    private static File getAncestorFile(File file)
    {
        File ancestorFile = file;
        while (ancestorFile.getParentFile() != null)
        {
            ancestorFile = ancestorFile.getParentFile();
        }
        return ancestorFile;
    }

    public void addToRecentFiles(String fileName)
    {
        List<String> existingFileNames = getRecentProjectFilenames();

        // Check if the fileName is already present
        existingFileNames.remove(fileName); // Remove it from its current position

        // Add the fileName to the front of the List
        existingFileNames.add(0, fileName);

        // Drop down to the max number of recent projects.
        existingFileNames = existingFileNames.subList(0, Math.min(existingFileNames.size(), MAX_RECENT_PROJECTS));

        refreshRecentProjectsFile(existingFileNames);
    }

    public void refreshRecentProjectsFile(Iterable<String> existingFileNames)
    {
        // Make sure that parent directory exists.
        boolean failed = !recentProjectsFile.getParentFile().mkdirs();
        if (failed && !recentProjectsFile.getParentFile().exists())
        {
            LOG.warn("Failed to create app data directory for recent files list.");
        }

        // Write the updated content back to the file
        try (PrintWriter writer = new PrintWriter(new FileWriter(recentProjectsFile, StandardCharsets.UTF_8)))
        {
            for (String name : existingFileNames)
            {
                writer.println(name);
            }
        }
        catch (IOException e)
        {
            LOG.error("Failed to update recent files list.", e);
        }

        // Update list of recent projects in program
        fireListeners();
    }

    public void removeInvalidReferences()
    {
        List<String> newRecentItems = getRecentProjectFilenames().stream()
            .map(File::new)
            .filter(File::exists)
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());

        // Write the updated content back to the file
        refreshRecentProjectsFile(newRecentItems);
    }

    public void removeAllReferences()
    {
        // Wipe recent projects list
        refreshRecentProjectsFile(List.of());
    }

    public String getMostRecentProjectPath()
    {
        return getRecentProjectFilenames().get(0);
    }

    //use these functions to make file selection more user-friendly across multiple File/Directory Choosers
    public void setMostRecentDirectory(File file)
    {
        recentDirectory = file;
    }

    public File getMostRecentDirectory()
    {
        if (recentDirectory != null && recentDirectory.exists())
        {
            return recentDirectory;
        }

        //loop through recent files and assign/return the first existing one
        for (String path : getRecentProjectFilenames())
        {
            File file = new File(path);
            if (file.exists())
            {
                recentDirectory = file.getParentFile();
                return recentDirectory;
            }
        }

        return new File(System.getProperty("user.home"));
    }

    public void addRecentProjectsChangedListener(Runnable listener)
    {
        recentFilesChangedListeners.add(listener);
    }

    private void fireListeners()
    {
        for (Runnable listener : recentFilesChangedListeners)
        {
            listener.run();
        }
    }
}
