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

package kintsugi3d.renderexport.resample;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import kintsugi3d.builder.io.RecentProjects;
import kintsugi3d.builder.rendering.Rendering;
import kintsugi3d.builder.util.FXWindowHelper;

import java.io.File;

public class ResampleRequestController
{
    @FXML private Pane root;
    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;
    @FXML private TextField exportDirectoryField;
    @FXML private TextField targetVSetFileField;

    private final FileChooser fileChooser = new FileChooser();
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    private void exportDirectoryButtonAction()
    {
        directoryChooser.setTitle("Choose an export directory");
        if (exportDirectoryField.getText().isEmpty())
        {
            directoryChooser.setInitialDirectory(RecentProjects.getInstance().getMostRecentDirectory());
        }
        else
        {
            File currentValue = new File(exportDirectoryField.getText());
            directoryChooser.setInitialDirectory(currentValue);
        }
        File file = directoryChooser.showDialog(root.getScene().getWindow());
        if (file != null)
        {
            exportDirectoryField.setText(file.toString());
            RecentProjects.getInstance().setMostRecentDirectory(file);
        }
    }

    @FXML
    private void targetVSetFileButtonAction()
    {
        fileChooser.setTitle("Choose a target view set file");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("View set files", "*.vset"));
        if (targetVSetFileField.getText().isEmpty())
        {
            fileChooser.setInitialDirectory(RecentProjects.getInstance().getMostRecentDirectory());
        }
        else
        {
            File currentValue = new File(targetVSetFileField.getText());
            fileChooser.setInitialDirectory(currentValue.getParentFile());
            fileChooser.setInitialFileName(currentValue.getName());
        }
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null)
        {
            targetVSetFileField.setText(file.toString());
            RecentProjects.getInstance().setMostRecentDirectory(file.getParentFile());
        }
    }

    @FXML
    public final void cancel()
    {
        FXWindowHelper.requestClose(root);
    }

    @FXML
    public final void run()
    {
        Rendering.getRequestQueue().addImageBasedGraphicsRequest(
            new ResampleRequest(
                Integer.parseInt(widthTextField.getText()),
                Integer.parseInt(heightTextField.getText()),
                new File(targetVSetFileField.getText()),
                new File(exportDirectoryField.getText())));
    }
}
