package com.example.footballmeet.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.google.firebase.auth.FirebaseAuth;
import android.content.SharedPreferences;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                // Acción de cerrar sesión
                FirebaseAuth.getInstance().signOut(); // Cerrar sesión de Firebase

                // Eliminar las credenciales guardadas
                SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear(); // Elimina todas las preferencias
                editor.apply();


                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish(); // Finalizar la actividad actual
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickBtnMenu(View view) {
        Intent intent = null;

        switch (view.getId()) {
            case R.id.btn_partidosMenu:
                // Lógica para partidos
                finish();
                break;

            case R.id.btn_perfilMenu:
                // Lógica para perfil
                finish();
                break;

            case R.id.btn_equiposMenu:
                // Lógica para equipos
                finish();
                break;

            case R.id.btn_torneosMenu:
                // Lógica para torneos
                finish();
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
