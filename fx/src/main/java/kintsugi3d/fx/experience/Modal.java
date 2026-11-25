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

package kintsugi3d.fx.experience;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import kintsugi3d.builder.util.AppIcon;
import kintsugi3d.builder.util.FXWindowHelper;
import kintsugi3d.fx.core.Accelerators;
import kintsugi3d.fx.core.MainWindowController;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Creates a modal window.
 * Only one modal window managed by a single instance of this class can be open at a time.
 */
public class Modal
{
    private final Window parentWindow;
    private Stage stage;

    private final BooleanProperty openProperty = new SimpleBooleanProperty(false);

    public Modal(Window parentWindow)
    {
        this.parentWindow = parentWindow;
    }

    public final @NotNull BooleanExpression getOpenProperty()
    {
        return openProperty;
    }

    public final boolean isOpen()
    {
        return openProperty.get();
    }

    public final Stage getStage()
    {
        return stage;
    }

    /**
     * Opens a modal window and returns the associated controller.
     * Returns null if window is already open or if an error occurred.
     * @param title
     * @param urlString
     * @return
     * @param <ControllerType>
     * @throws IOException
     */
    public final <ControllerType> ControllerType create(String title, @NotNull String urlString) throws IOException
    {
        return create(title, getFXMLLoader(urlString));
    }

    public final <ControllerType> ControllerType create(String title, @NotNull FXMLLoader fxmlLoader) throws IOException
    {
        Parent root = fxmlLoader.load();

        stage = new Stage();
        stage.getIcons().add(AppIcon.getImage());
        stage.setTitle(title);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(Modal.class.getResource("/fxml/kintsugi.css")).toExternalForm());
        stage.setScene(scene);
        stage.initOwner(parentWindow);
        stage.setResizable(false);
        openProperty.bind(stage.showingProperty());
        Accelerators.initForScene(stage.getScene());

        return fxmlLoader.getController();
    }

    public final void open()
    {
        stage.show();
    }

    public final void requestClose()
    {
        FXWindowHelper.requestClose(stage);
    }

    private static @NotNull FXMLLoader getFXMLLoader(@NotNull String urlString) throws FileNotFoundException
    {
        URL url = MainWindowController.class.getResource(urlString);
        if (url == null)
        {
            throw new FileNotFoundException(urlString);
        }
        return new FXMLLoader(url);
    }
}
