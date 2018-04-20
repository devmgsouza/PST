package meupet.android.soasd.com.br.meupet.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.soasd.meupet.ItemVenda;
import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by SOA - Development on 23/03/2018.
 */

public class AdapterItemVenda extends RecyclerView.Adapter<AdapterItemVenda.ItemVendaHolderView>{

    private List<ItemVenda> lista;
    private Context context;
    private ItemVendaOnClickListener itemVendaOnClickListener;

    public AdapterItemVenda(Context context, List<ItemVenda> lista, ItemVendaOnClickListener itemVendaOnClickListener){
        this.context = context;
        this.lista = lista;
        this.itemVendaOnClickListener = itemVendaOnClickListener;
        
    }

    @NonNull
    @Override
    public ItemVendaHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_itemvenda, parent, false);
        ItemVendaHolderView holder = new ItemVendaHolderView(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemVendaHolderView holder, int position) {
        ItemVenda item = lista.get(position);

        holder.textViewItem.setText(item.getText_produto());
        holder.textViewValorItemVenda.setText("" + String.format("%.2f", item.getValor_total()));

        
    }

    @Override
    public int getItemCount() {
        return this.lista != null ? this.lista.size():0;
    }

    public interface ItemVendaOnClickListener {
        void onClick(View view, int index);

    }


    public static class ItemVendaHolderView extends RecyclerView.ViewHolder {

        public TextView textViewItem;
        public TextView textViewValorItemVenda;

        public ItemVendaHolderView(View itemView) {
            super(itemView);

            textViewItem = itemView.findViewById(R.id.textViewItem);
            textViewValorItemVenda = itemView.findViewById(R.id.textViewValorItemVenda);

        }
    }
}
