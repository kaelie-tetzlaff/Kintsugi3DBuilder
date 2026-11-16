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

package kintsugi3d.fx.internal;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import kintsugi3d.builder.state.CarouselModel;
import kintsugi3d.builder.state.cards.CardsModel;
import kintsugi3d.builder.state.cards.ProjectDataCard;
import kintsugi3d.builder.state.cards.ProjectDataCardFactory;
import kintsugi3d.builder.state.cards.RefreshFunction;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @param <T> The backend type that the associated data cards represent.
 */
public class ObservableCardsModel<T> implements CardsModel<T>
{
    private final String label;
    private final String path;

    private final UUIDSelectionModel selectedCardsModel = new UUIDSelectionModel();
    private final UUIDSelectionModel expandedCardsModel = new UUIDSelectionModel();
    private final Map<UUID, ProjectDataCard> cardMap = FXCollections.observableHashMap();
    private final ObservableList<ProjectDataCard> cardsList = FXCollections.observableList(new ArrayList<>(8));

    // needs to be here to not get garbage-collected
    private final ObservableList<ProjectDataCard> unmodifiableCardsList =
        FXCollections.unmodifiableObservableList(cardsList);
    private final ObjectBinding<ProjectDataCard> lastSelectedCard = Bindings.createObjectBinding(
        () -> cardMap.get(selectedCardsModel.getLastSelected()), selectedCardsModel.lastSelectedProperty());

    private final ProjectDataCardFactory<T> cardFactory;
    private final ObservableCarouselModel carouselModel;

    public ObservableCardsModel(String label, String path, ProjectDataCardFactory<T> cardFactory,
                                ObservableCarouselModel carouselModel)
    {
        this.label = label;
        this.path = path;
        this.cardFactory = cardFactory;
        this.carouselModel = carouselModel;

        cardsList.addListener((ListChangeListener<? super ProjectDataCard>) change ->
        {
            while (change.next())
            {
                // Clear selection / expansion for a card when it is removed (but not replaced).
                if (change.wasRemoved())
                {
                    for (int i = 0; i < change.getRemovedSize(); i++)
                    {
                        ProjectDataCard removedItem = change.getRemoved().get(i);

                        if (change.wasAdded())
                        {
                            ProjectDataCard addedItem = change.getAddedSubList().get(i);

                            if (!addedItem.getCardId().equals(removedItem.getCardId()))
                            {
                                // Pass on selected / expanded state to the new card if it was replaced.
                                if (selectedCardsModel.isSelected(removedItem.getCardId()))
                                {
                                    selectedCardsModel.select(addedItem.getCardId());
                                }

                                if (expandedCardsModel.isSelected(removedItem.getCardId()))
                                {
                                    expandedCardsModel.select(addedItem.getCardId());
                                }

                                selectedCardsModel.unselect(removedItem.getCardId());
                                expandedCardsModel.unselect(removedItem.getCardId());
                            }
                        }
                        else
                        {
                            selectedCardsModel.unselect(removedItem.getCardId());
                            expandedCardsModel.unselect(removedItem.getCardId());
                        }
                    }
                }
            }
        });
    }

    public final void initialize()
    {
        List<ProjectDataCard> dataCards = cardFactory.createAllCards();
        this.setCardList(dataCards);
    }

    public final Class<T> getDataClass()
    {
        return cardFactory.getDataClass();
    }

    public final String getLabel()
    {
        return label;
    }

    public final String getPath()
    {
        return path;
    }

    public final boolean isSelected(UUID cardId)
    {
        return selectedCardsModel.isSelected(cardId);
    }

    public final boolean isExpanded(UUID cardId)
    {
        return expandedCardsModel.isSelected(cardId);
    }

    public final BooleanExpression createSelectedBinding(UUID cardId)
    {
        return selectedCardsModel.createSelectedBinding(cardId);
    }

    public final BooleanExpression createExpandedBinding(UUID cardId)
    {
        return expandedCardsModel.createSelectedBinding(cardId);
    }

    public final void expandCard(UUID id)
    {
        Platform.runLater(() -> expandedCardsModel.select(id));
    }

    public final void collapseCard(UUID cardId)
    {
        Platform.runLater(() -> expandedCardsModel.unselect(cardId));
    }

    public final void selectCard(UUID cardId)
    {
        Platform.runLater(() -> selectedCardsModel.select(cardId));
    }

    public final void deselectCard(UUID cardId)
    {
        Platform.runLater(() -> selectedCardsModel.unselect(cardId));
    }

    public final Collection<ProjectDataCard> getSelectedCards()
    {
        return selectedCardsModel.getSelected().keySet().stream().map(cardMap::get).collect(Collectors.toList());
    }

    public final ObjectExpression<ProjectDataCard> lastSelectedCardProperty()
    {
        return lastSelectedCard;
    }

    public final ProjectDataCard getLastSelectedCard()
    {
        return lastSelectedCard.get();
    }

    @Override
    public final List<? extends Map<String, Runnable>> getGlobalActions()
    {
        return cardFactory.getGlobalActions();
    }

    @Override
    public final ObservableList<ProjectDataCard> getCardList()
    {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return unmodifiableCardsList;
    }

    @Override
    public final void setCardList(List<ProjectDataCard> cards)
    {
        cardsList.clear();
        cardsList.addAll(cards);

        cardMap.clear();
        for (ProjectDataCard card : cards)
        {
            cardMap.put(card.getCardId(), card);
        }

        selectedCardsModel.clearSelection();
        expandedCardsModel.clearSelection();
    }

    public final int getEnabledCardCount()
    {
        return (int) cardsList.stream().filter(ProjectDataCard::isEnabled).count();
    }

    public final int getDisabledCardCount()
    {
        return (int) cardsList.stream().filter(card -> !card.isEnabled()).count();
    }

    @Override
    public final void refreshCards(RefreshFunction<T> refreshedData)
    {
        cardFactory.refreshCards(cardsList, refreshedData);
    }

    @Override
    public final void deleteCards(Predicate<ProjectDataCard> filter)
    {
        cardsList.removeIf(filter);

        var mapEntries = cardMap.entrySet().stream()
            .filter(entry -> filter.test(entry.getValue()))
            .collect(Collectors.toList());

        for (var entry : mapEntries)
        {
            cardMap.remove(entry.getKey(), entry.getValue());
        }
    }

    public final CarouselModel getCarousel()
    {
        return carouselModel;
    }
}
