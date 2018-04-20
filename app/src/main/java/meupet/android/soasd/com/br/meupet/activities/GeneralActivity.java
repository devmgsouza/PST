package meupet.android.soasd.com.br.meupet.activities;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;

import meupet.android.soasd.com.br.meupet.fragments.CartFragment;
import meupet.android.soasd.com.br.meupet.fragments.ComprasFragment;
import meupet.android.soasd.com.br.meupet.fragments.ItemShopFragment;
import meupet.android.soasd.com.br.meupet.fragments.CadastroPetFragment;
import meupet.android.soasd.com.br.meupet.fragments.HealthFragment;
import meupet.android.soasd.com.br.meupet.fragments.HealthListFragment;
import meupet.android.soasd.com.br.meupet.fragments.ItemVendaFragment;
import meupet.android.soasd.com.br.meupet.fragments.PetProfileFragment;
import meupet.android.soasd.com.br.meupet.fragments.PetViewFragment;
import meupet.android.soasd.com.br.meupet.fragments.SettingsFragment;

public class GeneralActivity extends BaseActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;


    static String first_access = null;
    static int login;
    static String petGson = null;
    static int origem;
    static int HEALTH_UPDATE;
    static String hGson;
    static String itemShop;
    static int rgVenda;
    static double vTotal;
    FragmentTransaction fragmentTransaction;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
        ButterKnife.bind(this);
        initToolbar();


        Bundle extras = getIntent().getExtras();
        first_access = extras.getString("FIRST_ACCESS");
        login = extras.getInt("LOGIN");
        petGson = extras.getString("PETGSON");
        origem = extras.getInt("ORIGEM");
        HEALTH_UPDATE = extras.getInt("HEALTH_UPDATE");
        hGson = extras.getString("HEALTH");
        itemShop = extras.getString("ITEM_SHOP");
        rgVenda = extras.getInt("REGISTRO_VENDA");
        vTotal = extras.getDouble("VALOR_TOTAL");

        loadFragment(getIntent().getExtras().getInt("fragment"));

    }

    private void initToolbar() {
        setSupportActionBar(toolbar);


    }

    public void updateToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);

    }

    private void loadFragment(int frag_id){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;

        Bundle data = new Bundle();
        data.putInt("LOGIN", login);
        data.putString("FIRST_ACCESS", first_access);
        data.putString("PETGSON", petGson);
        data.putInt("ORIGEM", origem);
        data.putString("HEALTH", hGson);
        data.putInt("HEALTH_UPDATE", HEALTH_UPDATE);
        data.putString("ITEM_SHOP", itemShop);
        data.putInt("REGISTRO_VENDA", rgVenda);
        data.putDouble("VALOR_TOTAL", vTotal);

        if (frag_id == 100) {
            fragment =  new CadastroPetFragment();
        } else if (frag_id == 200) {
            fragment =  new PetViewFragment();
        } else if (frag_id == 300) {
            fragment = new PetProfileFragment();
        } else if (frag_id == 400){
            fragment = new HealthListFragment();
        } else if (frag_id == 500) {
            fragment = new HealthFragment();
        } else if (frag_id == 600) {
            fragment = new SettingsFragment();
        } else if (frag_id == 700) {
            fragment = new ItemShopFragment();
        } else if (frag_id == 800){
            fragment = new CartFragment();
        } else if (frag_id == 900){
            fragment = new ComprasFragment();
        } else if (frag_id == 1000){
            fragment = new ItemVendaFragment();
        }

        fragment.setArguments(data);


        fragmentTransaction.replace(R.id.content_general_frame, fragment);
        fragmentTransaction.commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
