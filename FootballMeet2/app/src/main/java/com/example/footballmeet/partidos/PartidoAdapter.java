package com.example.footballmeet.partidos;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.footballmeet.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;

public class PartidoAdapter extends FirebaseListAdapter<Partido> {

    public PartidoAdapter(@NonNull FirebaseListOptions<Partido> options) {
        super(options);
    }

    @Override
    protected void populateView(View v, Partido model, int position) {
        ImageView imageView = v.findViewById(R.id.item_image);
        TextView titleTextView = v.findViewById(R.id.item_title);
        TextView descriptionTextView = v.findViewById(R.id.item_description);
        TextView fechaTextView = v.findViewById(R.id.item_fecha);
        TextView horaTextView = v.findViewById(R.id.item_hora);

        titleTextView.setText(model.getUserId());
        descriptionTextView.setText(model.getDescripcion());
        fechaTextView.setText(model.getFecha());
        horaTextView.setText(model.getHora());
        Glide.with(v.getContext()).load(model.getImagenUrl()).into(imageView);
    }
}
