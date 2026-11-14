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

package kintsugi3d.fx.controllers.sidebar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Iterator;

public final class SearchableTreeView extends SearchableView
{
    private final TreeView<String> treeView;
    private TreeItem<String> backupRoot;

    private SearchableTreeView(TreeView<String> treeView, TextInputControl searchTextInput, CheckBox regexMode)
    {
        super(searchTextInput, regexMode);
        this.treeView = treeView;
    }

    @Override
    public SearchableTreeView bind()
    {
        backupRoot = deepCopy(treeView.getRoot());

        getSearchTextInput().textProperty().addListener((obs, oldText, newText) -> updateView());

        if (getRegexMode() != null)
        {
            getRegexMode().selectedProperty().addListener((obs, oldVal, newVal) -> updateView());
        }
        return this;
    }

    public static SearchableTreeView createUnboundInstance(TreeView<String> tree, TextInputControl textInput, CheckBox regexMode)
    {
        return new SearchableTreeView(tree, textInput, regexMode);
    }

    public static SearchableTreeView createUnboundInstance(TreeView<String> tree, TextInputControl textInput)
    {
        return new SearchableTreeView(tree, textInput, null);
    }

    @Override
    protected void updateView()
    {
        treeView.setRoot(deepCopy(backupRoot));

        String searchTxt = getSearchTextInput().getText().trim();
        if (searchTxt.isBlank())
        {
            treeView.getRoot().setExpanded(true);
            return;
        }

        ObservableList<TreeItem<String>> leaves = getTreeViewLeaves(backupRoot);
        FilteredList<TreeItem<String>> filteredLeaves = new FilteredList<>(leaves, visibility -> true);

        if (getRegexMode() != null && getRegexMode().isSelected())
        {
            filteredLeaves.setPredicate(leaf -> leaf.getValue().matches(String.format(".*%s.*", searchTxt)));
        }
        else
        {
            filteredLeaves.setPredicate(leaf -> leaf.getValue().contains(searchTxt));
        }

        for (TreeItem<String> leaf : leaves)
        {
            if (!filteredLeaves.contains(leaf))
            {
                removeNonRootItem(treeView, leaf.getValue());
            }
        }

        for (TreeItem<String> group : getTreeViewGroups(treeView.getRoot()))
        {
            group.setExpanded(true);
        }
    }

    private static void removeNonRootItem(TreeView<String> treeView, String toRemove)
    {
        removeNonRootItemRecursive(treeView.getRoot().getChildren().iterator(), toRemove);
    }

    private static boolean removeNonRootItemRecursive(Iterator<TreeItem<String>> current, String toRemove)
    {
        while (current.hasNext()) // Iterate to search sibling nodes if needed
        {
            TreeItem<String> currentItem = current.next();

            if (currentItem.getValue().equals(toRemove))
            {
                current.remove();
                return true;
            }
            // Search child nodes with recursion if needed
            else if (removeNonRootItemRecursive(currentItem.getChildren().iterator(), toRemove))
            {
                return true;
            }
        }

        return false;
    }

    private static TreeItem<String> deepCopy(TreeItem<String> item)
    {
        TreeItem<String> copy = new TreeItem<>(item.getValue(), item.getGraphic());
        for (TreeItem<String> child : item.getChildren())
        {
            copy.getChildren().add(deepCopy(child));
        }
        return copy;
    }

    private static ObservableList<TreeItem<String>> getTreeViewGroups(TreeItem<String> backupRoot)
    {
        ObservableList<TreeItem<String>> list = FXCollections.observableArrayList();
        getTreeViewGroupsRec(backupRoot, list);
        return list;
    }

    private static void getTreeViewGroupsRec(TreeItem<String> item, ObservableList<TreeItem<String>> list)
    {
        if (!item.getChildren().isEmpty())
        {
            list.add(item);

            for (TreeItem<String> child : item.getChildren())
            {
                getTreeViewGroupsRec(child, list);
            }
        }
    }

    private static ObservableList<TreeItem<String>> getTreeViewLeaves(TreeItem<String> backupRoot)
    {
        ObservableList<TreeItem<String>> list = FXCollections.observableArrayList();
        getTreeViewLeavesRec(backupRoot, list);
        return list;
    }

    private static void getTreeViewLeavesRec(TreeItem<String> item, ObservableList<TreeItem<String>> list)
    {
        if (item.isLeaf())
        {
            list.add(item);
        }
        else
        {
            for (TreeItem<String> child : item.getChildren())
            {
                getTreeViewLeavesRec(child, list);
            }
        }
    }

    public TreeView<String> getTreeView()
    {
        return this.treeView;
    }
}
