package com.example.footballmeet.registration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.example.footballmeet.bd.MiDBHelper;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private Button btn_DatePickerDialog, btn_aceptSignIn, btn_cancelSignIn;
    private EditText et_newNombre, et_newUser, et_newPassword, et_newTelefono, et_newEmail, et_newFecha;

    private String nombre, user, password, email, telefono, fecha;
    private MiDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        dbHelper = new MiDBHelper(this);

        finds();

        setListeners();

    }


    private void finds() {
        btn_DatePickerDialog = findViewById(R.id.btn_datePickerDialog);
        et_newNombre = findViewById(R.id.et_newNombre);
        et_newUser = findViewById(R.id.et_newUser);
        et_newPassword = findViewById(R.id.et_newPassword);
        et_newTelefono = findViewById(R.id.et_newTelefono);
        et_newEmail = findViewById(R.id.et_newEmail);
        et_newFecha = findViewById(R.id.et_newFecha);
        btn_aceptSignIn = findViewById(R.id.btn_aceptSignIn);
        btn_cancelSignIn = findViewById(R.id.btn_cancelSignIn);
        et_newFecha.setEnabled(false);
    }

    private void setListeners() {
        btn_aceptSignIn.setOnClickListener(this);
        btn_cancelSignIn.setOnClickListener(this);
        btn_DatePickerDialog.setOnClickListener(this);
    }



    /**
     * onClick de los botones
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {

            case R.id.btn_datePickerDialog:
                mostrarDatePicker();
                break;

            case R.id.btn_aceptSignIn:
                if (comprobarCamposSignIn()) {
                    if (!dbHelper.verificarUsuario(user)) {
                        long result = dbHelper.insertarUsuario(user, email, password, nombre, fecha, telefono);
                        if (result != -1) {
                            MainActivity.showToast(SignIn.this, "Introducido con éxito en la base de datos");
                        } else {
                            MainActivity.showToast(SignIn.this, "No se pudo introducir el dato correctamente");
                        }
                        intent = new Intent();
                        setResult(RESULT_OK, intent);
                    } else {
                        MainActivity.showToast(SignIn.this, "Ya existe este nombre de usuario");
                    }
                }
                break;

            case R.id.btn_cancelSignIn:
                intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                break;

        }
        if (intent != null) {
            finish();
        }
    }





    /**
     * Muestra el DatePicker y escribe la fecha seleccionada en el EditText
     */
    private void mostrarDatePicker() {
        // Obtener la fecha actual
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear un DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Manejar la fecha seleccionada por el usuario
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;

                        et_newFecha.setText(selectedDate);

                    }
                }, year, month, dayOfMonth);

        // Mostrar el DatePickerDialog
        datePickerDialog.show();
    }


    /////////////////////////////VALIDACIONES//////////////////////////////////////


    /**
     * Comprueba todos los campos
     * @return true si los campos están correctos
     */
    private boolean comprobarCamposSignIn() {
        boolean correcto = false;

        try {
            nombre = et_newNombre.getText().toString();
            user = et_newUser.getText().toString();
            password = et_newPassword.getText().toString();
            email = et_newEmail.getText().toString();
            telefono = et_newTelefono.getText().toString();
            fecha = et_newFecha.getText().toString();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                if (Stream.of(nombre, user, password, email, telefono, fecha).anyMatch(s -> s.trim().isEmpty())) {

                    MainActivity.showToast(this, "Todos los campos deben estar cubiertos");

                } else if (!sonSoloNumeros(telefono)) {

                    MainActivity.showToast(this, "El teléfono sólo pueden ser números");

                } else if (!validarEmail(email)) {

                    MainActivity.showToast(this, "El formato del email no es válido");

                } else {

                    correcto = true;

                }

            }
        } catch (Exception e) {}

        return correcto;
    }


    /**
     * Comprueba si solo son números el telefono
     * @param telefono
     * @return
     */
    public boolean sonSoloNumeros(String telefono) {
        for (int i = 0; i < telefono.length(); i++) {
            if (!Character.isDigit(telefono.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Comprueba si el email tiene el formato correcto
     * @param email
     * @return true si el formato es correcto
     */
    public boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}