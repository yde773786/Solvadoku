package com.yde.solvadoku.UI.controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.yde.solvadoku.R;

public class AboutActivity extends AppCompatActivity {

    Intent open_link_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView github_link = findViewById(R.id.github_repo_link);
        github_link.setOnClickListener(view -> {
            open_link_intent = new Intent();
            open_link_intent.setAction(Intent.ACTION_VIEW);
            String url = (String) view.getTag();
            open_link_intent.setData(Uri.parse(url));
            startActivity(open_link_intent);
        });

        ImageView send_email_link = findViewById(R.id.email_link);
        send_email_link.setOnClickListener(view -> {
            open_link_intent = new Intent();
            open_link_intent.setAction(Intent.ACTION_VIEW);
            String url = (String) view.getTag();
            open_link_intent.setData(Uri.parse(url));
            startActivity(open_link_intent);
        });
    }
}