package com.example.footballmeet.registration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    private EditText et_email, et_password;
    private Button btn_acept, btn_cancel;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        finds();
        setListeners();

        // Verificar si hay credenciales guardadas
        checkSavedCredentials();
    }




    private void finds() {
        et_email = findViewById(R.id.et_emailLogIn);
        et_password = findViewById(R.id.et_passwordLogIn);
        btn_acept = findViewById(R.id.btn_acept);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    private void setListeners() {
        btn_acept.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }


    /**
     * Comprueba si hay credenciales guardadas
     */
    private void checkSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", null);
        String savedPassword = sharedPreferences.getString("password", null);
        if (savedEmail != null && savedPassword != null) {
            // Intentar iniciar sesión automáticamente con las credenciales guardadas
            logIn(savedEmail, savedPassword);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_acept:
                if (comprobarCamposLogIn()) {
                    String email = et_email.getText().toString();
                    String password = et_password.getText().toString();
                    logIn(email, password);
                }
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
    }


    /**
     * Comprueba los campos email y password
     * @return
     */
    private boolean comprobarCamposLogIn() {
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            MainActivity.showToast(this, "Los campos deben estar cubiertos");
            return false;
        }

        return true;
    }


    /**
     * Hace el logIn
     * @param email
     * @param password
     */
    public void logIn(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            MainActivity.showToast(LogIn.this, "Inicio de sesión exitoso");

                            // Guardar credenciales en SharedPreferences
                            saveCredentials(email, password);
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // Error al iniciar sesión
                            MainActivity.showToast(LogIn.this, "El email y/o la contraseña no son correctos");
                        }
                    }
                });
    }

    /**
     * Guarda las credenciales
     * @param email
     * @param password
     */
    private void saveCredentials(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }
}
