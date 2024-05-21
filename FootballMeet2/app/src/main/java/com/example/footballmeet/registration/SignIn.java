package com.example.footballmeet.registration;

import static com.example.footballmeet.MainActivity.showToast;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private Button btnDatePickerDialog, btnAcceptSignIn, btnCancelSignIn;
    private EditText etNewNombre, etNewUser, etNewPassword, etNewTelefono, etNewEmail, etNewFecha;

    private String nombre, user, password, email, telefono, fecha;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        finds();
        setListeners();
    }

    private void finds() {
        btnDatePickerDialog = findViewById(R.id.btn_datePickerDialog);
        etNewNombre = findViewById(R.id.et_newNombre);
        etNewUser = findViewById(R.id.et_newUser);
        etNewPassword = findViewById(R.id.et_newPassword);
        etNewTelefono = findViewById(R.id.et_newTelefono);
        etNewEmail = findViewById(R.id.et_newEmail);
        etNewFecha = findViewById(R.id.et_newFecha);
        btnAcceptSignIn = findViewById(R.id.btn_aceptSignIn);
        btnCancelSignIn = findViewById(R.id.btn_cancelSignIn);
        etNewFecha.setEnabled(false);
    }

    private void setListeners() {
        btnAcceptSignIn.setOnClickListener(this);
        btnCancelSignIn.setOnClickListener(this);
        btnDatePickerDialog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_datePickerDialog:
                mostrarDatePicker();
                break;
            case R.id.btn_aceptSignIn:
                if (comprobarCamposSignIn()) {
                    verificarYCrearUsuario();
                }
                break;
            case R.id.btn_cancelSignIn:
                finish();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }


    /**
     * Muestra el selector de fecha de nacimiento
     */
    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++; // Month is 0-based in Calendar
                String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, month, year);
                if (isOlderThan16(fechaSeleccionada)) {
                    fecha = fechaSeleccionada;
                    etNewFecha.setText(fecha);
                } else {
                    showToast(SignIn.this, "Debes tener al menos 16 años para registrarte");
                    mostrarDatePicker(); // Vuelve a mostrar el DatePickerDialog
                }
            }
        }, year, month, day);

        datePickerDialog.show();
    }


    /**
     * Comprueba que tenga más de 16 años
     * @param fechaNacimiento
     * @return
     */
    private boolean isOlderThan16(String fechaNacimiento) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date birthDate = sdf.parse(fechaNacimiento);
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(birthDate);

            Calendar todayCal = Calendar.getInstance();

            int age = todayCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

            // Verifica si el cumpleaños ya pasó este año; si no, resta un año de la edad.
            if (todayCal.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 16;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Comprueba que los campos estén correctos
     * @return
     */
    private boolean comprobarCamposSignIn() {
        boolean correcto = false;

        try {
            nombre = etNewNombre.getText().toString();
            user = etNewUser.getText().toString();
            password = etNewPassword.getText().toString();
            email = etNewEmail.getText().toString();
            telefono = etNewTelefono.getText().toString();
            fecha = etNewFecha.getText().toString();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Stream.of(nombre, user, password, email, telefono, fecha).anyMatch(s -> s.trim().isEmpty()))
                    showToast(this, "Todos los campos deben estar cubiertos");
                else if (!sonSoloNumeros(telefono))
                    showToast(this, "El teléfono sólo pueden ser números");
                else if (!validarEmail(email))
                    showToast(this, "El formato del email no es válido");
                else correcto = true;
            } else {
                if (nombre.trim().isEmpty() || user.trim().isEmpty() || password.trim().isEmpty() ||
                        email.trim().isEmpty() || telefono.trim().isEmpty() || fecha.trim().isEmpty())
                    showToast(this, "Todos los campos deben estar cubiertos");
                else if (!sonSoloNumeros(telefono))
                    showToast(this, "El teléfono sólo pueden ser números");
                else if (!validarEmail(email)) showToast(this, "El formato del email no es válido");
                else correcto = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return correcto;
    }


    /**
     * Verifica si existe usuario en Firebase Authentication
     */
    private void verificarYCrearUsuario() {
        final String userEmail = email;
        final String userPassword = password;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // El usuario ya existe
                    showToast(SignIn.this, "El usuario ya existe");
                } else {
                    // El usuario no existe, proceder con la creación
                    crearUsuario(userEmail, userPassword);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(SignIn.this, "Error al verificar el usuario: " + databaseError.getMessage());
            }
        });
    }


    /**
     * Crea el usuario en Firebase Authentication
     * @param userEmail
     * @param userPassword
     */
    private void crearUsuario(final String userEmail, final String userPassword) {
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuario creado exitosamente en Firebase Authentication
                            saveUserDataBase(userEmail, userPassword);
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // Error al crear usuario en Firebase Authentication
                            showToast(SignIn.this, "Error al crear usuario: " + task.getException().getMessage());
                        }
                    }
                });
    }


    /**
     * Guarda el Usuario en la base de datos de Firebase
     * @param userEmail
     * @param userPassword
     */
    private void saveUserDataBase(String userEmail, String userPassword) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        DatabaseReference newUserRef = usersRef.child(userId);

        newUserRef.child("email").setValue(userEmail);
        newUserRef.child("password").setValue(hashPassword(userPassword));
        newUserRef.child("nombre").setValue(nombre);
        newUserRef.child("fecha").setValue(fecha);
        newUserRef.child("telefono").setValue(telefono);

        showToast(SignIn.this, "Usuario creado y registrado exitosamente");
    }


    /**
     * Comprueba que el teléfono solo sean números
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
     * Valida que el formato del email sea el correcto
     * @param email
     * @return
     */
    public boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    /**
     * Hashea la contraseña para guardarla en la base de datos
     * @param password
     * @return
     */
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
