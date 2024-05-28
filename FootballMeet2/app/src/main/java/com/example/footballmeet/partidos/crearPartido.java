package com.example.footballmeet.partidos;

import static com.example.footballmeet.MainActivity.showToast;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.example.footballmeet.registration.SignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class crearPartido extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_SELECT_LOCATION = 2;
    private Uri selectedImageUri = null;

    private String ubicacion;

    private TextView et_newTimeMatch, et_newFechaMatch;

    private EditText etFechaPartido;
    private EditText etHoraPartido;
    private EditText etDescripcion;
    private EditText etCapacidad;
    private EditText etPrecio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_partido);

        finds();
    }

    private void finds() {
        et_newTimeMatch = findViewById(R.id.et_newTimeMatch);
        et_newFechaMatch = findViewById(R.id.et_newFechaMatch);

        etFechaPartido = findViewById(R.id.et_newFechaMatch);
        etHoraPartido = findViewById(R.id.et_newTimeMatch);
        etDescripcion = findViewById(R.id.et_newMatchDescripcion);
        etCapacidad = findViewById(R.id.et_newCapacidadJugadores);
        etPrecio = findViewById(R.id.et_newPrecioMatch);
    }

    public void onClickBtnNewMatch(View view) {

        switch (view.getId()) {

            case R.id.btn_dateMatch:
                mostrarDatePicker();
                break;

            case R.id.btn_timeMatch:
                mostrarTimePickerDialog();
                break;

            case R.id.btn_selectLocation:
                seleccionarUbicacion();
                break;

            case R.id.btn_selectImage:
                seleccionarImagen();
                break;

            case R.id.btn_aceptNewMatch:
                crearPartido();
                break;

            case R.id.btn_cancelNewMatch:
                finish();
                break;
        }
    }

    private void seleccionarUbicacion() {
        Intent intent = new Intent(this, SeleccionarUbicacion.class);
        startActivityForResult(intent, REQUEST_SELECT_LOCATION);
    }

    private void seleccionarImagen() {
        // Crear un intent para abrir la galería
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Verificar si hay alguna aplicación que pueda manejar el intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Iniciar la actividad de la galería esperando un resultado
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            // Mostrar un mensaje de error si no hay aplicaciones disponibles para manejar el intent
            MainActivity.showToast(this, "No hay aplicaciones disponibles para abrir la galería.");
        }
    }



    private void crearPartido() {
        String fechaPartido = etFechaPartido.getText().toString().trim();
        String horaPartido = etHoraPartido.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (TextUtils.isEmpty(fechaPartido) || TextUtils.isEmpty(horaPartido) ||
                TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(capacidadStr) ||
                TextUtils.isEmpty(precioStr)) {
            MainActivity.showToast(this, "Por favor completa todos los campos");
            return;
        }

        int capacidad = Integer.parseInt(capacidadStr);
        double precio = Double.parseDouble(precioStr);

        // Aquí puedes crear el partido con los datos ingresados
        // Puedes llamar a un método para guardar el partido en la base de datos, por ejemplo

        // Lógica para guardar el partido en la base de datos
        guardarPartidoEnBaseDeDatos(fechaPartido,horaPartido,descripcion,capacidad,precio, selectedImageUri.toString(), ubicacion);

        MainActivity.showToast(this, "Partido creado exitosamente");
        finish(); // Finalizar la actividad actual
    }

    private void guardarPartidoEnBaseDeDatos(String fechaPartido, String horaPartido, String descripcion, int capacidad, double precio, String imagenUrl, String ubicacion) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // Obtiene el ID único del usuario actualmente autenticado
            String userId = currentUser.getUid();

            // Crea un nodo "Partidos" en la base de datos y agrega un nuevo partido
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Partidos");
            String partidoId = databaseRef.push().getKey(); // Obtener un ID único para el partido

            Partido partido = new Partido(partidoId, userId, fechaPartido, horaPartido, descripcion, capacidad, precio, imagenUrl, ubicacion);

            // Guarda el partido en la base de datos
            databaseRef.child(partidoId).setValue(partido);
        }
    }


    public void mostrarTimePickerDialog() {
        // Obtenemos la hora actual
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);
        int minuto = calendar.get(Calendar.MINUTE);

        // Crear una instancia de TimePickerDialog y establecer el listener
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Redondear los minutos al múltiplo de 15 más cercano
                        minute = Math.round(minute / 15) * 15;
                        // Actualizar el TextView con la hora seleccionada
                        String horaSeleccionada = String.format("%02d:%02d", hourOfDay, minute);
                        et_newTimeMatch.setText(horaSeleccionada);
                    }
                }, hora, minuto, true);

        // Mostrar el cuadro de diálogo
        timePickerDialog.show();
    }


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

                et_newFechaMatch.setText(fechaSeleccionada);

            }
        }, year, month, day);

        datePickerDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Obtener la URI de la imagen seleccionada
            selectedImageUri = data.getData();
        } else if (requestCode == REQUEST_SELECT_LOCATION && resultCode == RESULT_OK) {
            // Extraer la ubicación seleccionada desde el intent data
            ubicacion = data.getStringExtra("ubicacion");

            // Hacer lo que necesites con la ubicación seleccionada
            // Por ejemplo, mostrarla en un TextView o guardarla en una variable
        }
    }

}