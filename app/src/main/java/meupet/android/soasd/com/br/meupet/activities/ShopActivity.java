package meupet.android.soasd.com.br.meupet.activities;

import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;


public class ShopActivity extends BaseActivity {

    @BindView(R.id.tabLayoutShop)
    TabLayout tabLayout;
    @BindArray(R.array.shop_tab_name)
    String[] TABS;
    @BindView(R.id.toolbarShop)
    Toolbar toolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }





}
