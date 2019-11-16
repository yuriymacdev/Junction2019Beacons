package com.example.chernobyl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        ((ImageView)findViewById(R.id.imageView2)).setBackgroundResource(R.drawable.ic_radiation);
    }
}
