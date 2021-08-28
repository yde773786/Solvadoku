package com.yde.solvadoku.UI.Grids;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;


import androidx.gridlayout.widget.GridLayout;

import com.yde.solvadoku.R;

import java.util.HashMap;
import java.util.Objects;

public class SudokuGrid extends SquareGrid {

    private HashMap<EditText, int[]> cellToState = new HashMap<>();
    private static final int CLEAR = 0, DISABLED = 1, SELECTED = 2, INVALID = 3;

    public SudokuGrid(Context context) {
        super(context);
        paintSudoku();
    }

    public void paintSudoku() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                EditText editText = new EditText(getContext());
                editText.setWidth(0);
                editText.setHeight(0);

                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams(
                        GridLayout.spec(i, 0.11f), GridLayout.spec(j, 0.11f)
                );
                editText.setLayoutParams(gridParams);
                editText.setTextSize(25);

                addView(editText, i + j);
                addCellToState(editText, i, j);

                editText.setBackgroundResource(Objects.requireNonNull(cellToState.get(editText))[CLEAR]);
            }
        }
    }

    public SudokuGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintSudoku();
    }

    public SudokuGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paintSudoku();
    }

    private void addCellToState(EditText editText, int r, int c) {
        if (c == 2 || c == 5) {
            if (r == 2 || r == 5) {
                cellToState.put(editText, new int[]{R.drawable.bottom_right, R.drawable.bottom_rightgrey,
                        R.drawable.bottom_rightblue, R.drawable.bottom_rightred});
            } else if (r == 3 || r == 6) {
                cellToState.put(editText, new int[]{R.drawable.top_right, R.drawable.top_rightgrey,
                        R.drawable.top_rightblue, R.drawable.top_rightred});
            } else
                cellToState.put(editText, new int[]{R.drawable.right_border, R.drawable.right_bordergrey,
                        R.drawable.right_borderblue, R.drawable.right_borderred});
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                cellToState.put(editText, new int[]{R.drawable.bottom_left, R.drawable.bottom_leftgrey,
                        R.drawable.bottom_leftblue, R.drawable.bottom_leftred});
            } else if (r == 3 || r == 6) {
                cellToState.put(editText, new int[]{R.drawable.top_left, R.drawable.top_leftgrey,
                        R.drawable.top_leftblue, R.drawable.top_leftred});
            } else
                cellToState.put(editText, new int[]{R.drawable.left_border, R.drawable.left_bordergrey,
                        R.drawable.left_borderblue, R.drawable.left_borderred});
        } else if (r == 2 || r == 5) {
            cellToState.put(editText, new int[]{R.drawable.bottom_border, R.drawable.bottom_bordergrey,
                    R.drawable.bottom_borderblue, R.drawable.bottom_borderred});
        } else if (r == 3 || r == 6) {
            cellToState.put(editText, new int[]{R.drawable.top_border, R.drawable.top_bordergrey,
                    R.drawable.top_borderblue, R.drawable.top_borderred});
        } else
            cellToState.put(editText, new int[]{R.drawable.border, R.drawable.bordergrey,
                    R.drawable.borderblue, R.drawable.borderred});
    }

}
