package meupet.android.soasd.com.br.meupet.fragments;


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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import br.com.soasd.meupet.Checkout;
import br.com.soasd.meupet.ItemVenda;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.adapter.AdapterItemVenda;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemVendaFragment extends Fragment {
    @BindView(R.id.recyclerViewItemVenda)
    RecyclerView recyclerView;

    private double vTotal;
    private int checkoutId;
    private List<ItemVenda> lista;
    public ItemVendaFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itemvenda, container, false);
        ButterKnife.bind(this, view);




        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        checkoutId = getArguments().getInt("REGISTRO_VENDA");
        vTotal = getArguments().getDouble("VALOR_TOTAL");
        ((GeneralActivity)getActivity()).updateToolbarTitle("Pedido " + checkoutId + " | R$ " + String.format("%.2f", vTotal));
        loadItens(checkoutId - 15000);
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


    private void loadItens(int chId){
        String metodo = "/SP_BUSCAR_ITEMVENDA?checkout=" + chId;
        new GetMethod(metodo, getContext(), new GetMethod.Callback() {
            @Override
            public void run(String result) {
                Type listType = new TypeToken<ArrayList<ItemVenda>>(){}.getType();
                lista = new Gson().fromJson(result, listType);

                if (lista != null) {
                    if (lista.size() > 0) {
                        recyclerView.setAdapter(new AdapterItemVenda(getContext(), lista, null));
                    }
                }  else {


                    }
            }
        }, "Carregando itens...").execute();

    }

}
