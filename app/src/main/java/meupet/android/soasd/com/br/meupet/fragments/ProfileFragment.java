package meupet.android.soasd.com.br.meupet.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import org.json.JSONObject;
import br.com.sapereaude.maskedEditText.MaskedEditText;
import br.com.soasd.meupet.Usuario;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.activities.MainActivity;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PostMethod;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;

import static meupet.android.soasd.com.br.meupet.R.drawable.ico_settings;


public class ProfileFragment extends BaseFragment{

    @BindView(R.id.imageProfile)
    SimpleDraweeView imageProfile;
    @BindView(R.id.editTextNomeProfile)
    EditText editTextNomeProfile;
    @BindView(R.id.editTextSobrenomeProfile)
    EditText editTextSobrenomeProfile;
    @BindView(R.id.editTextEmailProfile)
    EditText editTextEmailProfile;
    @BindView(R.id.editTextFoneProfile)
    MaskedEditText editTextFoneProfile;
    @BindView(R.id.editTextAddressProfile)
    EditText editTextAddressProfile;
    @BindView(R.id.editTextCityProfile)
    EditText editTextCityProfile;
    @BindView(R.id.spinnerUf)
    Spinner spinner;
    ArrayAdapter spinnerAdapter;

    @BindView(R.id.buttonSalvarProfile)
    TextView buttonSalvarProfile;
    @BindView(R.id.buttonEditarProfile)
    TextView buttonEditarProfile;
    private int editar = 0;
    private String userId = null;
    private int login_mode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



    }

    @Override
    public void onResume() {
        super.onResume();
        SettingsModel m = new LocalDatabase(getContext()).loadSettings();
        if (m!=null) {
            buttonSalvarProfile.setTextSize(m.getFonteSize());
            buttonEditarProfile.setTextSize(m.getFonteSize());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(this, view);
        Fresco.initialize(getContext());
        ( (MainActivity)getActivity()).updateToolbarTitle("Perfil");
        loadSpinner();

        login_mode = getArguments().getInt("LOGIN");
        //String first_access = getArguments().getString("FIRST_ACCESS");
        if (login_mode == 1) {
            loadDataFromFacebook();
        } else {

            loadDataFromGoogle();
        }


        String checkId = new LocalDatabase(getContext()).loadSettings().getUserId();

        if (checkId.equals("0")) {
            showCaseViewPST(buttonEditarProfile, getResources().getString(R.string.login_dados_atualizados), getResources().getString(R.string.login_dados_atualizar ),1);
        }


        SettingsModel m = new LocalDatabase(getContext()).loadSettings();
        if (m!=null) {
            buttonSalvarProfile.setTextSize(m.getFonteSize());
            buttonEditarProfile.setTextSize(m.getFonteSize());
        }


        buttonEditarProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editar == 0) {
                    editar = 1;
                    buttonSalvarProfile.setVisibility(Button.VISIBLE);
                    buttonEditarProfile.setText(R.string.cancel);
                    editTextAddressProfile.setEnabled(true);
                    editTextCityProfile.setEnabled(true);
                    editTextFoneProfile.setEnabled(true);
                    spinner.setEnabled(true);
                } else {
                    editar = 0;
                    buttonEditarProfile.setText(R.string.edit);
                    buttonSalvarProfile.setVisibility(Button.GONE);
                    editTextAddressProfile.setEnabled(false);
                    editTextCityProfile.setEnabled(false);
                    editTextFoneProfile.setEnabled(false);
                    spinner.setEnabled(false);
                }
            }
        });
        buttonSalvarProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Usuario usuario = new Usuario();
                usuario.setText_endereco(editTextAddressProfile.getText().toString());
                usuario.setText_cidade(editTextCityProfile.getText().toString());
                usuario.setText_uf(spinner.getSelectedItem().toString());
                usuario.setText_fone(editTextFoneProfile.getRawText());
                String postGson = new Gson().toJson(usuario);
                String metodo = "/SP_ATUALIZAR_USUARIO?id=" + userId;
                PostMethod post = new PostMethod(postGson, metodo, getContext(), new PostMethod.Callback() {
                    @Override
                    public void run(String result) {
                   showtAutoDismissDialog(result);

                    }
                }, "Atualizando...");
                post.execute();
                editar = 0;
                buttonSalvarProfile.setVisibility(Button.GONE);
                buttonEditarProfile.setText(R.string.edit);
                editTextAddressProfile.setEnabled(false);
                editTextCityProfile.setEnabled(false);
                editTextFoneProfile.setEnabled(false);
                spinner.setEnabled(false);
            }
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_settings, menu);
        final MenuItem addItemMenu = menu.findItem(R.id.menu_settings_button);

        AppCompatImageButton buttonSettings = null;

        if (addItemMenu != null) {
                buttonSettings = (AppCompatImageButton) addItemMenu.getActionView();
                buttonSettings.setImageResource(ico_settings);
                buttonSettings.setBackgroundColor(Color.TRANSPARENT);
                buttonSettings.setAdjustViewBounds(true);
                buttonSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), GeneralActivity.class);
                        i.putExtra("fragment", 600);
                        i.putExtra("LOGIN", login_mode);
                        startActivityForResult(i,0);
                    }
                });
            }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == 444){
            logout();

        }
    }

    private void loadSpinner(){
        spinnerAdapter= ArrayAdapter.createFromResource(getContext(), R.array.uf_brasil,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setEnabled(false);

    }

    private void loadDataFromGoogle(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        imageProfile.setImageURI(user.getPhotoUrl());
        userId = user.getUid();
        buscarUsuario(userId);


    }

    private void loadDataFromFacebook(){
        Bundle bundle = new Bundle();
        bundle.putString("fields", "picture.width(200).height(200)");
        userId = AccessToken.getCurrentAccessToken().getUserId();
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/",
                bundle,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        if (response != null) {
                            try {
                                JSONObject data = response.getJSONObject();
                                String pictureUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                Uri uri = Uri.parse(pictureUrl);
                                imageProfile.setImageURI(uri);

                                buscarUsuario(userId);
                            } catch (Exception e) {

                            }
                        }
                    }
                }
        ).executeAsync();
    }

    private void buscarUsuario(String id) {
        String metodo = "/SP_BUSCAR_USUARIO?id=" + id;
        new GetMethod(metodo, getContext(), new GetMethod.Callback() {
            @Override
            public void run(String result) {
                Usuario usuario = new Gson().fromJson(result, Usuario.class);
                if (usuario != null) {
                    editTextNomeProfile.setText(usuario.getText_nome());
                    editTextSobrenomeProfile.setText(usuario.getText_sobrenome());
                    editTextEmailProfile.setText(usuario.getText_email());

                    editTextAddressProfile.setText(usuario.getText_endereco());
                    editTextCityProfile.setText(usuario.getText_cidade());
                    editTextFoneProfile.setText(usuario.getText_fone());
                    int adapterPosition = spinnerAdapter.getPosition(usuario.getText_uf());
                    spinner.setSelection(adapterPosition);
                }
                }
        }, "Carregando perfil...").execute();
    }

    private void logout(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        getActivity().setResult(444);
        getActivity().finish();
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


                        new LocalDatabase(getContext()).updateSettings(userId);

                        ((MainActivity) getActivity()).switchTab(0);
                        ((MainActivity) getActivity()).updateTabSelection(0);

                    }


                }).build();



    }
}
