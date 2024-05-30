package com.example.footballmeet.menu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footballmeet.MainActivity;
import com.example.footballmeet.R;
import com.example.footballmeet.databinding.ActivityMainBinding;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Partidos extends AppCompatActivity {

    private ListView listViewPartidos;
    private TextView textViewNoData, textView_fechaDesde, textView_fechaHasta;
    private DatabaseReference databaseReference;
    private FirebaseListAdapter<Partido> adapter;
    private LinearLayout ly_Fechas;
    private Switch switchFecha_partidos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partidos);

        listViewPartidos = findViewById(R.id.custom_listView);
        textViewNoData = findViewById(R.id.textViewNoData);
        ly_Fechas = findViewById(R.id.ly_Fechas);
        switchFecha_partidos = findViewById(R.id.switchFecha_partidos);
        textView_fechaDesde = findViewById(R.id.textView_fechaDesde);
        textView_fechaHasta = findViewById(R.id.textView_fechaHasta);

        fechaActual();

        listenerSwicth();

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



    private void listenerSwicth() {
        switchFecha_partidos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    ly_Fechas.setVisibility(View.VISIBLE);
                } else {
                    textView_fechaDesde.setText("");
                    textView_fechaHasta.setText("");
                    ly_Fechas.setVisibility(View.GONE);
                }
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
            case R.id.fecha_desde:

                mostrarDatePicker(1);
                break;

            case R.id.fecha_hasta:

                mostrarDatePicker(2);
                break;

        }

    }

    private void mostrarDatePicker(int tipo) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++; // Month is 0-based in Calendar
                String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, month, year);

                if (tipo == 1){
                    textView_fechaDesde.setText(fechaSeleccionada);
                } else if (tipo == 2){
                    textView_fechaHasta.setText(fechaSeleccionada);
                }

                actualizarFechas(textView_fechaDesde.toString(), textView_fechaHasta.toString());

            }
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void actualizarFechas(String desde, String hasta) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("partidos");

        long fechaDesde = convertStringToDateInMillis(desde);

        if (hasta == "" || hasta.isEmpty()) {
            // Si no se proporciona una fecha de fin, buscar hasta el último partido registrado
            ref.orderByChild("timestamp")
                    .startAt(fechaDesde)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            procesarPartidos(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
            return;
        }

        long fechaHasta = convertStringToDateInMillis(hasta);

        // Si se proporcionan ambas fechas, buscar partidos entre esas fechas
        ref.orderByChild("timestamp")
                .startAt(fechaDesde)
                .endAt(fechaHasta)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        procesarPartidos(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void procesarPartidos(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            // Procesa cada partido
        }
    }


    private void fechaActual() {
        Date myDate = new Date();

        textView_fechaDesde.setText(new SimpleDateFormat("dd/MM/yyyy").format(myDate));
    }


    /**
     * Convierte una fecha en String a milisegundos desde la época.
     *
     * @param dateString La fecha en formato String.
     * @return El valor en milisegundos desde la época, o -1 si ocurre un error de parseo.
     */
    public static long convertStringToDateInMillis(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
