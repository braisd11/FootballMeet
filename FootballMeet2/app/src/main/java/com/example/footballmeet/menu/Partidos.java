package com.example.footballmeet.menu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.example.footballmeet.partidos.Partido;
import com.example.footballmeet.partidos.crearPartido;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Partidos extends AppCompatActivity {

    private ListView listViewPartidos;
    private TextView textViewNoData, textView_fechaSeleccionada;
    private DatabaseReference databaseReference;
    private FirebaseListAdapter<Partido> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partidos);

        listViewPartidos = findViewById(R.id.custom_listView);
        textViewNoData = findViewById(R.id.textViewNoData);
        textView_fechaSeleccionada = findViewById(R.id.textView_fechaSeleccionada);

        // Inicializar Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Partidos");

        FirebaseListOptions<Partido> options = getPartidoFirebaseListOptions();

        // Inicializar el adaptador con las opciones configuradas
        adapter = new FirebaseListAdapter<Partido>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull Partido model, int position) {
                // Enlazar los datos del modelo a las vistas en el layout personalizado
                TextView titleTextView = v.findViewById(R.id.item_title);
                TextView descriptionTextView = v.findViewById(R.id.item_description);
                TextView fechaTextView = v.findViewById(R.id.item_fecha);
                TextView horaTextView = v.findViewById(R.id.item_hora);

                titleTextView.setText(model.getUserId());
                descriptionTextView.setText(model.getDescripcion());
                fechaTextView.setText(model.getFecha());
                horaTextView.setText(model.getHora());
            }
        };

        // Configurar el adaptador en la ListView
        listViewPartidos.setAdapter(adapter);

        // Agregar el listener para mostrar el mensaje de "No hay Partidos disponibles"
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listViewPartidos.setVisibility(View.VISIBLE);
                    textViewNoData.setVisibility(View.GONE);
                } else {
                    listViewPartidos.setVisibility(View.GONE);
                    textViewNoData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    private FirebaseListOptions<Partido> getPartidoFirebaseListOptions() {
        // Configurar opciones para el adaptador FirebaseListAdapter
        FirebaseListOptions<Partido> options = new FirebaseListOptions.Builder<Partido>()
                .setQuery(databaseReference, Partido.class)
                .setLayout(R.layout.custom_listview)
                .build();
        return options;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_partidos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newMatch:
                Intent intent = new Intent(this, crearPartido.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickBtnPartidos(View view) {

        switch (view.getId()){
            case R.id.fecha_partidos:
                mostrarDatePicker();
                break;

        }

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
                textView_fechaSeleccionada.setText(fechaSeleccionada.toString());

                // Aquí actualizas la consulta con la nueva fecha seleccionada
                Query newQuery = databaseReference.orderByChild("fecha").equalTo(fechaSeleccionada);
                adapter.notifyDataSetChanged();
            }
        }, year, month, day);

        datePickerDialog.show();
    }


}
