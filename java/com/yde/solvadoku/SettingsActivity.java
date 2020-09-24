package com.yde.solvadoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    Switch switcher;
    boolean isChecked;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ImageView github = findViewById(R.id.github);
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                String url = (String) view.getTag();
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        isChecked = getIntent().getBooleanExtra("input", true);
        switcher = findViewById(R.id.switcher);
        switcher.setChecked(isChecked);
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checker) {
                isChecked = checker;
                intent = new Intent();
                intent.putExtra("key", isChecked);
                setResult(RESULT_OK, intent);
            }
        });
    }

}