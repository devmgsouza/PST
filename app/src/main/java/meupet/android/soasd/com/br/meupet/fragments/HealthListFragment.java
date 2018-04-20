package meupet.android.soasd.com.br.meupet.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import br.com.soasd.meupet.BuscarPet;
import br.com.soasd.meupet.Health;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.adapter.AdapterHealth;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.DeleteMethod;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;


public class HealthListFragment extends Fragment {
    @BindView(R.id.recyclerViewHealth)
    RecyclerView recyclerView;
    @BindView(R.id.imageViewBackgroundHealth)
    ImageView imageViewBackgroundHealth;
    @BindView(R.id.swipeRefreshHealth)
    SwipeRefreshLayout swipeRefreshHealth;

    private String petGson;
    private BuscarPet pet;
    private List<Health> lista;
    private int login_mode;
    private String idUser;
    public HealthListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_list, container, false);
        ButterKnife.bind(this, view);
        ((GeneralActivity) getActivity()).updateToolbarTitle("Cuidados médicos");

        ((GeneralActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        petGson = getArguments().getString("PETGSON");
        login_mode = getArguments().getInt("LOGIN");

        pet = new Gson().fromJson(petGson, BuscarPet.class);
        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        loadCuidados(pet.getCodigo_validador());
        swipeRefreshHealth.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadCuidados(pet.getCodigo_validador());
                swipeRefreshHealth.setRefreshing(false);
            }
        });


        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = ((GeneralActivity) getActivity()).getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_cuidado, menu);
        final MenuItem addItemMenu = menu.findItem(R.id.menu_addCuidado);
        AppCompatButton buttonAdd = null;

        if (addItemMenu != null) {
            buttonAdd = (AppCompatButton) addItemMenu.getActionView();

            SettingsModel m = new LocalDatabase(getContext()).loadSettings();
            if (m!=null) {
                buttonAdd.setTextSize(m.getFonteSize());
            }

            buttonAdd.setText(getResources().getString(R.string.add));
            buttonAdd.setBackgroundColor(Color.TRANSPARENT);
            buttonAdd.setAllCaps(false);
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(), GeneralActivity.class);
                    i.putExtra("fragment", 500);
                    i.putExtra("PETGSON", petGson);
                    i.putExtra("HEALTH", "");
                    i.putExtra("HEALTH_UPDATE", 0);
                    i.putExtra("LOGIN",login_mode);
                    startActivityForResult(i, 0);
                }
            });

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 501) {
            loadCuidados(pet.getCodigo_validador());
        }
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


    private void loadCuidados(String codigo_validador) {
        String metodo = "/SP_BUSCAR_CUIDADO?codigo=" + codigo_validador;
        GetMethod g = new GetMethod(metodo, getContext(), new GetMethod.Callback() {
            @Override
            public void run(String result) {
                Type listType = new TypeToken<ArrayList<Health>>() {
                }.getType();
                lista = new Gson().fromJson(result, listType);
                if (lista != null) {
                    if (lista.size()>0) {
                        imageViewBackgroundHealth.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(new AdapterHealth(getContext(), lista, onClickHealth()));
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        imageViewBackgroundHealth.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    imageViewBackgroundHealth.setVisibility(View.VISIBLE);
                }
            }
        }, "Atualizando lista...");
        g.execute();
    }

    private AdapterHealth.HealthOnClickListener onClickHealth() {
        return new AdapterHealth.HealthOnClickListener() {

            @Override
            public void onClick(View view, int index) {
                Health health = lista.get(index);
                String hGgson = new Gson().toJson(health);
                Intent i = new Intent(getContext(), GeneralActivity.class);
                i.putExtra("fragment", 500);
                i.putExtra("PETGSON", petGson);
                i.putExtra("HEALTH", hGgson);
                i.putExtra("HEALTH_UPDATE", 1);
                i.putExtra("LOGIN",login_mode);
                startActivityForResult(i, 0);

            }

            @Override
            public void onClickMore(View view, int index) {
                final Health health = lista.get(index);
                        PopupMenu popup = new PopupMenu(getContext(), view);
                popup.getMenuInflater()
                        .inflate(R.menu.menu_health, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_health_excluir:
                                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                                        .setTitle(R.string.atention)
                                        .setMessage(getResources().getString(R.string.remove_item))
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                               deleteItem(health);

                                            }
                                        }).setNegativeButton(R.string.no, null);
                                dialog.show();
                                break;
                        }
                        return true;
                    }
                });

                popup.show(); //sho
            }
        };
    }

    private void deleteItem(final Health h){
    String metodo  = "/SP_EXCLUIR_CUIDADO?codigo=" + h.getCodigo_validador();
    metodo = metodo + "&pk=" + h.getPk_health();
    metodo = metodo + "&id=" + idUser;

        DeleteMethod d = new DeleteMethod(metodo, getContext(), new DeleteMethod.Callback() {
            @Override
            public void run(String result) {
                showtAutoDismissDialog(result);
                loadCuidados(h.getCodigo_validador());
            }
        }, "Apagando...");
        d.execute();

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
}

