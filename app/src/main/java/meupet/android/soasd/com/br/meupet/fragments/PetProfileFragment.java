package meupet.android.soasd.com.br.meupet.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import br.com.soasd.meupet.BuscarPet;
import br.com.soasd.meupet.RegistroLeitura;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.httpservices.PrivatePostMethod;

/**
 * A simple {@link Fragment} subclass.
 */
public class PetProfileFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener {
    String myFormat = "dd/MM/yyyy";
    DateFormat df = new SimpleDateFormat(myFormat);

    @BindView(R.id.imageViewProfileBackground)
    SimpleDraweeView imageViewProfileBackground;
    @BindView(R.id.imageViewProfilePet)
    SimpleDraweeView imageViewprofilePet;
    @BindView(R.id.textViewPetName)
    TextView textViewNomePet;
    @BindView(R.id.textViewRaca)
    TextView textViewRaca;
    @BindView(R.id.textViewNomeProprietario)
    TextView textviewNomeProprietario;
    @BindView(R.id.textViewEndereco)
    TextView textViewEndereco;
    @BindView(R.id.textViewContato)
    TextView textViewContato;
    @BindView(R.id.textViewMensagem)
    TextView textViewMensagem;
    @BindView(R.id.imageButtonCall)
    ImageButton imageButtonCall;
    @BindView(R.id.imageButtonWhatsapp)
    ImageButton imageButtonWhatsapp;




    FirebaseStorage storage;
    StorageReference storageRef;
    private BuscarPet pet;
    private String idUser;
    private String petGson;
    private int origem;
    GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;

    public PetProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet_profile, container, false);
        ButterKnife.bind(this, view);


        petGson = getArguments().getString("PETGSON");
        pet = new Gson().fromJson(petGson, BuscarPet.class);
        int login_mode = getArguments().getInt("LOGIN");
        origem = getArguments().getInt("ORIGEM");

        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        loadData(pet);
        if (origem != 0) {
            ((GeneralActivity) getActivity()).updateToolbarTitle("Pet Identificado!");
        } else {
            ((GeneralActivity) getActivity()).updateToolbarTitle(pet.getText_nome_pet());
        }

        ((GeneralActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri call = Uri.parse("tel:" + textViewContato.getText().toString());
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(call);
                startActivity(i);
            }
        });

        imageButtonWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toNumber = "55" + textViewContato.getText().toString();
                String text_femea = "Ola!, Encontrei a " + pet.getText_nome_pet();
                String text_macho = "Ola!, Encontrei o " + pet.getText_nome_pet();
                String text = "";
                if(pet.getGenero().equals("MACHO")) {
                     text = text_macho + ". Você poderia vir busca-lo?";
                } else {
                     text = text_femea + ". Você poderia vir busca-la?";
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
                startActivity(intent);




            }
        });



        playServices();
        if (pet.getPet_localizado() != 1) {
            if (origem != 0)
                buscarLocalGPS();


        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().setResult(0);
                getActivity().finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    private void loadData(BuscarPet b) {
        if (b.getUrl_foto().length() > 5) {
            loadImage(b.getUrl_foto(), 1);
        } else {
                imageViewprofilePet.setActualImageResource(R.drawable.img_catdog);
        }

        if (b.getUrl_capa().length() > 5) {
            loadImage(b.getUrl_capa(), 2);
        } else {
            if (b.getGenero().equals("Macho")) {
                imageViewProfileBackground.setActualImageResource(R.drawable.image_background);
                imageViewProfileBackground.setBackground(getResources().getDrawable(R.drawable.image_background));
            } else {
                imageViewProfileBackground.setActualImageResource(R.drawable.image_background_roxo);
                imageViewProfileBackground.setBackground(getResources().getDrawable(R.drawable.image_background_roxo));
            }
        }

        textViewNomePet.setText(b.getText_nome_pet());
        textViewRaca.setText(b.getGenero() + ", " + b.getText_descricao());
        textviewNomeProprietario.setText(b.getText_nome_proprietario());
        textViewEndereco.setText(b.getText_endereco());
        textViewContato.setText(b.getText_fone());
        if (b.getPet_localizado() != 1) {
            textViewMensagem.setVisibility(View.VISIBLE);
        } else {
            textViewMensagem.setVisibility(View.GONE);
        }
        checkWhatsApp();
        if (b.getText_fone().length() < 3) {
            imageButtonWhatsapp.setVisibility(View.GONE);
            imageButtonCall.setVisibility(View.GONE);
            textViewContato.setText("Não consta");
        }


    }

    private void loadImage(String url, final int img) {


        StorageReference pathReference = storageRef.child(url);

        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (img == 1) {
                    imageViewprofilePet.setImageURI(uri);
                    imageViewprofilePet.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageViewProfileBackground.setImageURI(uri);
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (img == 1) {
                    imageViewprofilePet.setActualImageResource(R.drawable.img_catdog);
                }
            }
        });

    }

    private void checkWhatsApp() {
        PackageManager pm = getActivity().getPackageManager();

        try {
            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            imageButtonWhatsapp.setVisibility(View.VISIBLE);
        } catch (PackageManager.NameNotFoundException e) {
            imageButtonWhatsapp.setVisibility(View.GONE);
        }
    }

    private void playServices() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void buscarLocalGPS() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        sendNotification(location);
                    } else {
                        isGPSOn();
                    }
                }
            });
        }

    }

    private void isGPSOn(){
        LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.important)
                    .setMessage("Seu GPS está desativado. Ele é importante para que possamos ajudar Pets perdidos a voltar para casa. " +
                            "Gostaria de ativa-lo agora?")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton(R.string.no, null);
            dialog.show();

        }


    }

    private void sendNotification(Location location){
        RegistroLeitura r = new RegistroLeitura();
        r.setCodigo_validador(pet.getCodigo_validador());
        r.setData_registro(df.format(System.currentTimeMillis()));
        r.setLatitude(location.getLatitude());
        r.setLongitude(location.getLongitude());
        String postGson = new Gson().toJson(r);

        String metodo = "/SP_REGISTRAR_LEITURA";
        PrivatePostMethod post = new PrivatePostMethod(postGson, metodo, getContext());
        post.execute();
    }


}
