package com.example.footballmeet.registration;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private Button btn_DatePickerDialog, btn_aceptSignIn, btn_cancelSignIn;
    private EditText et_newNombre, et_newUser, et_newPassword, et_newTelefono, et_newEmail, et_newFecha;

    private String nombre, user, password, email, telefono, fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_datePickerDialog:
                mostrarDatePicker();
                break;

            case R.id.btn_aceptSignIn:
                if (comprobarCamposSignIn()) {
                    final String userEmail = email;
                    final String userPassword = password;

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Usuario creado exitosamente en Firebase Authentication
                                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");
                                        DatabaseReference newUserRef = usersRef.child(userId);

                                        newUserRef.child("email").setValue(userEmail);
                                        newUserRef.child("password").setValue(hashPassword(userPassword));
                                        newUserRef.child("nombre").setValue(nombre);
                                        newUserRef.child("fecha").setValue(fecha);
                                        newUserRef.child("telefono").setValue(telefono);

                                        MainActivity.showToast(SignIn.this, "Usuario creado y registrado exitosamente");
                                        Intent intent = new Intent();
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } else {
                                        // Error al crear usuario en Firebase Authentication
                                        MainActivity.showToast(SignIn.this, "Error al crear usuario: " + task.getException().getMessage());
                                    }
                                }
                            });
                }
                break;


            case R.id.btn_cancelSignIn:
                // Crear el Intent y establecer el resultado
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }



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

            } else {
                if (nombre.trim().isEmpty() || user.trim().isEmpty() || password.trim().isEmpty() ||
                        email.trim().isEmpty() || telefono.trim().isEmpty() || fecha.trim().isEmpty()) {

                    MainActivity.showToast(this, "Todos los campos deben estar cubiertos");

                } else if (!sonSoloNumeros(telefono)) {

                    MainActivity.showToast(this, "El teléfono sólo pueden ser números");

                } else if (!validarEmail(email)) {

                    MainActivity.showToast(this, "El formato del email no es válido");

                } else {

                    correcto = true;

                }
            }
        } catch (Exception e) {
        }

        return correcto;
    }


    public boolean sonSoloNumeros(String telefono) {
        for (int i = 0; i < telefono.length(); i++) {
            if (!Character.isDigit(telefono.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    public boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    public static String hashPassword(String password) {
        try {
            // Crea una instancia de MessageDigest con el algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Aplica el hash a la contraseña
            byte[] hashBytes = digest.digest(password.getBytes());

            // Convierte el hash a una representación hexadecimal
            StringBuilder builder = new StringBuilder();
            for (byte b : hashBytes) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            // Manejar la excepción
            e.printStackTrace();
            return null;
        }
    }

}

