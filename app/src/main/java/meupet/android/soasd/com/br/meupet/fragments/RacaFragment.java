package meupet.android.soasd.com.br.meupet.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import br.com.soasd.meupet.Raca;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.BaseActivity;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;

/**
 * A simple {@link Fragment} subclass.
 */
public class RacaFragment extends BaseActivity {
    @BindView(R.id.toolbar2)
    Toolbar toolbar;
    @BindView(R.id.textViewPorte)
    TextView textViewPorte;
    @BindView(R.id.textViewOrigem)
    TextView textViewOrigem;
    @BindView(R.id.textViewPesoMedio)
    TextView textViewPesoMedio;
    @BindView(R.id.textViewTemperamento)
    TextView textViewTemperamento;
    @BindView(R.id.textViewDoencas)
    TextView textViewDoencas;
    @BindView(R.id.imageViewRacaHead)
    SimpleDraweeView imageViewRacaHead;

    FirebaseStorage storage;
    StorageReference storageRef;
    private static String raca;
    private static int sp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_raca);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        raca = extras.getString("RACA");
        sp = extras.getInt("ESPECIE");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(raca);

        }
        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();
        loadItens(raca, sp);

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



    private void loadItens(String raca, int sp){


        try {
            raca = URLEncoder.encode(raca, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        String metodo = "/SP_BUSCAR_RACA?desc=" + raca + "&sp=" + sp;;
        GetMethod g = new GetMethod(metodo, this, new GetMethod.Callback() {
            @Override
            public void run(String result) {

                Raca r = new Gson().fromJson(result, Raca.class);

                if (r != null) {
                    textViewPorte.setText(r.getText_porte());
                    textViewOrigem.setText(r.getText_origem());
                    textViewPesoMedio.setText(r.getPeso_medio());
                    textViewTemperamento.setText(r.getText_temperamento());
                    textViewDoencas.setText(r.getText_doencas_comuns());
                    if (r.getUrl_imagem() != null){
                        loadImage(r.getUrl_imagem());
                    }
                }
            }
        }, "Carregando informações...");
        g.execute();

    }

    private void loadImage(String url){


        StorageReference pathReference = storageRef.child(url);

        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                imageViewRacaHead.setImageURI(uri);



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });
    }



}
