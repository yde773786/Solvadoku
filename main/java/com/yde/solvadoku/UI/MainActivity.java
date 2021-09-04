package com.yde.solvadoku.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yde.solvadoku.Logic.Cell;
import com.yde.solvadoku.Logic.Sudoku;
import com.yde.solvadoku.R;
import com.yde.solvadoku.UI.Grids.SudokuGrid;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 0;
    private TextView[][] unit;
    private Button partial;
    private Button solve;
    private Button checkSteps;
    Cell[][] puzzle = new Cell[9][9];
    Button[] keypad = new Button[9];
    private static final float actualFontSize = 25;
    private static boolean putPencilMarks;
    final ArrayList<String> checkedItems = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        try {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ignored) {
        }

        putPencilMarks = false;
        final SudokuGrid sudokuGrid = findViewById(R.id.gridLayout);
        unit = sudokuGrid.getUnit();

        keypad = new Button[]{findViewById(R.id.one), findViewById(R.id.two), findViewById(R.id.three), findViewById(R.id.four),
                findViewById(R.id.five), findViewById(R.id.six), findViewById(R.id.seven), findViewById(R.id.eight), findViewById(R.id.nine)};

        for (int i = 0; i < keypad.length; i++) {
            final int finalI = i;
            keypad[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sudokuGrid.setFocusedValue(getString(R.string.cell_focus_value, finalI + 1));
                }
            });

            keypad[i].setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        keypad[finalI].setBackgroundResource(R.drawable.keypad_unfocused);
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        keypad[finalI].setBackgroundResource(R.drawable.keypad_focused);
                    }
                    return false;
                }
            });
        }

        solve = findViewById(R.id.solve);
        solve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (noErrors()) {
                    checkSteps.setEnabled(true);
                    puzzle = new Cell[9][9];
                    Sudoku.cellCount = 0;

                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            puzzle[i][j] = new Cell();
                        }
                    }

                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            if ((unit[i][j].getCurrentTextColor() == getResources().getColor(R.color.colorDeepBlue) && hasValue(unit[i][j]))) {
                                unit[i][j].setText("");
                            }
                        }
                    }

                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            if (hasValue(unit[i][j])) {
                                Sudoku.placeNumber(puzzle, i, j, value(unit[i][j]));
                            }
                        }
                    }

                    Sudoku.algorithm = new ArrayList<>();
                    Sudoku.insert = new ArrayList<>();
                    Sudoku.remove = new ArrayList<>();
                    Sudoku.viewHolderList = new ArrayList<>();

                    Sudoku.Solve(puzzle);

                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            if (!hasValue(unit[i][j]) && puzzle[i][j].getSolution() != 0) {
                                unit[i][j].setTextSize(actualFontSize);
                                unit[i][j].setGravity(Gravity.CENTER);
                                unit[i][j].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                                unit[i][j].setTextColor(getResources().getColor(R.color.colorDeepBlue));
                                unit[i][j].setText(String.valueOf(puzzle[i][j].getSolution()));
                            }
                            if (!hasValue(unit[i][j])) {
                                unit[i][j].setTextColor(getResources().getColor(R.color.colorDeepBlue));
                                unit[i][j].setText(String.valueOf(puzzle[i][j].getSolution()));
                            }
                            makeNotEditable(unit[i][j], unit[i][j].getCurrentTextColor());
                            clearBackground(unit[i][j], i, j);
                        }
                    }

                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            if ((unit[i][j].getCurrentTextColor() == getResources().getColor(R.color.colorDeepBlue) && hasValue(unit[i][j])))
                                disableBackground(unit[i][j], i, j);
                        }
                    }

                    if (Sudoku.cellCount == 81) {
                        solve.setEnabled(false);
                        partial.setEnabled(false);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.invalid_input, Toast.LENGTH_LONG).show();
                }
            }
        });

        partial = (Button) findViewById(R.id.partial);
        partial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (noErrors()) {
                    Context context = new ContextThemeWrapper(MainActivity.this, R.style.CustomDialog);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = getLayoutInflater();
                    View customTitle = inflater.inflate(R.layout.custom_title, null);
                    final String[] logics = {"Naked Single", "Hidden Single", "Naked Pair", "Pointing Pair",
                            "Claiming Pair", "Hidden Pair", "Naked Triple", "Hidden Triple", "X-Wing", "Swordfish", "Jellyfish",
                            "Naked Quad", "Hidden Quad", "Finned X-Wing", "Finned Swordfish", "Finned Jellyfish", "Brute Force"};
                    boolean[] previous = new boolean[logics.length];
                    for (int i = 0; i < logics.length; i++) {
                        if (checkedItems.contains(logics[i])) {
                            previous[i] = true;
                        }
                    }
                    builder.setCustomTitle(customTitle).setMultiChoiceItems(logics, previous, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                            if (isChecked) {
                                checkedItems.add(logics[i]);
                            } else checkedItems.remove(logics[i]);
                        }
                    })
                            .setPositiveButton(R.string.solve, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int x) {
                                    if (checkedItems.size() != 0) {
                                        checkSteps.setEnabled(true);
                                        puzzle = new Cell[9][9];
                                        Sudoku.cellCount = 0;

                                        for (int i = 0; i < 9; i++) {
                                            for (int j = 0; j < 9; j++) {
                                                puzzle[i][j] = new Cell();
                                            }
                                        }

                                        for (int i = 0; i < 9; i++) {
                                            for (int j = 0; j < 9; j++) {
                                                if ((unit[i][j].getCurrentTextColor() == getResources().getColor(R.color.colorDeepBlue) && hasValue(unit[i][j]))) {
                                                    unit[i][j].setText("");
                                                }
                                            }
                                        }

                                        for (int i = 0; i < 9; i++) {
                                            for (int j = 0; j < 9; j++) {
                                                if (hasValue(unit[i][j])) {
                                                    Sudoku.placeNumber(puzzle, i, j, value(unit[i][j]));
                                                }
                                            }
                                        }

                                        Sudoku.algorithm = new ArrayList<>();
                                        Sudoku.insert = new ArrayList<>();
                                        Sudoku.remove = new ArrayList<>();
                                        Sudoku.viewHolderList = new ArrayList<>();

                                        Sudoku.partiallySolve(puzzle, checkedItems);

                                        gridAsNecessary();

                                        if (Sudoku.cellCount == 81) {
                                            solve.setEnabled(false);
                                            partial.setEnabled(false);
                                        }
                                        if (Sudoku.viewHolderList.size() == 0) {
                                            Toast.makeText(MainActivity.this, R.string.no_effect, Toast.LENGTH_LONG).show();
                                        }
                                    } else
                                        Toast.makeText(MainActivity.this, R.string.no_strat_chosen, Toast.LENGTH_LONG).show();

                                    invalidateOptionsMenu();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();


                } else {
                    Toast.makeText(MainActivity.this, R.string.invalid_input, Toast.LENGTH_LONG).show();
                }
            }
        });

        checkSteps = (Button) findViewById(R.id.check_steps);
        checkSteps.setEnabled(false);
        checkSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String[]> viewHolderList = Sudoku.viewHolderList;
                if (viewHolderList.size() != 0) {
                    Intent intent = new Intent(MainActivity.this, StepsActivity.class);
                    intent.putExtra("CheckSteps", viewHolderList);
                    startActivity(intent);
                } else
                    Toast.makeText(MainActivity.this, R.string.no_effect, Toast.LENGTH_LONG).show();
            }
        });

    }

    private boolean noErrors() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (duplicateInColumn(c) || duplicateInRow(r) || duplicateInSubsquare(r - r % 3, c - c % 3)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void makeNotEditable(TextView textView, int color) {
        textView.setFocusable(false);
        textView.setEnabled(false);
        textView.setTextColor(color);
        textView.setCursorVisible(false);
    }

    private int value(TextView textView) {
        return Integer.parseInt(textView.getText().toString());
    }

    private boolean hasValue(TextView textView) {
        for (int i = 1; i <= 9; i++) {
            if (textView.getText().toString().equals(String.valueOf(i)))
                return true;
        }
        return false;
    }

    private void makeEditable(TextView textView) {
        textView.setFocusableInTouchMode(true);
        textView.setTextColor(getResources().getColor(R.color.colorBlack));
        textView.setEnabled(true);
        textView.setCursorVisible(true);
    }

    @Override
    public void onFocusChange(View view, boolean b) {

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                clearBackground(unit[r][c], r, c);
            }
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (view.getId() == unit[r][c].getId()) {
                    selectedBackground(unit[r][c], r, c);
                }

            }
        }

        for (int r = 0; r < 9; r++) {
            if (duplicateInRow(r)) {
                for (int c = 0; c < 9; c++) {
                    wrongBackground(unit[r][c], r, c);
                }
            }
        }

        for (int c = 0; c < 9; c++) {
            if (duplicateInColumn(c)) {
                for (int r = 0; r < 9; r++) {
                    wrongBackground(unit[r][c], r, c);
                }
            }
        }

        for (int sub_r = 0; sub_r < 9; sub_r += 3) {
            for (int sub_c = 0; sub_c < 9; sub_c += 3) {
                if (duplicateInSubsquare(sub_r, sub_c)) {
                    for (int r = sub_r; r < sub_r + 3; r++) {
                        for (int c = sub_c; c < sub_c + 3; c++) {
                            wrongBackground(unit[r][c], r, c);
                        }
                    }
                }
            }
        }

    }

    private boolean duplicateInSubsquare(int sub_r, int sub_c) {
        for (int num = 1; num < 10; num++) {
            int cnt = 0;
            for (int r = sub_r; r < sub_r + 3; r++) {
                for (int c = sub_c; c < sub_c + 3; c++) {
                    if (hasValue(unit[r][c])) {
                        if (value(unit[r][c]) == num) {
                            cnt++;
                            if (cnt == 2) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean duplicateInColumn(int c) {
        for (int num = 1; num < 10; num++) {
            int cnt = 0;
            for (int r = 0; r < 9; r++) {
                if (hasValue(unit[r][c])) {
                    if (value(unit[r][c]) == num) {
                        cnt++;
                        if (cnt == 2) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean duplicateInRow(int r) {
        for (int num = 1; num < 10; num++) {
            int cnt = 0;
            for (int c = 0; c < 9; c++) {
                if (hasValue(unit[r][c])) {
                    if (value(unit[r][c]) == num) {
                        cnt++;
                        if (cnt == 2) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void clearBackground(TextView textView, int r, int c) {
        if (c == 2 || c == 5) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_right);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_right);
            } else
                textView.setBackgroundResource(R.drawable.right_border);
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_left);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_left);
            } else
                textView.setBackgroundResource(R.drawable.left_border);
        } else if (r == 2 || r == 5) {
            textView.setBackgroundResource(R.drawable.bottom_border);
        } else if (r == 3 || r == 6) {
            textView.setBackgroundResource(R.drawable.top_border);
        } else
            textView.setBackgroundResource(R.drawable.border);
    }

    private void disableBackground(TextView textView, int r, int c) {
        if (c == 2 || c == 5) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_rightgrey);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_rightgrey);
            } else
                textView.setBackgroundResource(R.drawable.right_bordergrey);
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_leftgrey);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_leftgrey);
            } else
                textView.setBackgroundResource(R.drawable.left_bordergrey);
        } else if (r == 2 || r == 5) {
            textView.setBackgroundResource(R.drawable.bottom_bordergrey);
        } else if (r == 3 || r == 6) {
            textView.setBackgroundResource(R.drawable.top_bordergrey);
        } else
            textView.setBackgroundResource(R.drawable.bordergrey);
    }

    private void selectedBackground(TextView textView, int r, int c) {
        if (c == 2 || c == 5) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_rightblue);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_rightblue);
            } else
                textView.setBackgroundResource(R.drawable.right_borderblue);
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_leftblue);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_leftblue);
            } else
                textView.setBackgroundResource(R.drawable.left_borderblue);
        } else if (r == 2 || r == 5) {
            textView.setBackgroundResource(R.drawable.bottom_borderblue);
        } else if (r == 3 || r == 6) {
            textView.setBackgroundResource(R.drawable.top_borderblue);
        } else
            textView.setBackgroundResource(R.drawable.borderblue);
    }

    private void wrongBackground(TextView textView, int r, int c) {
        if (c == 2 || c == 5) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_rightred);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_rightred);
            } else
                textView.setBackgroundResource(R.drawable.right_borderred);
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                textView.setBackgroundResource(R.drawable.bottom_leftred);
            } else if (r == 3 || r == 6) {
                textView.setBackgroundResource(R.drawable.top_leftred);
            } else
                textView.setBackgroundResource(R.drawable.left_borderred);
        } else if (r == 2 || r == 5) {
            textView.setBackgroundResource(R.drawable.bottom_borderred);
        } else if (r == 3 || r == 6) {
            textView.setBackgroundResource(R.drawable.top_borderred);
        } else
            textView.setBackgroundResource(R.drawable.borderred);
    }

    private class CellTextWatcher implements TextWatcher {

        private int r, c;

        private CellTextWatcher(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (duplicateInRow(r)) {
                for (int c = 0; c < 9; c++) {
                    wrongBackground(unit[r][c], r, c);
                }
            } else {
                for (int c = 0; c < 9; c++) {
                    if (!duplicateInColumn(c) && !duplicateInSubsquare(r - r % 3, c - c % 3)) {
                        clearBackground(unit[r][c], r, c);
                        if (c == this.c) {
                            selectedBackground(unit[this.r][this.c], this.r, this.c);
                        }
                    }
                }
            }

            if (duplicateInColumn(c)) {
                for (int r = 0; r < 9; r++) {
                    wrongBackground(unit[r][c], r, c);
                }
            } else {
                for (int r = 0; r < 9; r++) {
                    if (!duplicateInRow(r) && !duplicateInSubsquare(r - r % 3, c - c % 3)) {
                        clearBackground(unit[r][c], r, c);
                        if (r == this.r) {
                            selectedBackground(unit[this.r][this.c], this.r, this.c);
                        }
                    }
                }
            }

            if (duplicateInSubsquare(r - r % 3, c - c % 3)) {
                int sub_r = r - r % 3;
                int sub_c = c - c % 3;
                for (int r = sub_r; r < sub_r + 3; r++) {
                    for (int c = sub_c; c < sub_c + 3; c++) {
                        wrongBackground(unit[r][c], r, c);
                    }
                }
            } else {
                int sub_r = r - r % 3;
                int sub_c = c - c % 3;
                for (int r = sub_r; r < sub_r + 3; r++) {
                    for (int c = sub_c; c < sub_c + 3; c++) {
                        if (!duplicateInRow(r) && !duplicateInColumn(c)) {
                            clearBackground(unit[r][c], r, c);
                            if (r == this.r && c == this.c) {
                                selectedBackground(unit[this.r][this.c], this.r, this.c);
                            }
                        }
                    }
                }
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_clear_all:
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        unit[i][j].setGravity(Gravity.CENTER);
                        unit[i][j].setTextSize(actualFontSize);
                        unit[i][j].setKeyListener(DigitsKeyListener.getInstance("123456789"));
                        unit[i][j].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                        makeEditable(unit[i][j]);
                        unit[i][j].setText("");
                        unit[i][j].clearFocus();
                        clearBackground(unit[i][j], i, j);
                    }
                }
                solve.setEnabled(true);
                partial.setEnabled(true);
                checkSteps.setEnabled(false);
                invalidateOptionsMenu();
                return true;

            case R.id.about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                intent.putExtra("input", putPencilMarks);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                putPencilMarks = data.getBooleanExtra("key", true);
                gridAsNecessary();
            }
        }
    }

    private void gridAsNecessary() {
        try {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (!hasValue(unit[i][j]) && puzzle[i][j].getSolution() != 0) {
                        unit[i][j].setTextSize(actualFontSize);
                        unit[i][j].setGravity(Gravity.CENTER);
                        unit[i][j].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                        unit[i][j].setTextColor(getResources().getColor(R.color.colorDeepBlue));
                        unit[i][j].setText(String.valueOf(puzzle[i][j].getSolution()));
                    } else if (!hasValue(unit[i][j]) && putPencilMarks) {
                        unit[i][j].setTextSize(actualFontSize / 2.5f);
                        unit[i][j].setGravity(Gravity.TOP);

                        unit[i][j].setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
                        unit[i][j].setTextColor(getResources().getColor(R.color.colorBlack));
                        unit[i][j].setText(puzzle[i][j].listCandidates());
                    } else if (!hasValue(unit[i][j]) && !putPencilMarks) {
                        unit[i][j].setText("");
                    }
                    makeNotEditable(unit[i][j], unit[i][j].getCurrentTextColor());
                    clearBackground(unit[i][j], i, j);
                }
            }

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if ((unit[i][j].getCurrentTextColor() == getResources().getColor(R.color.colorDeepBlue) && hasValue(unit[i][j])))
                        disableBackground(unit[i][j], i, j);
                }
            }
        } catch (NullPointerException ignored) {

        }
    }

}