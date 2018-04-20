package meupet.android.soasd.com.br.meupet.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentController;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import br.com.soasd.meupet.Pet;
import br.com.soasd.meupet.Raca;
import br.com.soasd.meupet.RegistroLeitura;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.CheckoutActivity;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.activities.MainActivity;
import meupet.android.soasd.com.br.meupet.adapter.AdapterPets;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.DeleteMethod;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;
import meupet.android.soasd.com.br.meupet.httpservices.GetSingleMethod;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;

import static android.app.Activity.RESULT_OK;


public class HomeFragment extends BaseFragment {
    private static final int REQUEST_PERMISSIONS_CODE = 128;
    @BindView(R.id.buttonIdentificarPet)
    FloatingActionButton buttonIdentificarPet;
    @BindView(R.id.recyclerViewMeusPets)
    RecyclerView recyclerView;
    @BindView(R.id.imageViewBackgroundoHome)
    ImageView imageViewBackgroundoHome;
    @BindView(R.id.swipeRefreshHome)
    SwipeRefreshLayout swipeRefreshHome;
    AppCompatButton buttonAdd = null;
    FirebaseStorage storage;
    StorageReference storageRef;
    String idUser = null;
    private List<Pet> meusPets;
    private int login_mode;
    private String first_access = null;




    public HomeFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        login_mode = getArguments().getInt("LOGIN");
        first_access = getArguments().getString("FIRST_ACCESS");
        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        ((MainActivity) getActivity()).updateToolbarTitle("Meus Pets");

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);


        loadMyPets();


        buttonIdentificarPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solicitaPermission();
            }
        });

        swipeRefreshHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMyPets();
                swipeRefreshHome.setRefreshing(false);
            }

        });


        int count;
        count = new LocalDatabase(getContext()).buscarRacas(1).size();
        if (count == 0) {
            buscarRacas();
        }

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            String contents = data.getStringExtra("SCAN_RESULT");
            String codigo_validador = contents.substring(contents.length() - 6);
            petIdentify(codigo_validador, 1);
        }
        if (resultCode == 101) {
            loadMyPets();
        }


    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_pet, menu);
        final MenuItem addItemMenu = menu.findItem(R.id.menupets_addPet);


        if (addItemMenu != null) {
            buttonAdd = (AppCompatButton) addItemMenu.getActionView();

            String checkId = new LocalDatabase(getContext()).loadSettings().getUserId();

            if (checkId.equals("0")) {
                showCaseViewPST(null, getResources().getString(R.string.welcome), getResources().getString(R.string.first_access),1);

            }


            SettingsModel m = new LocalDatabase(getContext()).loadSettings();
            if (m != null) {
                buttonAdd.setTextSize(m.getFonteSize());
            }


            buttonAdd.setText(getResources().getString(R.string.add_pet));

            buttonAdd.setBackgroundColor(Color.TRANSPARENT);
            buttonAdd.setAllCaps(false);
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(getContext(), GeneralActivity.class);
                    i.putExtra("fragment", 100);
                    i.putExtra("LOGIN", login_mode);
                    i.putExtra("FIRST_ACCESS", first_access);
                    i.putExtra("PETGSON", "");
                    startActivityForResult(i, 0);
                }
            });

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults[0] == 0 && grantResults[1] == 0) {
                ativarLeitor();
            } else {
                createDialogYes(R.string.error, R.string.necessary_permission, R.string.yes);
            }

        }

    }


    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            createDialogYes(R.string.error, R.string.error_camera, R.string.continue_to);
            return false;
        }
    }

    private void solicitaPermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_CODE);
        } else {
            ativarLeitor();
        }

    }

    private void ativarLeitor() {
        if (checkCameraHardware(getActivity())) {
            IntentIntegrator i = new IntentIntegrator(getActivity());
            i.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            i.setOrientationLocked(true);
            i.setBeepEnabled(true);

            i.setPrompt("Posicione a camera no QR_CODE da Coleira para identificar o Pet");
            startActivityForResult(i.createScanIntent(), 0);
        }
    }

    private void createDialogYes(int title, int message, int YES) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);

                    }
                }).show();
    }

    private void loadMyPets() {

        String metodo = "/SP_BUSCAR_PET?id=" + idUser;
        GetMethod get = new GetMethod(metodo, getContext(), new GetMethod.Callback() {
            @Override
            public void run(String result) {

                Type listType = new TypeToken<ArrayList<Pet>>() {
                }.getType();
                meusPets = new Gson().fromJson(result, listType);
                if (meusPets != null) {
                    if (meusPets.size() > 0) {
                        new LocalDatabase(getContext()).deleteAllPET();
                        for (int i = 0; i < meusPets.size(); i++) {
                            new LocalDatabase(getContext()).inserirPet(new Gson().toJson(meusPets.get(i)),
                                    meusPets.get(i).getCodigo_validador());
                        }


                        imageViewBackgroundoHome.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(new AdapterPets(getContext(), meusPets, onClickPet()));
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        imageViewBackgroundoHome.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    imageViewBackgroundoHome.setVisibility(View.VISIBLE);
                }
            }
        }, "Buscando seus Pets");
        get.execute();
    }

    private void deletePet(final Pet pet) {
        String metodo = "/SP_EXCLUIR_PET?codigo=" + pet.getCodigo_validador() + "&id=" + idUser;
        DeleteMethod delete = new DeleteMethod(metodo, getContext(), new DeleteMethod.Callback() {
            @Override
            public void run(String result) {
                if (result.equals("Registro excluido")) {
                    new LocalDatabase(getContext()).deletePET(pet.getCodigo_validador());
                    if (!(pet.getUrl_foto().length() < 5)) {
                        deleteImages(pet.getUrl_foto(), 1);
                    }
                    if (!(pet.getUrl_capa().length() < 5)) {
                        deleteImages(pet.getUrl_foto(), 2);
                    }
                    showtAutoDismissDialog(result);
                    loadMyPets();
                } else {
                    showtAutoDismissDialog(result);
                }
            }
        }, "Apagando registro...");
        delete.execute();
    }

    private void deleteImages(String url, int param) {
        StorageReference desertRef = null;
        if (param == 1) {
            desertRef = storageRef.child(url);
        } else {
            desertRef = storageRef.child(url);
        }


        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    private void showtAutoDismissDialog(String mensagem) {
        TextView msg = new TextView(getContext());
        msg.setText("\n" + mensagem + "\n");
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ico_carimbo)
                .setTitle(R.string.app_name)

                .setView(msg);

        final AlertDialog alert = dialog.create();
        alert.show();


        final Handler handler = new Handler();
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

    private AdapterPets.MeusPetsOnClickListener onClickPet() {
        return new AdapterPets.MeusPetsOnClickListener() {
            @Override
            public void onClickPet(View view, int index) {
                Pet meuPet = meusPets.get(index);
                String petGson = new Gson().toJson(meuPet);
                Intent i = new Intent(getContext(), GeneralActivity.class);
                i.putExtra("fragment", 200);
                i.putExtra("LOGIN", login_mode);
                i.putExtra("FIRST_ACCESS", first_access);
                i.putExtra("PETGSON", petGson);
                startActivityForResult(i, 0);
            }

            @Override
            public void onLongClick(View view, int index) {
                final Pet meuPet = meusPets.get(index);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.atention)
                        .setMessage(getResources().getString(R.string.remove_pet) + " " +
                                meuPet.getText_nome() + "? " + getResources().getString(R.string.remove_pet_p2))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deletePet(meuPet);

                            }
                        }).setNegativeButton(R.string.no, null);
                dialog.show();
            }

            @Override
            public void onClickMenu(View view, int index) {
                final Pet pet = meusPets.get(index);

                PopupMenu popup = new PopupMenu(getContext(), view);
                popup.getMenuInflater()
                        .inflate(R.menu.menu_card_more, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_excluir:

                                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                                        .setTitle(R.string.atention)
                                        .setMessage(getResources().getString(R.string.remove_pet) + " " +
                                                pet.getText_nome() + "? " + getResources().getString(R.string.remove_pet_p2))
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deletePet(pet);

                                            }
                                        }).setNegativeButton(R.string.no, null);
                                dialog.show();

                                break;
                            case R.id.menu_perfil:
                                petIdentify(pet.getCodigo_validador(), 0);
                                break;

                        }


                        return true;
                    }
                });

                popup.show();

            }

            @Override
            public void onClickHealth(View view, int index) {
                Pet meuPet = meusPets.get(index);
                String petGson = new Gson().toJson(meuPet);
                Intent i = new Intent(getContext(), GeneralActivity.class);
                i.putExtra("fragment", 400);
                i.putExtra("LOGIN", login_mode);
                i.putExtra("FIRST_ACCESS", first_access);
                i.putExtra("PETGSON", petGson);
                startActivityForResult(i, 0);

            }

            @Override
            public void onClickMap(View view, int index) {
                final Pet pet = meusPets.get(index);
                buscarLocalPet(pet);
            }


        };
    }


    private void petIdentify(String codigo_validador, final int origem) {
        String metodo = "/PET/" + codigo_validador;
        GetSingleMethod get = new GetSingleMethod(metodo, getContext(), new GetSingleMethod.Callback() {
            @Override
            public void run(String result) {
                if (!(result.length() < 10)) {
                    Intent i = new Intent(getContext(), GeneralActivity.class);
                    i.putExtra("fragment", 300);
                    i.putExtra("LOGIN", login_mode);
                    i.putExtra("FIRST_ACCESS", first_access);
                    i.putExtra("PETGSON", result);
                    i.putExtra("ORIGEM", origem);
                    ((AppCompatActivity) getContext()).startActivityForResult(i, 0);
                } else {
                    showtAutoDismissDialog("Pet Não registrado");
                }
            }
        });
        get.execute();


    }

    private void buscarLocalPet(final Pet p) {
        String metodo = "/SP_BUSCAR_LOCALIZACAO?codigo=" + p.getCodigo_validador() + "&id=" + idUser;
        GetSingleMethod get = new GetSingleMethod(metodo, getContext(), new GetSingleMethod.Callback() {
            @Override
            public void run(String result) {
                Type listType = new TypeToken<ArrayList<RegistroLeitura>>() {
                }.getType();
                List<RegistroLeitura> leituras = new Gson().fromJson(result, listType);
                if (leituras.size() > 0) {
                    RegistroLeitura l = leituras.get(0);
                    String label = "";
                    if (p.getGenero().equals("MACHO")) {
                        label = "(Uma leitura da TAG do " + p.getText_nome() + " foi realizado no dia " + l.getData_registro()
                                + ", Bem aqui!)";
                    } else {
                        label = "(Uma leitura da TAG da " + p.getText_nome() + " foi realizado no dia " + l.getData_registro()
                                + ", Bem aqui!)";
                    }

                    String latlon = l.getLatitude() + "," + l.getLongitude();
                    String uriString = "geo:0,0?q=" + latlon + label;
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                    //intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);


                } else {
                    showtAutoDismissDialog("Nenhum registro encontrado no momento");
                }
            }
        });
        get.execute();
    }



    private void showCaseViewPST(View view, String title, String text, final int sCase) {
        Typeface t = ResourcesCompat.getFont(getContext(), R.font.honeyscript);
        TextPaint p = new TextPaint();
        p.setTypeface(t);
        p.setColor(Color.WHITE);
        p.setTextSize(280);


       ShowcaseView.Builder scv = new ShowcaseView.Builder(getActivity())
                .withNewStyleShowcase()
                .setStyle(R.style.CustomShowcaseTheme3);
                if (sCase != 1) {
                    scv.setTarget(new ViewTarget(view));
                }
                scv.hideOnTouchOutside()
                .setContentTitlePaint(p)
                .setContentTitle(title)
                .setContentText(text)
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        if (sCase == 1){
                            showCaseViewPST(buttonAdd, getResources().getString(R.string.login_registrando), getResources().getString(R.string.login_registrar ),2);

                        } else if (sCase == 2){
                            showCaseViewPST(buttonIdentificarPet, getResources().getString(R.string.login_identificando), getResources().getString(R.string.login_identificarpet ),3);
                        } else if (sCase == 3) {
                             first_access = "1";
                            ((MainActivity) getActivity()).switchTab(1);
                            ((MainActivity) getActivity()).updateTabSelection(1);

                        }
                    }


                });
                scv.build();



    }

    private void buscarRacas(){

        new GetSingleMethod("/SP_BUSCAR_TODASRACAS", getContext(), new GetSingleMethod.Callback() {
            @Override
            public void run(String result) {
                Type listType = new TypeToken<ArrayList<Raca>>() {
                }.getType();
                List<Raca> racasCadastrada = new Gson().fromJson(result, listType);

                if (racasCadastrada != null) {
                    if (racasCadastrada.size() > 0) {
                        new LocalDatabase(getContext()).deleteAllRacas();
                        for (int i = 0; i < racasCadastrada.size(); i++){
                            new LocalDatabase(getContext()).inserirRaca(racasCadastrada.get(i));
                        }
                    }
                }
            }
        }).execute();

    }



}
