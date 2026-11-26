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

package kintsugi3d.builder.io.metashape;

import kintsugi3d.builder.io.XMLHelper;
import kintsugi3d.builder.util.UnzipHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MetashapeDocument
{
    private static final Logger LOG = LoggerFactory.getLogger(MetashapeDocument.class);

    private final String psxFilePath;

    private List<MetashapeChunk> chunks;

    private int activeChunkID;

    public MetashapeDocument(String path)
    {
        psxFilePath = path;

        if (!isValidPSXFilePath(psxFilePath))
        {
            return;
        }

        try
        {
            //Get Document from .psx file
            Document psxDocument = XMLHelper.getDocumentBuilder().parse(new File(psxFilePath));

            //get the path attribute from the document tag
            NodeList nodes = psxDocument.getElementsByTagName("document");
            Element documentTag = (Element) nodes.item(0);

            //this gives "{projectname}.files/project.zip"
            //need to replace {projectname} with full path (except .psx)
            String documentPathInfo = documentTag.getAttribute("path");

            documentPathInfo = documentPathInfo.substring(13);
            documentPathInfo = String.format("%s%s", getPSXPathBase(), documentPathInfo);

            //extract project.zip and open the doc.xml
            Document projectZipXML = UnzipHelper.unzipToDocument(new File(documentPathInfo));

            //set active chunk id if the project has one
            NodeList chunksWrapper = projectZipXML.getElementsByTagName("chunks");
            if (chunksWrapper.getLength() > 0)
            {
                Element chunkWrapperElem = (Element) chunksWrapper.item(0);
                String activeID = chunkWrapperElem.getAttribute("active_id");
                if (!activeID.isBlank())
                {
                    activeChunkID = Integer.parseInt(activeID);
                }
            }

            //find the chunks and open the .zip for each chunk
            NodeList chunkList = projectZipXML.getElementsByTagName("chunk");

            loadChunks(chunkList);
        }
        catch (ParserConfigurationException | IOException | SAXException e)
        {
            LOG.error("An error occurred:", e);
        }
    }

    public final @Nullable String getChunkNameFromID(int id)
    {
        Optional<@NotNull MetashapeChunk> chunk = chunks.stream()
            .filter(c -> c.getID() == id)
            .findFirst();

        if (chunk.isPresent())
        {
            return chunk.get().getLabel();
        }
        return "";
    }

    final @NotNull String getPSXPathBase()
    {
        return psxFilePath.substring(0, psxFilePath.length() - 4);
    }

    private void loadChunks(@NotNull NodeList chunkList) throws IOException
    {
        chunks = new ArrayList<>(chunkList.getLength());
        for (int i = 0; i < chunkList.getLength(); ++i)
        {
            Node chunkNode = chunkList.item(i);

            if (chunkNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element chunkElement = (Element) chunkNode;

                MetashapeChunk chunk = MetashapeChunk.parseFromElement(this, chunkElement);
                chunks.add(chunk);

                if (chunk.getID().equals(activeChunkID))
                {
                    selectChunk(chunk.getLabel());
                }
            }
        }
    }

    public final int getActiveChunkID()
    {
        return activeChunkID;
    }

    private static boolean isValidPSXFilePath(@NotNull String path)
    {
        File file = new File(path);
        return file.exists() && file.getAbsolutePath().endsWith(".psx");
    }

    public final @NotNull File getPsxFile()
    {
        return new File(psxFilePath);
    }

    public final String getPsxFilePath()
    {
        return psxFilePath;
    }

    public final @NotNull List<MetashapeChunk> getChunks()
    {
        return Collections.unmodifiableList(chunks);
    }

    public final void selectChunk(@Nullable String selectedChunkLabel)
    {
        chunks.stream()
            .filter(chunk -> Objects.equals(chunk.getLabel(), selectedChunkLabel))
            .findFirst()
            .ifPresent(metashapeChunk -> activeChunkID = metashapeChunk.getID());
    }

    public final @Nullable MetashapeChunk getSelectedChunk()
    {
        return chunks.stream()
            .filter(chunk -> chunk.getID() == activeChunkID)
            .findFirst()
            .orElse(null);
    }
}
