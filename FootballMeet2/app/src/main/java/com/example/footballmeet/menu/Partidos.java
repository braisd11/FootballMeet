package com.example.footballmeet.menu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.example.footballmeet.partidos.Partido;
import com.example.footballmeet.partidos.PartidoAdapter;
import com.example.footballmeet.partidos.crearPartido;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Partidos extends AppCompatActivity {

    private ListView listViewPartidos;
    private TextView textViewNoData, textView_fechaDesde, textView_fechaHasta;
    private DatabaseReference databaseReference;
    private PartidoAdapter adapter;
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

        // Inicializa la referencia a la base de datos de Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Partidos");

        // Inicializa el adaptador con una lista vacía de partidos
        adapter = new PartidoAdapter(this, R.layout.custom_listview, new ArrayList<Partido>());
        listViewPartidos.setAdapter(adapter);

        // Escucha los cambios en los datos de Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Partido partido = dataSnapshot.getValue(Partido.class);
                    adapter.add(partido);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        listViewPartidos.setAdapter(adapter);
        registerForContextMenu(listViewPartidos);
    }

    private void listenerSwicth() {
        switchFecha_partidos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    ly_Fechas.setVisibility(View.VISIBLE);
                } else {
                    textView_fechaDesde.setText("");
                    textView_fechaHasta.setText("");
                    ly_Fechas.setVisibility(View.GONE);
                    actualizarFechas("","");
                }
            }
        });
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_anotarse:
                anotarseAlPartido(info.position);
                return true;
            case R.id.menu_favorito:
                anhadirAFavoritos(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void anotarseAlPartido(int position) {
        MainActivity.showToast(this,"Te has anotado al partido");
    }

    private void anhadirAFavoritos(int position) {
        MainActivity.showToast(this,"Partido añadido a favoritos");
    }

    public void onClickBtnPartidos(View view) {
        switch (view.getId()) {
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

                if (tipo == 1) {
                    textView_fechaDesde.setText(fechaSeleccionada);
                } else if (tipo == 2) {
                    textView_fechaHasta.setText(fechaSeleccionada);
                }

                actualizarFechas(textView_fechaDesde.getText().toString(), textView_fechaHasta.getText().toString());
            }
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void actualizarFechas(String desde, String hasta) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Partidos");

        long fechaDesde = convertStringToDateInMillis(desde);

        if (hasta.isEmpty()) {
            // Si no se proporciona una fecha de fin, buscar hasta el último partido registrado
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Partido> listaPartidos = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Partido partido = snapshot.getValue(Partido.class);

                        long fechaPartido = convertStringToDateInMillis(partido.getFecha());

                        // Comparar con la fecha desde
                        if (fechaPartido >= fechaDesde) {
                            listaPartidos.add(partido);
                        }
                    }

                    adapter.clear();
                    adapter.addAll(listaPartidos);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        } else {
            long fechaHasta = convertStringToDateInMillis(hasta);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Partido> listaPartidos = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Partido partido = snapshot.getValue(Partido.class);

                        // Convertir la fecha del partido a milisegundos
                        long fechaPartido = convertStringToDateInMillis(partido.getFecha());

                        // Comparar con la fecha desde y hasta
                        if (fechaPartido >= fechaDesde && fechaPartido <= fechaHasta) {
                            listaPartidos.add(partido);
                        }
                    }

                    adapter.clear();
                    adapter.addAll(listaPartidos);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }

        if (!switchFecha_partidos.isChecked()){
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Partido> listaPartidos = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Partido partido = snapshot.getValue(Partido.class);
                        listaPartidos.add(partido);
                    }

                    adapter.clear();
                    adapter.addAll(listaPartidos);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    private void procesarPartidos(DataSnapshot dataSnapshot) {
        List<Partido> listaPartidos = new ArrayList<>();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String partidoId = snapshot.child("partidoId").getValue(String.class);
            String userId = snapshot.child("userId").getValue(String.class);
            String fecha = snapshot.child("fecha").getValue(String.class);
            String hora = snapshot.child("hora").getValue(String.class);
            String descripcion = snapshot.child("descripcion").getValue(String.class);
            int capacidad = snapshot.child("capacidad").getValue(Integer.class);
            double precio = snapshot.child("precio").getValue(Double.class);
            String imagenUrl = snapshot.child("imagenUrl").getValue(String.class);
            String ubicacion = snapshot.child("ubicacion").getValue(String.class);

            Partido partido = new Partido(partidoId, userId, fecha, hora, descripcion, capacidad, precio, imagenUrl, ubicacion);

            listaPartidos.add(partido);
        }

        adapter.clear();
        adapter.addAll(listaPartidos);
        adapter.notifyDataSetChanged();
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
