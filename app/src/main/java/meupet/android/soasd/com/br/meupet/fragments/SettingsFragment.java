package meupet.android.soasd.com.br.meupet.fragments;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.BuildConfig;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    @BindView(R.id.switchFonteSize)
    Switch switchFonteSize;
    @BindView(R.id.buttonSair)
    Button buttonSair;
    @BindView(R.id.textViewVersion)
    TextView textViewVersion;
    @BindView(R.id.buttonLikeFacebook)
    Button buttonLikeFacebook;
    @BindView(R.id.buttonLikeInstagram)
    Button buttonLikeInstagram;
    @BindView(R.id.textViewOurSite)
    TextView textViewOurSite;
    @BindView(R.id.buttonMinhasCompras)
    Button buttonMinhasCompras;

    public static String FACEBOOK_URL = "https://www.facebook.com/petsmarttag";
    public static String FACEBOOK_PAGE_ID = "petsmarttag";
    private int login_mode;
    private String idUser;
    public SettingsFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        login_mode = getArguments().getInt("LOGIN");
        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        ((GeneralActivity)getActivity()).updateToolbarTitle("Configurações");
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        loadSettings();
        textViewVersion.setText(BuildConfig.VERSION_NAME);
        buttonSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        switchFonteSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsModel s = new SettingsModel();
                if (isChecked) {
                    s.setFonteSize(18);
                    s.setUserId(idUser);
                        new LocalDatabase(getContext()).addSetting(s);
                } else {
                    s.setFonteSize(14);
                    s.setUserId(idUser);
                    new LocalDatabase(getContext()).addSetting(s);
                }
            }
        });


        buttonLikeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = getFacebookPageURL();
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);


            }
        });

        buttonLikeInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://instagram.com/_u/" + FACEBOOK_PAGE_ID);
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                likeIng.setPackage("com.instagram.android");

                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/" + FACEBOOK_PAGE_ID)));
                }
            }
        });

        textViewOurSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.petsmarttag.com"));
                startActivity(browserIntent);
            }
        });


        buttonMinhasCompras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), GeneralActivity.class);
                i.putExtra("fragment", 900);
                i.putExtra("LOGIN", login_mode);
                ((AppCompatActivity) getContext()).startActivityForResult(i, 0);
            }
        });

        return view;
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

    private void logout(){
        getActivity().setResult(444);
        getActivity().finish();
    }

    private void loadSettings(){

        SettingsModel m = new LocalDatabase(getContext()).loadSettings();
       if (m != null) {
           if (m.getFonteSize() == 18) {
               switchFonteSize.setChecked(true);
           } else {
               switchFonteSize.setChecked(false);
           }
       }
    }



    public String getFacebookPageURL() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }


}
