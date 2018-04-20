package meupet.android.soasd.com.br.meupet.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import br.com.soasd.meupet.Imagem;
import br.com.soasd.meupet.Pet;
import br.com.soasd.meupet.Produto;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.activities.MainActivity;
import meupet.android.soasd.com.br.meupet.adapter.AdapterImageItemShop;
import meupet.android.soasd.com.br.meupet.adapter.AdapterShop;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;
import meupet.android.soasd.com.br.meupet.httpservices.GetSingleMethod;
import meupet.android.soasd.com.br.meupet.utils.CartModel;
import meupet.android.soasd.com.br.meupet.utils.CirclePagerIndicatorDecoration;
import meupet.android.soasd.com.br.meupet.utils.LinePagerIndicatorDecoration;


public class MarketFragment extends BaseFragment{
    @BindView(R.id.recyclerViewMarket)
    RecyclerView recyclerViewMarket;
    @BindView(R.id.imageViewBackgroundoMarket)
    ImageView imageViewBackground;
    @BindView(R.id.splashHead)
    RecyclerView splashHead;



    private int login_mode;
    private String idUser = null;
    private String first_access = null;
    private List<Produto> produtos;
    LocalDatabase db;
    FirebaseStorage storage;
    StorageReference storageRef;
    AdapterImageItemShop adapter;
    private AppCompatImageButton buttonMyCart = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_market, container, false);
        ButterKnife.bind(this, view);
        ( (MainActivity)getActivity()).updateToolbarTitle("Shop");

        recyclerViewMarket.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewMarket.setItemAnimator(new DefaultItemAnimator());
        recyclerViewMarket.setHasFixedSize(true);

        final LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        splashHead.setLayoutManager(layoutManager);
        splashHead.setItemAnimator(new DefaultItemAnimator());

        splashHead.setHasFixedSize(true);



        db = new LocalDatabase(getContext());
        login_mode = getArguments().getInt("LOGIN");
        first_access = getArguments().getString("FIRST_ACCESS");

        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        loadImagesHead();
        loadItensShop();

        String checkId = new LocalDatabase(getContext()).loadSettings().getUserId();

        if (checkId.equals("0")) {
            showCaseViewPST(recyclerViewMarket, null, getResources().getString(R.string.login_market ),1);
        }




        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (buttonMyCart != null ) {
            List<CartModel> cart = new LocalDatabase(getContext()).selectItens();
            buttonMyCart.setImageResource(R.drawable.ico_market_cart);
            if (cart != null) {
                if (cart.size() > 0) {
                    buttonMyCart.setImageResource(R.drawable.ico_market_cart_item);
                }
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater =  getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_minhas_compras, menu);
        final MenuItem myCartItem = menu.findItem(R.id.menumarket_car);




        if (myCartItem != null) {
            buttonMyCart = (AppCompatImageButton) myCartItem.getActionView();

            List<CartModel> cart = new LocalDatabase(getContext()).selectItens();
            buttonMyCart.setImageResource(R.drawable.ico_market_cart);
            if (cart != null) {
                 if (cart.size() > 0) {
                     buttonMyCart.setImageResource(R.drawable.ico_market_cart_item);
                 }
            }


            buttonMyCart.setBackgroundColor(Color.TRANSPARENT);
            buttonMyCart.setAdjustViewBounds(true);
            buttonMyCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(), GeneralActivity.class);
                    i.putExtra("fragment", 800);
                    i.putExtra("LOGIN", login_mode);
                    startActivityForResult(i,0);
                }
            });
        }


    }



    private void loadItensShop(){

        String metodo = "/SP_BUSCAR_PRODUTO?token=" + idUser;
        GetMethod get = new GetMethod(metodo, getContext(), new GetMethod.Callback() {
            @Override
            public void run(String result) {



                Type listType = new TypeToken<ArrayList<Produto>>(){}.getType();
                produtos = new Gson().fromJson(result, listType);

                if (produtos != null) {

                    if (produtos.size() > 0) {

                        recyclerViewMarket.setVisibility(View.VISIBLE);
                        splashHead.setVisibility(View.VISIBLE);
                        imageViewBackground.setVisibility(View.GONE);

                        recyclerViewMarket.setAdapter(new AdapterShop(getContext(), produtos, onClick(), 1));
                    } else {
                        recyclerViewMarket.setVisibility(View.GONE);
                        splashHead.setVisibility(View.GONE);
                        imageViewBackground.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerViewMarket.setVisibility(View.GONE);
                    splashHead.setVisibility(View.GONE);
                    imageViewBackground.setVisibility(View.VISIBLE);
                }
            }
        }, "Carregando...");
        get.execute();
    }

    private AdapterShop.ShopOnClickListener onClick() {

        return new AdapterShop.ShopOnClickListener() {
            @Override
            public void onClick(View view, int index) {
                    String gson = new Gson().toJson(produtos.get(index));


                Intent i = new Intent(getContext(), GeneralActivity.class);
                i.putExtra("fragment", 700);
                i.putExtra("LOGIN", login_mode);
                i.putExtra("ITEM_SHOP", gson);
                startActivityForResult(i,0);

            }

            @Override
            public void onClickButton(View view, final int index) {
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

                        String gson = new Gson().toJson(produtos.get(index));
                        db.addItemCart(gson, 1, codigo_pet);

                        buttonMyCart.setImageResource(R.drawable.ico_market_cart_item);
                        showtAutoDismissDialog("Item adicionado ao carrinho");
                    }
                });

                dialog.show();







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


    private void loadImagesHead(){
        String metodo = "/SP_BUSCAR_IMAGEM?tipo=1&qtd=4";

        new GetSingleMethod(metodo, getContext(), new GetSingleMethod.Callback() {
            @Override
            public void run(String result) {
                Type listType = new TypeToken<ArrayList<Imagem>>(){}.getType();
                List<Imagem> listaImagem = new Gson().fromJson(result, listType);
                if (listaImagem != null) {
                    List<String> url = new ArrayList<>();
                    for (int i = 0; i < listaImagem.size(); i++) {
                        url.add(listaImagem.get(i).getUrl_imagem());
                    }
                     adapter = new AdapterImageItemShop(getContext(), url, onClickListener());
                    splashHead.setAdapter(adapter);
                    splashHead.addItemDecoration(new CirclePagerIndicatorDecoration());
                }

            }
        }).execute();

    }


    private AdapterImageItemShop.ImageOnClickListener onClickListener(){
        return new AdapterImageItemShop.ImageOnClickListener() {
            @Override
            public void onClick(View view, int index) {

            }
        };
    }


    private void showCaseViewPST(View view, String title, String text, final int sCase) {
        Typeface t = ResourcesCompat.getFont(getContext(), R.font.honeyscript);
        TextPaint p = new TextPaint();
        p.setTypeface(t);
        p.setColor(Color.WHITE);
        p.setTextSize(230);


        new ShowcaseView.Builder(getActivity())
                .withNewStyleShowcase()
                .setStyle(R.style.CustomShowcaseTheme3)
                .setTarget(new ViewTarget(view))
                .hideOnTouchOutside()
                .setContentTitlePaint(p)
                .setContentTitle(title)
                .setContentText(text)
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {

                        ((MainActivity) getActivity()).switchTab(2);
                        ((MainActivity) getActivity()).updateTabSelection(2);


                    }


                }).build();



    }


}
