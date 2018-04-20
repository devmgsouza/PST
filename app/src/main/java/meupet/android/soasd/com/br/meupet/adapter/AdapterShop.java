package meupet.android.soasd.com.br.meupet.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import br.com.soasd.meupet.Produto;
import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by SOA - Development on 17/02/2018.
 */

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.ShopHolderView>{
    private Context context;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private List<Produto> produtos;
    private ShopOnClickListener shopOnClickListener;
    private int shop_var;



    public AdapterShop(Context context, List<Produto> produtos, ShopOnClickListener shopOnClickListener,
                       int shop_var){
        this.context = context;
        this.produtos = produtos;
        this.shopOnClickListener = shopOnClickListener;
        this.shop_var = shop_var;
    }

    @Override
    public ShopHolderView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_itemshop, parent, false);
        ShopHolderView holder = new ShopHolderView(view);
        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();

        return holder;
    }

    @Override
    public void onBindViewHolder(final ShopHolderView holder, final int position) {
            final Produto p = produtos.get(position);

    holder.progressBar.setVisibility(View.VISIBLE);

        holder.textNomeProduto.setText(p.getText_produto() + " \nCor:" + p.getText_cor() +
                " \nTamanho: " + p.getText_tamanho());



        holder.textViewValor.setText(String.format("%.2f",p.getValor_venda()));
        if (p.getUrl_foto1() != null) {

            if (!p.getUrl_foto1().equals("")) {

                StorageReference pathReference = storageRef.child(p.getUrl_foto1());
                pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                    @Override
                    public void onSuccess(Uri uri) {
                        holder.cardImageViewProduto.setImageURI(uri);
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.cardImageViewProduto.setActualImageResource(R.drawable.ic_presente);
                        holder.cardImageViewProduto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                });

            } else {
                holder.cardImageViewProduto.setActualImageResource(R.drawable.ic_presente);
            }

        } else {
            holder.cardImageViewProduto.setActualImageResource(R.drawable.ic_presente);
        }

        if (shop_var == 1) {
            holder.buttonAction.setText("Comprar");
        } else {
            holder.buttonAction.setText("Remover");
        }


        holder.progressBar.setVisibility(View.GONE);

        if (shopOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shopOnClickListener.onClick(holder.itemView, position);
                }
            });
            holder.buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shopOnClickListener.onClickButton(holder.buttonAction, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {

        return this.produtos != null ? this.produtos.size():0;
    }





    public interface ShopOnClickListener {
        void onClick(View view, int index);
        void onClickButton(View view, int index);
    }

    public static class ShopHolderView extends RecyclerView.ViewHolder {
        public SimpleDraweeView cardImageViewProduto;
        public TextView textNomeProduto;
        public TextView textViewValor;
        public Button buttonAction;
        public ProgressBar progressBar;




        public ShopHolderView(View view) {
            super(view);
            cardImageViewProduto = view.findViewById(R.id.ImageViewItemVenda);
            textNomeProduto = view.findViewById(R.id.textViewItem);
            textViewValor = view.findViewById(R.id.cardTextViewValor);
            buttonAction = view.findViewById(R.id.cardButtonAction);
            progressBar = view.findViewById(R.id.progressBarShop);


        }
    }



}
