package com.yde.solvadoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    Switch switcher;
    boolean isChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        isChecked = getIntent().getBooleanExtra("input", true);
        switcher = findViewById(R.id.switcher);
        switcher.setChecked(isChecked);
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checker) {
                isChecked = checker;
                Intent intent = new Intent();
                intent.putExtra("key", isChecked);
                setResult(RESULT_OK, intent);
            }
        });
    }

}