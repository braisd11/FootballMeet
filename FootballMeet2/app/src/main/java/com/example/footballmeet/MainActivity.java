package com.example.footballmeet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.footballmeet.bd.MiDBHelper;
import com.example.footballmeet.registration.LogIn;
import com.example.footballmeet.registration.SignIn;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0;
    private Button btn_LogIn, btn_Exit, btn_Registr;
    private SQLiteDatabase database;
    private MiDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new MiDBHelper(this);


        finds();

    }

    private void finds() {

        btn_LogIn = findViewById(R.id.btn_LogIn);
        btn_Exit = findViewById(R.id.btn_Exit);
        btn_Registr = findViewById(R.id.btn_Registr);

    }

    public void onClickBtn(View view) {
        Intent intent = null;

        switch (view.getId()) {
            case R.id.btn_LogIn:

                intent = new Intent(this, LogIn.class);
                break;

            case R.id.btn_Registr:

                intent = new Intent(this, SignIn.class);
                break;

            case R.id.btn_Exit:

                finishAffinity();
                break;
        }

        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                showToast(this, "Iniciado correctamente");

            } else if (resultCode == RESULT_CANCELED) {

                showToast(this, "Cancelado correctamente");

            }
        }
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}