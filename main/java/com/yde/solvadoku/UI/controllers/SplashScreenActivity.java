package com.yde.solvadoku.UI.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.yde.solvadoku.R;

public class SplashScreenActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        // Switching to the Main Activity.
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);

        // Finishing SplashScreenActivity.
        finish();
    }
} // end of SplashScreenActivity
