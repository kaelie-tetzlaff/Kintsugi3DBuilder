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

package kintsugi3d.fx.controllers.paged;

import javafx.fxml.FXMLLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SelectionPageBuilder<T, FinishType> extends PageBuilder<FinishType>
{
    private final SelectionPage<T> page;

    SelectionPageBuilder(SelectionPage<T> page, PageFrameController frameController, Supplier<FinishType> finisher)
    {
        super(frameController, finisher);
        this.page = page;
    }

    @Override
    public final SelectionPage<T> getPage()
    {
        return page;
    }

    final <PageType extends Page<? super T, NextOutType>, NextOutType>
    @NotNull SelectionPageBuilder<? super T, FinishType> choiceJoin(String choiceLabel, PageType joinPage)
    {
        page.addChoice(choiceLabel, joinPage);
        return this;
    }

    public final <PageType extends Page<? super T, NextOutType>, NextOutType, ControllerType extends PageController<?>>
    @NotNull SimplePageBuilder<T, NextOutType, SelectionPageBuilder<T, FinishType>> choice(
        String choiceLabel, @NotNull String fxmlPath, @NotNull Function<FXMLLoader, PageType> pageConstructor,
        @Nullable Supplier<ControllerType> controllerConstructorOverride)
    {
        PageType nextPage = getFrameController().createPage(fxmlPath, pageConstructor, controllerConstructorOverride);
        page.addChoice(choiceLabel, nextPage);
        return new DataPageBuilder<>(nextPage, getFrameController(), () -> this);
    }

    public final <PageType extends Page<? super T, NextOutType>, NextOutType>
    @NotNull SimplePageBuilder<T, NextOutType, SelectionPageBuilder<T, FinishType>> choice(
        String choiceLabel, @NotNull String fxmlPath, @NotNull Function<FXMLLoader, PageType> pageConstructor)
    {
        return choice(choiceLabel, fxmlPath, pageConstructor, null);
    }

    public final <ControllerType extends NonSupplierPageController<T>>
    @NotNull SimplePageBuilder<T, T, SelectionPageBuilder<T, FinishType>> choice(
        String choiceLabel, @NotNull String fxmlPath, @Nullable Supplier<ControllerType> controllerConstructorOverride)
    {
        return choice(choiceLabel, fxmlPath, SimpleDataReceiverPage<T, ControllerType>::new, controllerConstructorOverride);
    }

    public final <ControllerType extends NonSupplierPageController<T>>
    @NotNull SimplePageBuilder<T, T, SelectionPageBuilder<T, FinishType>> choice(String choiceLabel, @NotNull String fxmlPath)
    {
        return this.<ControllerType>choice(choiceLabel, fxmlPath, null);
    }

    public final <ControllerType extends NonSupplierPageController<Object>>
    @NotNull NonDataPageBuilder<SelectionPageBuilder<T, FinishType>> choiceNonData(
        String choiceLabel, @NotNull String fxmlPath, @Nullable Supplier<ControllerType> controllerConstructorOverride)
    {
        SimpleNonDataPage<ControllerType> nextPage =
            getFrameController().createPage(fxmlPath, SimpleNonDataPage<ControllerType>::new, controllerConstructorOverride);
        page.addChoice(choiceLabel, nextPage);
        return new NonDataPageBuilder<>(nextPage, getFrameController(), () -> this);
    }

    public final <ControllerType extends NonSupplierPageController<Object>>
    @NotNull NonDataPageBuilder<SelectionPageBuilder<T, FinishType>> choiceNonData(String choiceLabel, @NotNull String fxmlPath)
    {
        return this.<ControllerType>choiceNonData(choiceLabel, fxmlPath, null);
    }

    public final <PageType extends SelectionPage<T>, ControllerType extends PageController<T>>
    @NotNull SelectionPageBuilder<T, FinishType> choiceSubSelect(
        String choiceLabel, String subprompt, @NotNull Function<FXMLLoader, PageType> pageConstructor, @Nullable Supplier<ControllerType> controllerConstructorOverride)
    {
        PageType nextPage = getFrameController().createPage(SELECTION_PAGE_FXML, pageConstructor, controllerConstructorOverride);
        nextPage.setPrompt(subprompt);
        page.addChoice(choiceLabel, nextPage);
        return new DataSelectionPageBuilder<>(nextPage, getFrameController(), getFinisher());
    }

    public final <PageType extends SelectionPage<T>, ControllerType extends PageController<T>>
    @NotNull SelectionPageBuilder<T, FinishType> choiceSubSelect(String choiceLabel, String subprompt, @NotNull Function<FXMLLoader, PageType> pageConstructor)
    {
        return this.<PageType, ControllerType>choiceSubSelect(choiceLabel, subprompt, pageConstructor, null);
    }

    public abstract <ControllerType extends SelectionPageController<T>>
    SelectionPageBuilder<T, FinishType> choiceSubSelect(String choiceLabel, String subprompt, Supplier<ControllerType> controllerConstructorOverride);

    public abstract SelectionPageBuilder<T, FinishType> choiceSubSelect(String choiceLabel, String subprompt);
}
