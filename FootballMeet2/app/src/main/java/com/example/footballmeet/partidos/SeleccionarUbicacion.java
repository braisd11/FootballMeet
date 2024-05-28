package com.example.footballmeet.partidos;

import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class SeleccionarUbicacion extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_ubicacion);

        // Obtén una referencia al SupportMapFragment y configura el mapa cuando esté listo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipula el mapa una vez esté disponible.
     * Este callback se llama cuando el mapa está listo para ser utilizado.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configura un listener para capturar la ubicación seleccionada por el usuario
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLatLng = latLng; // Guarda las coordenadas de la ubicación seleccionada
            }
        });
    }

    // Método para manejar el click del botón en el XML
    public void onClickBtnConfirmLocation(View view) {
        if (selectedLatLng != null) {
            // Crea un Intent para devolver las coordenadas a la actividad anterior
            Intent returnIntent = new Intent();
            returnIntent.putExtra("latitude", selectedLatLng.latitude);
            returnIntent.putExtra("longitude", selectedLatLng.longitude);

            // Establece el resultado del Intent y finaliza la actividad actual
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            // Maneja el caso en el que no se ha seleccionado ninguna ubicación
            MainActivity.showToast(this, "Por favor, selecciona una ubicación en el mapa");
        }
    }
}
