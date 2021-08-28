package com.yde.solvadoku.UI;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class customImageView extends AppCompatImageView {
    public customImageView(Context context) {
        super(context);
    }

    public customImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public customImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
