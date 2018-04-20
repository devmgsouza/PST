package meupet.android.soasd.com.br.meupet.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import br.com.soasd.meupet.Pet;
import br.com.soasd.meupet.Produto;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.adapter.AdapterImageItemShop;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.utils.CirclePagerIndicatorDecoration;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemShopFragment extends Fragment {
    @BindView(R.id.textViewProduto)
    TextView textViewProduto;
    @BindView(R.id.textViewTamanho)
    TextView textViewTamanho;
    @BindView(R.id.textViewCor)
    TextView textViewCor;
    @BindView(R.id.textViewValor)
    TextView textViewValor;
    @BindView(R.id.textButtonAddCart)
    TextView textButtonAddCart;
    @BindView(R.id.textViewDescription)
    TextView textViewDescription;
    @BindView(R.id.viewPagerImgProduto)
    RecyclerView recyclerView;





    final List<Uri> result = new ArrayList<>();
    private Produto produto;
    static String itemShop;
    public ItemShopFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itemshop, container, false);
        ButterKnife.bind(this, view);


        itemShop = getArguments().getString("ITEM_SHOP");
        produto = new Gson().fromJson(itemShop, Produto.class);
        loadItem(produto);

        ((GeneralActivity)getActivity()).updateToolbarTitle("Shop");
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

       // recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);



        textButtonAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<Pet> lista = new LocalDatabase(getContext()).selectPets();


                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle(getResources().getString(R.string.select_pet));
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.select_dialog_singlechoice);
                for (int i = 0; i < lista.size(); i++) {
                    arrayAdapter.add(lista.get(i).getText_nome());
                }

                dialog.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        String codigo_pet = lista.get(which).getCodigo_validador();

                        String gson = new Gson().toJson(produto);
                        new LocalDatabase(getContext()).addItemCart(gson, 1, codigo_pet);

                        showtAutoDismissDialog("Item adicionado ao carrinho");
                    }
                });

                dialog.show();


            }
        });




        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();

                break;

        }
        return super.onOptionsItemSelected(item);
    }



    private void loadItem(Produto p){

        textViewProduto.setText(p.getText_produto());
        textViewTamanho.setText(p.getText_tamanho());
        textViewCor.setText(p.getText_cor());
        textViewValor.setText(String.format("%.2f",p.getValor_venda()));
        textViewDescription.setText(p.getText_descricao());

        loadImage(p.getUrl_foto1(), p.getUrl_foto2(), p.getUrl_foto3());

    }


    private void loadImage(String url1, String url2, String url3) {
        List<String> lista = new ArrayList<>();

        if (url1.length() > 10) {
            lista.add(url1);
        } else {
            lista.add("#NULL");
        }

        if (url2.length() > 10) {
            lista.add(url2);
        } else {
            lista.add("#NULL");
        }

        if (url3.length() > 10) {
            lista.add(url3);
        } else {
            lista.add("#NULL");
        }

        AdapterImageItemShop adapter = new AdapterImageItemShop(getContext(), lista, onClickListener());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new CirclePagerIndicatorDecoration());


    }



    private AdapterImageItemShop.ImageOnClickListener onClickListener(){
        return new AdapterImageItemShop.ImageOnClickListener() {
            @Override
            public void onClick(View view, int index) {

            }
        };
    }

    private void showtAutoDismissDialog(String mensagem){
        TextView msg = new TextView(getContext());
        msg.setText("\n" + mensagem + "\n");
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ico_carimbo)
                .setTitle(R.string.app_name)

                .setView(msg);

        final AlertDialog alert = dialog.create();
        alert.show();




        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alert.isShowing()) {
                    alert.dismiss();
                    getActivity().finish();
                }
            }
        };

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 1500);
    }


}
