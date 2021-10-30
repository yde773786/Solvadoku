package com.yde.solvadoku.UI.grids;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

public class SquareGrid extends GridLayout {

    public TextView[][] grid;

    public SquareGrid(Context context) {
        super(context);
    }

    public SquareGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int minSpec = Math.min(widthSpec, heightSpec);
        super.onMeasure(minSpec, minSpec);
    }
}
