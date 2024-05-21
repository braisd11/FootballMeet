package com.example.footballmeet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footballmeet.menu.Menu;
import com.example.footballmeet.registration.LogIn;
import com.example.footballmeet.registration.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        checkSavedCredentials();

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
                Intent intent = new Intent(this, Menu.class);
                startActivity(intent);

            } else if (resultCode == RESULT_CANCELED) {

                showToast(this, "Cancelado correctamente");

            }
        }
    }


    /**
     * Checkea si hubo un inicio de sesión previo, si lo hubo incicia sesión directamente
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

    /**
     * Activity para iniciar sesión con las credenciales
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
                            showToast(MainActivity.this, "Inicio de sesión exitoso");

                            // Guardar credenciales en SharedPreferences
                            saveCredentials(email, password);

                            Intent intent = new Intent(MainActivity.this, Menu.class);
                            startActivity(intent);
                        } else {
                            // Error al iniciar sesión
                            showToast(MainActivity.this, "El email y/o la contraseña no son correctos");
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

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}