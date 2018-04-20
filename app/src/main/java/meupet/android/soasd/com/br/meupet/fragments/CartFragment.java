package meupet.android.soasd.com.br.meupet.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import com.mercadopago.constants.Sites;

import com.mercadopago.core.MercadoPagoCheckout;

import com.mercadopago.lite.constants.PaymentTypes;
import com.mercadopago.model.Item;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PaymentData;

import org.json.JSONException;
import org.json.JSONObject;


import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.List;


import br.com.soasd.meupet.Produto;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.CheckoutActivity;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.adapter.AdapterShop;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.BuscarCEP;
import meupet.android.soasd.com.br.meupet.httpservices.CorreiosWS;
import meupet.android.soasd.com.br.meupet.utils.CartModel;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;

import static android.app.Activity.RESULT_CANCELED;

/**
 * A simple {@link Fragment} subclass.
 */
public class CartFragment extends BaseFragment {
    @BindView(R.id.recyclerViewCartItens)
    RecyclerView recyclerViewCartItens;
    @BindView(R.id.imageViewBgCart)
    ImageView imageViewBgCart;
    @BindView(R.id.textButtonFinalizar)
    TextView textButtonFinalizar;
    @BindView(R.id.textViewValorTotal)
    TextView textViewValorTotal;
    @BindView(R.id.textButtonPesquisar)
    TextView textButtonPesquisar;
    @BindView(R.id.editTextConsultaCep)
    EditText editTextConsultaCep;

    static String cep = "#NULL";
    double vFrete = -1;
    String valor_frete;
    String prazo = "";
    private static String idUser;
    private List<CartModel> lista;
    private List<Produto> produtos;
    int login_mode;
    double vt;
    List<Item> itens = new ArrayList<>();
    public CartFragment() {

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
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        ButterKnife.bind(this, view);


        ((GeneralActivity)getActivity()).updateToolbarTitle("Meu carrinho");
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewCartItens.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewCartItens.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCartItens.setHasFixedSize(true);

         login_mode = getArguments().getInt("LOGIN");
        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        loadItens();



        textButtonFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (vFrete != -1) {
                    if (produtos.size() > 0) {

                        Intent i = new Intent(getContext(), CheckoutActivity.class);
                        i.putExtra("fragment", 900);
                        i.putExtra("LOGIN", login_mode);
                        i.putExtra("CEP", editTextConsultaCep.getText().toString());
                        i.putExtra("VALOR_FRETE", vFrete);
                        startActivityForResult(i, 0);
                    }else {
                        showtAutoDismissDialog("Seu carrinho  está vazio");

                        }
                } else {
                    showtAutoDismissDialog("Calculo o frete antes de finalizar a venda");
                }
            }
        });


        textButtonPesquisar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lista.size() > 0) {
                   final String cep = editTextConsultaCep.getText().toString();


                        if (cep.equals("_____-___")) {
                            showtAutoDismissDialog("Insira um CEP válido");

                        } else {

                            String peso = String.valueOf(produtos.size() * 0.1);

                            String metodo = "?sCepOrigem=18200085&sCepDestino=" + cep + "&nVlPeso=" + peso + "&nCdFormato=1" +
                                    "&nVlComprimento=16&nVlAltura=6&nVlLargura=11&nVlDiametro=0&sCdMaoPropria=n" +
                                    "&nValorDeclarado=150&sCdAvisoRecebimento=n&StrRetorno=xml&nCdServico=41106";
                            //cep = editTextConsultaCep.getText().toString();
                            CorreiosWS correiosWS = new CorreiosWS(metodo, getContext(), new CorreiosWS.Callback() {
                                @Override
                                public void run(String result) {
                                    XmlToJson xmlToJson = new XmlToJson.Builder(result).build();

                                    JSONObject object = xmlToJson.toJson();


                                    try {
                                        /** CALCULO DO FRETE **/

                                        vFrete = Double.parseDouble(object.getJSONObject("Servicos").getJSONObject("cServico").getString("Valor").replace(",", "."));
                                        valor_frete = "R$" + String.format("%.2f", vFrete);

                                        if (vt > 50 && vt < 120 && vFrete < 25) {
                                            vFrete = vFrete/2;
                                            valor_frete = "R$" + String.format("%.2f", vFrete);
                                        } else if (vt > 120 && vFrete < 25) {
                                            vFrete = 0;
                                            valor_frete = "Grátis";
                                        }





                                        prazo = object.getJSONObject("Servicos").getJSONObject("cServico").getString("PrazoEntrega");
                                        if (prazo.equals("0")) {
                                            showtAutoDismissDialog("Insira um CEP válido");

                                        } else {

                                            if (prazo.equals("1")) {
                                                prazo += " dia útil";
                                            } else {
                                                prazo += " dias úteis";
                                            }
                                            showtAutoDismissDialog("Valor do frete: " + valor_frete + "\nPrazo: " + prazo);

                                        }


                                        /** CALCULO FRETE **/





                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                }
                            }, "calculando...");
                            correiosWS.execute();


                        }

                } else{
                    showtAutoDismissDialog("Seu carrinho  está vazio");

                    cep = "#NULL";
                }

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


    private void loadItens(){
        lista = null;
        lista = new LocalDatabase(getContext()).selectItens();

        vt = 0;
        if (lista != null){

            if (lista.size() >= 0) {

                produtos = new ArrayList<>();
                for (int i = 0; i < lista.size(); i++){
                    String gson = lista.get(i).getP();
                    Produto p = new Gson().fromJson(gson, Produto.class);
                    vt += p.getValor_venda();
                    produtos.add(p);
                }


                imageViewBgCart.setVisibility(View.GONE);
                recyclerViewCartItens.setVisibility(View.VISIBLE);

                recyclerViewCartItens.setVisibility(View.VISIBLE);
                recyclerViewCartItens.setAdapter(new AdapterShop(getContext(), produtos, onClick(), 0));



                textViewValorTotal.setText(String.format("%.2f", vt));


            } else {
                imageViewBgCart.setVisibility(View.VISIBLE);
                recyclerViewCartItens.setVisibility(View.GONE);
                textViewValorTotal.setText(String.format("%.2f", vt));
            }
        } else {
            imageViewBgCart.setVisibility(View.VISIBLE);
            recyclerViewCartItens.setVisibility(View.GONE);
            textViewValorTotal.setText(String.format("%.2f", vt));
        }
    }


    private AdapterShop.ShopOnClickListener onClick() {
        return new AdapterShop.ShopOnClickListener() {
            @Override
            public void onClick(View view, int index) {

            }

            @Override
            public void onClickButton(View view, int index) {
                CartModel c = lista.get(index);

                new LocalDatabase(getContext()).deleteItem(c.getPk_cart());
                loadItens();
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == 901) {
                getActivity().finish();
            }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_limpar, menu);
        final MenuItem addItemMenu = menu.findItem(R.id.menu_clear);
        AppCompatButton buttonAdd = null;

        if (addItemMenu != null) {
            buttonAdd = (AppCompatButton) addItemMenu.getActionView();

            SettingsModel m = new LocalDatabase(getContext()).loadSettings();
            if (m!=null) {
                buttonAdd.setTextSize(m.getFonteSize());
            }



            buttonAdd.setText(getResources().getString(R.string.clear));

            buttonAdd.setBackgroundColor(Color.TRANSPARENT);
            buttonAdd.setAllCaps(false);
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new LocalDatabase(getContext()).clearCart();
                    loadItens();

                }
            });

        }

    }


    private void calcularFrete(JSONObject object) {

    }
}
