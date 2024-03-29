package com.yde.solvadoku.UI.grids;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import com.yde.solvadoku.Logic.Cell;
import com.yde.solvadoku.R;

public class PencilMarksGrid extends SquareGrid {

    public PencilMarksGrid(Context context) {
        super(context);
        grid = new TextView[3][3];
        paintPencilMarks();
    }

    public PencilMarksGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        grid = new TextView[3][3];
        paintPencilMarks();
    }

    public PencilMarksGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        grid = new TextView[3][3];
        paintPencilMarks();
    }

    /**
     * Paints the pencil marks grid empty
     */
    public void paintPencilMarks() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                TextView textView = new TextView(getContext());
                textView.setWidth(0);
                textView.setHeight(0);
                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams(
                        GridLayout.spec(i, 0.33f), GridLayout.spec(j, 0.33f)
                );
                textView.setLayoutParams(gridParams);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.pencil_marks_text_size));
                textView.setGravity(Gravity.CENTER);
                grid[i][j] = textView;
                addView(textView, i + j);
            }
        }
    }

    /**
     * Remove Pencil Marks when not needed
     */
    public void clearPencilMarks() {
        for (TextView[] textViews : grid) {
            for (TextView textView : textViews) {
                textView.setText(R.string.empty);
            }
        }
    }

    /**
     * Adds pencil marks as per candidates of cell required
     *
     * @param cell Cell input provided
     */
    public void insertPencilMarks(Cell cell) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cell.isCandidate(i * 3 + j + 1)) {
                    grid[i][j].setText(String.valueOf(i * 3 + j + 1));
                } else {
                    grid[i][j].setText(R.string.empty);
                }
            }
        }
    }
}
