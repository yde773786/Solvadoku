package com.yde.solvadoku.UI.persistence;

import androidx.lifecycle.ViewModel;

import com.yde.solvadoku.Logic.Cell;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivityViewModel extends ViewModel {

    public final static String[] LOGICS = {"Naked Single", "Hidden Single", "Naked Pair", "Pointing Pair",
            "Claiming Pair", "Hidden Pair", "Naked Triple", "Hidden Triple", "X-Wing", "Swordfish", "Jellyfish",
            "Naked Quad", "Hidden Quad", "Finned X-Wing", "Finned Swordfish", "Finned Jellyfish", "Brute Force"};

    private Cell[][] grid = new Cell[9][9];
    private boolean pencilMarks;
    private ArrayList<String> checkedItems = new ArrayList<>(Arrays.asList(MainActivityViewModel.LOGICS));
    ;

    public Cell[][] getGrid() {
        return grid;
    }

    public void setGrid(Cell[][] grid) {
        this.grid = grid;
    }

    public boolean isPencilMarks() {
        return pencilMarks;
    }

    public void setPencilMarks(boolean pencilMarks) {
        this.pencilMarks = pencilMarks;
    }

    public ArrayList<String> getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(ArrayList<String> checkedItems) {
        this.checkedItems = checkedItems;
    }
}
