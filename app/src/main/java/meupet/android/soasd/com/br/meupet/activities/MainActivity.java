package meupet.android.soasd.com.br.meupet.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;


import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.fragments.BaseFragment;
import meupet.android.soasd.com.br.meupet.fragments.HomeFragment;
import meupet.android.soasd.com.br.meupet.fragments.ProfileFragment;
import meupet.android.soasd.com.br.meupet.fragments.MarketFragment;
import meupet.android.soasd.com.br.meupet.httpservices.MyFirebaseInstanceIDService;
import meupet.android.soasd.com.br.meupet.utils.FragmentHistory;
import meupet.android.soasd.com.br.meupet.utils.Utils;
import meupet.android.soasd.com.br.meupet.views.FragNavController;

public class MainActivity extends BaseActivity implements BaseFragment.FragmentNavigation, FragNavController.TransactionListener, FragNavController.RootFragmentListener {


    @BindView(R.id.content_frame)
    FrameLayout contentFrame;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    static String first_access = null;
    static int login;

    private int[] mTabIconsSelected = {
            R.drawable.tab_pets,
            R.drawable.tab_market,
            R.drawable.tab_profile};


    @BindArray(R.array.tab_name)
    String[] TABS;

    @BindView(R.id.bottom_tab_layout)
    TabLayout bottomTabLayout;

    private FragNavController mNavController;

    private FragmentHistory fragmentHistory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();

        first_access = extras.getString("FIRST_ACCESS");
        login = extras.getInt("LOGIN");
        ButterKnife.bind(this);
        new MyFirebaseInstanceIDService().onTokenRefresh();

        initToolbar();

        initTab();

        fragmentHistory = new FragmentHistory();


        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.content_frame)
                .transactionListener(this)
                .rootFragmentListener(this, TABS.length)
                .build();


        switchTab(0);

        bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                fragmentHistory.push(tab.getPosition());

                switchTab(tab.getPosition());


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                mNavController.clearStack();

                switchTab(tab.getPosition());


            }
        });


    }

    private void initToolbar() {
        setSupportActionBar(toolbar);


    }

    private void initTab() {
        if (bottomTabLayout != null) {
            for (int i = 0; i < TABS.length; i++) {
                bottomTabLayout.addTab(bottomTabLayout.newTab());
                TabLayout.Tab tab = bottomTabLayout.getTabAt(i);
                if (tab != null)
                    tab.setCustomView(getTabView(i));
            }
        }
    }


    private View getTabView(int position) {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_item_bottom, null);
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageDrawable(Utils.setDrawableSelector(MainActivity.this, mTabIconsSelected[position], mTabIconsSelected[position]));
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();
    }


    public void switchTab(int position) {
        mNavController.switchTab(position);


//        updateToolbarTitle(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case android.R.id.home:


                onBackPressed();
                return true;
        }


        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        setResult(400);
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment();
        } else {

            if (fragmentHistory.isEmpty()) {
                //super.onBackPressed();
            } else {


                if (fragmentHistory.getStackSize() > 1) {

                    int position = fragmentHistory.popPrevious();

                    switchTab(position);

                    updateTabSelection(position);

                } else {

                    switchTab(0);

                    updateTabSelection(0);

                    fragmentHistory.emptyStack();
                }
            }

        }
    }


    public void updateTabSelection(int currentTab){

        for (int i = 0; i <  TABS.length; i++) {
            TabLayout.Tab selectedTab = bottomTabLayout.getTabAt(i);
            if(currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            }else{
                selectedTab.getCustomView().setSelected(true);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 110) {
            switchTab(1);
            updateTabSelection(1);
        }
        if(resultCode == 444){
            finish();
        }

    }

    @Override
    public void onTabTransaction(Fragment fragment, int index) {

        if (getSupportActionBar() != null && mNavController != null) {


            updateToolbar();

        }
    }

    private void updateToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setDisplayShowHomeEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }



    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {

        if (getSupportActionBar() != null && mNavController != null) {

            updateToolbar();

        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        Bundle data;
        switch (index) {



            case FragNavController.TAB1:
                data = new Bundle();
                data.putInt("LOGIN", login);
                data.putString("FIRST_ACCESS", first_access);
                HomeFragment h = new HomeFragment();
                h.setArguments(data);
                return h;
            case FragNavController.TAB2:
                data = new Bundle();
                data.putInt("LOGIN", login);
                data.putString("FIRST_ACCESS", first_access);
                MarketFragment m = new MarketFragment();
                m.setArguments(data);
                return m;
            case FragNavController.TAB3:
                data = new Bundle();
                data.putInt("LOGIN", login);
                data.putString("FIRST_ACCESS", first_access);
                ProfileFragment p = new ProfileFragment();
                p.setArguments(data);
                return p;

        }
        throw new IllegalStateException("Need to send an index that we know");
    }


//    private void updateToolbarTitle(int position){
//
//
//        getSupportActionBar().setTitle(TABS[position]);
//
//    }


    public void updateToolbarTitle(String title) {


        getSupportActionBar().setTitle(title);

    }




}
