package com.yde.solvadoku.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.yde.solvadoku.R;

public class AboutActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ImageView github = findViewById(R.id.github_repo_link);
        github.setOnClickListener(view -> {
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            String url = (String) view.getTag();
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }
}