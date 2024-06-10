package com.example.footballmeet.partidos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.footballmeet.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PartidoAdapter extends ArrayAdapter<Partido> {

    private Context mContext;
    private int mResource;


    public PartidoAdapter(Context context, int resource, List<Partido> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Partido partido = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
        }

        TextView title = convertView.findViewById(R.id.item_title);
        TextView description = convertView.findViewById(R.id.item_description);
        TextView fecha = convertView.findViewById(R.id.item_fecha);
        TextView hora = convertView.findViewById(R.id.item_hora);
        ImageView image = convertView.findViewById(R.id.item_image);

        title.setText(partido.getDescripcion());
        description.setText("Capacidad: " + partido.getCapacidad() + "\nPrecio: " + partido.getPrecio() + " €/persona");
        fecha.setText(partido.getFecha());
        hora.setText(partido.getHora());

        if (partido.getImagenUrl() != null && !partido.getImagenUrl().isEmpty()) {
            Picasso.get().load(partido.getImagenUrl()).into(image);
        } else {
            image.setImageResource(R.drawable.placeholder_image); // Imagen de placeholder si no hay URL
        }

        return convertView;
    }
}
