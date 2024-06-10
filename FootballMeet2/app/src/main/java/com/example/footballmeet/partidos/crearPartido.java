package com.example.footballmeet.partidos;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

public class crearPartido extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_SELECT_LOCATION = 2;

    private Uri selectedImageUri;

    private EditText etFechaPartido;
    private EditText etHoraPartido;
    private EditText etDescripcion;
    private EditText etCapacidad;
    private EditText etPrecio;
    private TextView tv_image;

    private String latitude;
    private String longitude;
    private String ubicacion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_partido);

        finds();
    }

    private void finds() {
        etFechaPartido = findViewById(R.id.et_newFechaMatch);
        etHoraPartido = findViewById(R.id.et_newTimeMatch);
        etDescripcion = findViewById(R.id.et_newMatchDescripcion);
        etCapacidad = findViewById(R.id.et_newCapacidadJugadores);
        etPrecio = findViewById(R.id.et_newPrecioMatch);
        tv_image = findViewById(R.id.tv_image);
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
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            MainActivity.showToast(this, "No hay aplicaciones disponibles para abrir la galería.");
        }
    }

    private void crearPartido() {
        String fechaPartido = etFechaPartido.getText().toString().trim();
        String horaPartido = etHoraPartido.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (TextUtils.isEmpty(fechaPartido) || TextUtils.isEmpty(horaPartido) || TextUtils.isEmpty(capacidadStr) ||
                TextUtils.isEmpty(precioStr)) {
            MainActivity.showToast(this, "Por favor completa todos los campos");
            return;
        }

        int capacidad = Integer.parseInt(capacidadStr);
        double precio = Double.parseDouble(precioStr);

        if (selectedImageUri != null) {
            guardarImagenEnFirebaseStorage(fechaPartido, horaPartido, descripcion, capacidad, precio);
        } else {
            guardarPartidoEnBaseDeDatos(fechaPartido, horaPartido, descripcion, capacidad, precio, null);
        }

        MainActivity.showToast(this, "Partido creado exitosamente");
        finish();
    }

    private void guardarImagenEnFirebaseStorage(final String fechaPartido, final String horaPartido, final String descripcion, final int capacidad, final double precio) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images");

        final StorageReference imageRef = storageRef.child(selectedImageUri.getLastPathSegment());

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);

            UploadTask uploadTask = imageRef.putStream(inputStream);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    obtenerUrlDeImagen(imageRef, fechaPartido, horaPartido, descripcion, capacidad, precio);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    MainActivity.showToast(crearPartido.this, "Error al subir la imagen: " + e.getMessage());
                    guardarPartidoEnBaseDeDatos(fechaPartido, horaPartido, descripcion, capacidad, precio, null);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            MainActivity.showToast(crearPartido.this, "Error al obtener el InputStream de la imagen: " + e.getMessage());
            guardarPartidoEnBaseDeDatos(fechaPartido, horaPartido, descripcion, capacidad, precio, null);
        }
    }


    private void obtenerUrlDeImagen(StorageReference storageRef, final String fechaPartido, final String horaPartido, final String descripcion, final int capacidad, final double precio) {
        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        guardarPartidoEnBaseDeDatos(fechaPartido, horaPartido, descripcion, capacidad, precio, imageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MainActivity.showToast(crearPartido.this, "Error al obtener la URL de la imagen: " + e.getMessage());
                    }
                });
    }

    private void guardarPartidoEnBaseDeDatos(String fechaPartido, String horaPartido, String descripcion, int capacidad, double precio, String imagenUrl) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Partidos");
            String partidoId = databaseRef.push().getKey();

            Partido partido = new Partido(partidoId, userId, fechaPartido, horaPartido, descripcion, capacidad, precio, imagenUrl, ubicacion);

            databaseRef.child(partidoId).setValue(partido);
        }
    }

    public void mostrarTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);
        int minuto = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        minute = Math.round(minute / 15) * 15;
                        String horaSeleccionada = String.format("%02d:%02d", hourOfDay, minute);
                        etHoraPartido.setText(horaSeleccionada);
                    }
                }, hora, minuto, true);

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
                month++;
                String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, month, year);

                etFechaPartido.setText(fechaSeleccionada);

            }
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            tv_image.setText(selectedImageUri.toString());
            tv_image.setVisibility(View.VISIBLE);

        } else if (requestCode == REQUEST_SELECT_LOCATION && resultCode == RESULT_OK) {
            latitude = data.getStringExtra("latitude");
            longitude = data.getStringExtra("longitude");

            ubicacion = latitude + ";" + longitude;

            MainActivity.showToast(this, ubicacion);

        }
    }
}
