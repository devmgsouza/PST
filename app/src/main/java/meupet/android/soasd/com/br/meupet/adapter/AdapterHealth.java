package meupet.android.soasd.com.br.meupet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Random;

import br.com.soasd.meupet.Health;
import br.com.soasd.meupet.Produto;
import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by SOA - Development on 17/02/2018.
 */

public class AdapterHealth extends RecyclerView.Adapter<AdapterHealth.HealthHolderView>{
    private Context context;
    private List<Health> lista;
    private HealthOnClickListener healthOnClickListener;

    public AdapterHealth (Context context, List<Health> lista, HealthOnClickListener healthOnClickListener){
        this.context = context;
        this.lista = lista;
        this.healthOnClickListener = healthOnClickListener;
    }



    @NonNull
    @Override
    public HealthHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_health, parent, false);
        HealthHolderView holder = new HealthHolderView(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HealthHolderView holder, final int position) {
        Health h = lista.get(position);

        holder.textViewCuidadoDescricao.setText(h.getText_cuidado());
        holder.textViewAplicacao.setText(context.getResources().getString(R.string.data_aplicacao) + ": \n" + h.getData_aplicacao());
        if (h.getData_reaplicacao() != null){
            holder.textViewReaplicacao.setText(context.getResources().getString(R.string.data_reaplicacao)+": \n" + h.getData_reaplicacao());
        } else {
            holder.textViewReaplicacao.setVisibility(View.GONE);
        }

        char letter = h.getText_cuidado().charAt(0);
        int color = 0;
        switch (letter) {
            case 'V':
                color = Color.argb(255,97, 76, 159);
                break;
            case 'C':
                color = Color.argb(255, 255, 198, 68);
                break;
            case 'A':
                color = Color.argb(255, 235, 54, 68);
            break;
            case 'B':
                color = Color.argb(255, 144, 2, 81);
                break;
        }


        TextDrawable drawable = TextDrawable.builder().buildRound(String.valueOf(letter), color);
        holder.imageViewHealthLetter.setImageDrawable(drawable);



        if (healthOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    healthOnClickListener.onClick(holder.itemView, position);
                }
            });
            holder.imageButtonHealthMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    healthOnClickListener.onClickMore(holder.imageButtonHealthMore, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return this.lista != null ? this.lista.size():0;
    }


    public interface HealthOnClickListener {
        void onClick(View view, int index);
        void onClickMore(View view, int index);
    }

    public static class HealthHolderView extends RecyclerView.ViewHolder {
        public ImageView imageViewHealthLetter;
        public TextView textViewCuidadoDescricao;
        public TextView textViewAplicacao;
        public TextView textViewReaplicacao;
        public ImageButton imageButtonHealthMore;

        public HealthHolderView(View view) {
            super(view);

            imageViewHealthLetter = view.findViewById(R.id.imageViewHealthLetter);
            textViewCuidadoDescricao = view.findViewById(R.id.textViewCuidadoDescricao);
            textViewAplicacao = view.findViewById(R.id.textViewAplicacao);
            textViewReaplicacao = view.findViewById(R.id.textViewReaplicacao);
            imageButtonHealthMore = view.findViewById(R.id.imageButtonHealthMore);

        }

    }

}
