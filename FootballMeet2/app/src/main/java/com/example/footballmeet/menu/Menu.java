package com.example.footballmeet.menu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.footballmeet.R;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void onClickBtnMenu(View view) {

        Intent intent= null;

        switch (view.getId()){
            case R.id.btn_partidosMenu:

                finish();

                break;

            case R.id.btn_perfilMenu:

                finish();

                break;

            case R.id.btn_equiposMenu:

                finish();

                break;

            case R.id.btn_torneosMenu:

                finish();

                break;
        }

        if (intent!=null){

            startActivity(intent);

        }

    }
}