package com.yde.solvadoku.UI.persistence;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivityViewModel extends ViewModel {

    public final static String[] LOGICS = {"Naked Single", "Hidden Single", "Naked Pair", "Pointing Pair",
            "Claiming Pair", "Hidden Pair", "Naked Triple", "Hidden Triple", "X-Wing", "Swordfish", "Jellyfish",
            "Naked Quad", "Hidden Quad", "Finned X-Wing", "Finned Swordfish", "Finned Jellyfish", "Brute Force"};

    private String[][] values = new String[9][9];

    private boolean pencilMarks;
    private ArrayList<String> checkedItems = new ArrayList<>(Arrays.asList(MainActivityViewModel.LOGICS));
    private boolean isSolving;

    public String[][] getValues() {
        return values;
    }

    public void setValues(String[][] values) {
        this.values = values;
    }

    public boolean isSolving() {
        return isSolving;
    }

    public void setSolving(boolean solving) {
        isSolving = solving;
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
