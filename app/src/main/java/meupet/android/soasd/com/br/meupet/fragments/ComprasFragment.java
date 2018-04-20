package meupet.android.soasd.com.br.meupet.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import br.com.soasd.meupet.Checkout;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.adapter.AdapterCheckout;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;
import meupet.android.soasd.com.br.meupet.httpservices.GetShopMethod;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComprasFragment extends Fragment {
    @BindView(R.id.recyclerViewCompras)
    RecyclerView recyclerViewCompras;
    @BindView(R.id.imageViewBackgroundCompras)
    ImageView imageViewBackgroundCompras;

    private int login_mode;
    private String idUser;
    private List<Checkout> checkoutList;
    public ComprasFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compras, container, false);
        ButterKnife.bind(this, view);


        ((GeneralActivity)getActivity()).updateToolbarTitle("Minhas compras");
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewCompras.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewCompras.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCompras.setHasFixedSize(true);

        login_mode = getArguments().getInt("LOGIN");
        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        loadItens();

        return view;
    }





    private void loadItens(){
        String metodo = "/SP_BUSCAR_CHECKOUT?id=" + idUser;
        new GetMethod(metodo, getContext(), new GetMethod.Callback() {
            @Override
            public void run(String result) {
                Type listType = new TypeToken<ArrayList<Checkout>>(){}.getType();
                checkoutList = new Gson().fromJson(result, listType);

                if (checkoutList != null) {

                        if (checkoutList.size() > 0) {
                            imageViewBackgroundCompras.setVisibility(View.GONE);
                            recyclerViewCompras.setVisibility(View.VISIBLE);


                            recyclerViewCompras.setAdapter(new AdapterCheckout(getContext(), checkoutList, onClick()));
                        } else {
                            imageViewBackgroundCompras.setVisibility(View.VISIBLE);
                            recyclerViewCompras.setVisibility(View.GONE);
                        }
                }  else {
                    imageViewBackgroundCompras.setVisibility(View.VISIBLE);
                    recyclerViewCompras.setVisibility(View.GONE);
                }
            }
        }, "Buscando registros...").execute();

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

    private AdapterCheckout.CheckoutOnClickListener onClick(){

        return new AdapterCheckout.CheckoutOnClickListener(){

            @Override
            public void onClick(View view, int index) {

                Intent i = new Intent(getContext(), GeneralActivity.class);
                i.putExtra("fragment", 1000);
                i.putExtra("REGISTRO_VENDA", checkoutList.get(index).getPk_checkout());
                i.putExtra("VALOR_TOTAL", checkoutList.get(index).getValor_total());
                startActivityForResult(i, 0);


            }

            @Override
            public void onClickVer(View view, int index) {
                Intent i = new Intent(getContext(), GeneralActivity.class);
                i.putExtra("fragment", 1000);
                i.putExtra("REGISTRO_VENDA", checkoutList.get(index).getPk_checkout());
                startActivityForResult(i, 0);
            }
        };
    }

}
