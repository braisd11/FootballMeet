package com.example.footballmeet.registration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.example.footballmeet.bd.MiDBHelper;

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    private EditText et_user, et_password;
    private Button btn_acept, btn_cancel;
    private MiDBHelper dbHelper;
    private String usuario, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        dbHelper = new MiDBHelper(this);

        finds();

        setListeners();

    }

    private void finds() {
        et_user = findViewById(R.id.et_userLogIn);
        et_password = findViewById(R.id.et_passwordLogIn);
        btn_acept = findViewById(R.id.btn_acept);
        btn_cancel = findViewById(R.id.btn_cancel);
    }
    private void setListeners() {
        btn_acept.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }



    /**
     * onClik de los botones
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {

            case R.id.btn_acept:

                if (comprobarCamposLogIn()) {
                    if (dbHelper.verificarCredenciales(usuario, password)) {
                        intent = new Intent();
                        setResult(RESULT_OK, intent);
                    } else {
                        MainActivity.showToast(LogIn.this, "El usuario y/o la contraseña no son correctos");
                    }
                }
                break;

            case R.id.btn_cancel:

                intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                break;

        }

        if (intent != null) {

            finish();

        }
    }



    /**
     * Comprueba los campos del LogIn
     * @return true si son correctos
     */
    private boolean comprobarCamposLogIn() {

        boolean correcto = true;

        usuario = et_user.getText().toString();

        password = et_password.getText().toString();

        if (usuario.trim().isEmpty() || password.trim().isEmpty()) {

            MainActivity.showToast(this, "Los campos deben estar cubiertos");
            correcto = false;

        }

        return correcto;
    }

}