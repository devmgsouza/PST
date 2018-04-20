package meupet.android.soasd.com.br.meupet.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.mercadopago.constants.PaymentMethods;
import com.mercadopago.constants.Sites;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.lite.constants.PaymentTypes;
import com.mercadopago.model.Address;
import com.mercadopago.model.Item;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.Phone;
import com.mercadopago.model.Token;
import com.mercadopago.preferences.CheckoutPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.com.soasd.meupet.Checkout;
import br.com.soasd.meupet.ItemVenda;
import br.com.soasd.meupet.Produto;
import br.com.soasd.meupet.Usuario;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.BuscarCEP;
import meupet.android.soasd.com.br.meupet.httpservices.GetSingleMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PostShopMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PrivateGetMethod;
import meupet.android.soasd.com.br.meupet.utils.CartModel;

public class CheckoutActivity extends AppCompatActivity {
    @BindView(R.id.editTextCepCheckout)
    TextView editTextCepCheckout;
    @BindView(R.id.textViewEndereco)
    TextView textViewEndereco;
    @BindView(R.id.editTextComplemento)
    EditText editTextComplemento;
    @BindView(R.id.textViewValorFinal)
    TextView textViewValorTotal;
    @BindView(R.id.imageButtonMp)
    ImageButton imageButtonMp;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private List<CartModel> lista;
    private List<Produto> produtos;
    private List<Item> itens = new ArrayList<>();
    private double vFrete = 0;
    private double valor_final = 0;
    private String idUser;
    private String cep;
    private Usuario user = null;
    private JSONObject paymentObject;
    private List<ItemVenda> listaItens = new ArrayList<>();
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private JSONArray itemArrayJson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Finalizar compra");

        }

        Bundle extras = getIntent().getExtras();

        int login_mode = extras.getInt("LOGIN");
        cep = extras.getString("CEP");
        vFrete = extras.getDouble("VALOR_FRETE");


        editTextCepCheckout.setText(cep);
        loadAddress(cep);

        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        loadItens();



        imageButtonMp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = editTextComplemento.getText().toString();
                String addr = textViewEndereco.getText().toString();
                if (value.equals("") || value.equals(null) || value.isEmpty()
                        || addr.equals("") || addr.equals(null) || addr.isEmpty()) {
                    showtAutoDismissDialog("Necessário preencher os dados complementares");
                } else {



                    new MercadoPagoCheckout.Builder()
                            .setActivity(CheckoutActivity.this)
                            .setPublicKey("APP_USR-6e38655d-969a-48d4-8bf1-059e4231e2d8")
                            .setCheckoutPreference(getCheckoutPreference())
                            .startForPaymentData();
                }
            }


        });
    }

    private void loadItens() {
        lista = new LocalDatabase(this).selectItens();
        if (lista != null){
            produtos = new ArrayList<>();
            double vt = 0;
            for (int i = 0; i < lista.size(); i++){
                ItemVenda itemVenda = new ItemVenda();
                String gson = lista.get(i).getP();
                Produto p = new Gson().fromJson(gson, Produto.class);
                vt += p.getValor_venda();
                produtos.add(p);
                itemVenda.setFk_produto(p.getPk_produto());
                itemVenda.setQtd(1);
                itemVenda.setText_produto(p.getText_produto() + " " + p.getText_cor() + " " + p.getText_tamanho());
                itemVenda.setValor_total(p.getValor_venda());
                itemVenda.setText_descricao(lista.get(i).getCodigo_validador());
                listaItens.add(itemVenda);

            }
            valor_final = vt + vFrete;
            textViewValorTotal.setText("R$" + String.format("%.2f", valor_final));
        }
        String metodo = "/SP_BUSCAR_USUARIO?id=" + idUser;
        new GetSingleMethod(metodo, this, new GetSingleMethod.Callback() {
            @Override
            public void run(String result) {
                user = new Gson().fromJson(result, Usuario.class);
            }
        }).execute();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private com.mercadopago.preferences.CheckoutPreference getCheckoutPreference(){
        itens.clear();
        BigDecimal freteBig = new BigDecimal(String.format("%.2f", vFrete).replace(",","."));
        //BigDecimal freteBig = new BigDecimal(0);
        Item frete = new Item("Frete - PAC - Correios",
                1,
                freteBig);
        frete.setCategoryId("others");
        frete.setId("000");
        frete.setTitle("Frete");
        itens.add(frete);


        for (int i = 0; i < lista.size(); i++){
            Produto p = new Gson().fromJson(lista.get(i).getP(), Produto.class);
            Item item = new Item(
                    p.getText_produto()+ " " + p.getText_tamanho() + " " + p.getText_cor(),
                    lista.get(i).getQtd(),
                    new BigDecimal(String.format("%.2f", p.getValor_venda()).replace(",","."))
            );
            item.setId(p.getCodigo_produto());
            item.setTitle(p.getText_produto());
            item.setCategoryId("others");
            itens.add(item);


        }


        String result;
        try {
            result = new PrivateGetMethod("/DATETIME", this).execute().get();
        } catch (InterruptedException e) {
            result = System.currentTimeMillis() + "";
        } catch (ExecutionException e) {
            result = System.currentTimeMillis() + "";
        }


        Long time = Long.parseLong(result);
        time += 48*60*60*1000;
        Date maxDate = new Date(time);


        CheckoutPreference c = new  CheckoutPreference.Builder()
                .addItems(itens)
                .setExpirationDate(maxDate)
                .addExcludedPaymentType(PaymentTypes.TICKET)
                .setSite(Sites.BRASIL)
                .setMaxInstallments(3) //Limit the amount of installments
                .build();




        return c;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MercadoPagoCheckout.CHECKOUT_REQUEST_CODE) {
            if (resultCode == MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE) {
                PaymentData paymentData = com.mercadopago.util.JsonUtil.getInstance().fromJson(data.getStringExtra("paymentData"), PaymentData.class);



                paymentObject = new JSONObject();
                try {

                    BigDecimal amout = new BigDecimal(String.format("%.2f", paymentData.getTransactionAmount()).replace(",","."));
                    paymentObject.put("transaction_amount", amout);
                    paymentObject.put("token", paymentData.getToken().getId());
                    paymentObject.put("description", "Compras no aplicativo PET Smart Tag");
                    paymentObject.put("installments", paymentData.getPayerCost().getInstallments());
                    paymentObject.put("payment_method_id", paymentData.getPaymentMethod().getId());
                    paymentObject.put("capture", true);
                    paymentObject.put("statement_descriptor", "PST_APP");
                    paymentObject.put("notification_url", "https://ec2-18-231-108-89.sa-east-1.compute.amazonaws.com:8443/PST/rest/resources/whook");
                    paymentObject.put("issuer_id", paymentData.getIssuer().getId());

                    JSONObject payerInfo = new JSONObject();
                    payerInfo.put("email", user.getText_email());
                    payerInfo.put("type", "customer");

                    JSONObject order = new JSONObject();
                    order.put("type", "mercadopago");
                    order.put("id", System.currentTimeMillis());
                   // paymentObject.put("order", order);
                    paymentObject.put("payer", payerInfo);

                    paymentObject.put("external_reference", user.getPk_usuario());



                    /** ADITIONAL INFO**/
                    JSONObject aditionalInfo = new JSONObject();

                    JSONObject p = new JSONObject();
                    p.put("first_name", user.getText_nome());
                    p.put("last_name", user.getText_sobrenome());


                    JSONObject phone = new JSONObject();
                    if (user.getText_fone() != null) {
                        if (user.getText_fone().length() >= 10){
                            phone.put("area_code", user.getText_fone().substring(0,2));
                            phone.put("number", user.getText_fone().substring(2));
                        }
                    }
                    p.put("phone", phone);


                    p.put("registration_date", df.format(new Date()));
                    JSONObject shipment = new JSONObject();
                    JSONObject receiver = new JSONObject();
                    JSONObject address = new JSONObject();
                    if (user.getText_endereco() != null) {
                        if (user.getText_endereco().length() > 2) {
                            address.put("street_name", user.getText_endereco());
                            address.put("zip_code", cep);
                            receiver.put("zip_code", cep);
                            receiver.put("street_name", user.getText_endereco());
                        }
                    }
                    shipment.put("receiver_address", receiver);

                    p.put("address", address);


                    /** LISTA DE ITENS **/
                    itemArrayJson = new JSONArray();
                    for (int i = 0; i < itens.size(); i++) {
                        JSONObject item = new JSONObject();
                        item.put("id", itens.get(i).getId());
                        item.put("title", itens.get(i).getTitle());
                        item.put("description", itens.get(i).getDescription());
                        item.put("category_id", itens.get(i).getCategoryId());
                        item.put("quantity", itens.get(i).getQuantity());
                        item.put("unit_price", itens.get(i).getUnitPrice());
                        itemArrayJson.put(item);
                    }


                    aditionalInfo.put("ip_address", getIPAddress(true));
                    aditionalInfo.put("items", itemArrayJson);
                    aditionalInfo.put("payer", p);
                    aditionalInfo.put("shipments", shipment);



                    paymentObject.put("additional_info", aditionalInfo);



                    setCheckout();

                } catch (JSONException e) {

                }


            } else if (resultCode == RESULT_CANCELED) {
                if (data != null && data.getStringExtra("mercadoPagoError") != null) {

                } else {

                }
            }
        }
    }

    public void loadAddress(String cep){
        new BuscarCEP(cep, this, new BuscarCEP.Callback() {
            @Override
            public void run(String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String address = jsonObject.getString("logradouro");
                    address += ", " + jsonObject.getString("bairro");
                    address += ", " + jsonObject.getString("localidade");
                    address += "-" + jsonObject.getString("uf");
                    textViewEndereco.setText(address);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).execute();
    }

    private void showtAutoDismissDialog(String mensagem){
        TextView msg = new TextView(this);
        msg.setText("\n" + mensagem + "\n");
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ico_carimbo)
                .setTitle(this.getResources().getString(R.string.app_name))

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

        handler.postDelayed(runnable, 2500);
    }

    private void setCheckout() throws JSONException {
        String endereco = textViewEndereco.getText().toString() + ", " + editTextComplemento.getText();
        JSONObject checkout = new JSONObject();

        checkout.put("endereco", endereco);
        checkout.put("valor_final",valor_final);
        checkout.put("cep",cep);
        checkout.put("id", idUser);


        JSONObject dataToSend = new JSONObject();
        dataToSend.put("payment", paymentObject);
        dataToSend.put("checkout", checkout);
        dataToSend.put("itens", new Gson().toJson(listaItens));



        String metodo = "/SP_REGISTRAR_CHECKOUT";

        new PostShopMethod(dataToSend.toString(), metodo, CheckoutActivity.this, new PostShopMethod.Callback() {
            @Override
            public void run(String result) {
                    if (result.equals("Pronto, o seu pagamento foi aprovado!")) {
                        new LocalDatabase(CheckoutActivity.this).clearCart();
                        result += "\nVocê receberá um e-mail contendo" +
                                " os detalhes da compra! Você também poderá acompanhar o Status da Compra no menu de Configurações!"
                                +"\nAgradecemos a sua confiança";
                    }
                    AlertDialog.Builder dialog = new AlertDialog.Builder(CheckoutActivity.this)
                            .setTitle(getResources().getString(R.string.app_name))
                            .setMessage(result)
                            .setNeutralButton(getResources().getString(R.string.continue_to),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            setResult(901);
                                            finish();
                                        }
                                    });
                    dialog.show();


            }

        }, "Realizando pagamento...").execute();



    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }




}
