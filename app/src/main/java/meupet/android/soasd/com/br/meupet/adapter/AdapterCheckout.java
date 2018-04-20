package meupet.android.soasd.com.br.meupet.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.soasd.meupet.Checkout;
import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by SOA - Development on 23/03/2018.
 */

public class AdapterCheckout extends RecyclerView.Adapter<AdapterCheckout.CheckouthHolderView>{
    Context context;
    List<Checkout> lista;
    CheckoutOnClickListener checkoutOnClickListener;


    public AdapterCheckout (Context context, List<Checkout> lista, CheckoutOnClickListener checkoutOnClickListener){
        this.context = context;
        this.lista = lista;
        this.checkoutOnClickListener = checkoutOnClickListener;
    }

    @NonNull
    @Override
    public CheckouthHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_checkout, parent, false);
        CheckouthHolderView holder = new CheckouthHolderView(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CheckouthHolderView holder, final int position) {
        Checkout c = lista.get(position);


        holder.textViewStatus.setText(c.getText_status());
        holder.textViewDataCompra.setText(c.getData_inicio());
        holder.textViewRegistroCompra.setText(c.getPk_checkout() + "");





        if (checkoutOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkoutOnClickListener.onClick(holder.itemView , position);
                }
            });

            holder.textViewButtonVer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkoutOnClickListener.onClickVer(holder.textViewButtonVer, position);
                }
            });
        }






    }

    @Override
    public int getItemCount() {
        return this.lista != null ? this.lista.size():0;
    }

    public interface CheckoutOnClickListener {
        void onClick(View view, int index);
        void onClickVer(View view, int index);
    }


    public static class CheckouthHolderView extends RecyclerView.ViewHolder {

        public TextView textViewStatus;
        public TextView textViewRegistroCompra;
        public TextView textViewDataCompra;
        public TextView textViewButtonVer;


        public CheckouthHolderView(View view) {
            super(view);

            textViewRegistroCompra = view.findViewById(R.id.textViewRegistroCompra);
            textViewStatus = view.findViewById(R.id.textViewStatus);
            textViewDataCompra = view.findViewById(R.id.textViewDataCompra);
            textViewButtonVer = view.findViewById(R.id.textViewButtonVer);

        }
    }
}
