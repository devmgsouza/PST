package meupet.android.soasd.com.br.meupet.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import br.com.soasd.meupet.Pet;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.PostMethod;
import meupet.android.soasd.com.br.meupet.httpservices.UploadImageFirebase;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;


import static android.app.Activity.RESULT_OK;


public class CadastroPetFragment extends BaseFragment {
    private Calendar myCalendar = Calendar.getInstance();
    @BindView(R.id.spinnerSpecie)
    Spinner spinnerEspecie;
    ArrayAdapter adapterSpecie;
    @BindView(R.id.spinnerGenero)
    Spinner spinnerGenero;
    ArrayAdapter adapterGenero;
    @BindView(R.id.spinnerRaca)
    Spinner spinnerRaca;
    ArrayAdapter adapterRaca;
    @BindView(R.id.imageViewSpecie)
    ImageView imageViewSpecie;
    @BindView(R.id.checkBoxDataNasc)
    CheckBox checkBoxDataNasc;
    @BindView(R.id.imageProfile)
    SimpleDraweeView imageProfile;
    @BindView(R.id.editTextBday)
    EditText editTextBDay;
    @BindView(R.id.editTextNomePet)
    EditText editTextNomePet;
    @BindView(R.id.buttonAddPic)
    Button buttonAddPic;
    @BindView(R.id.scrollViewCadastroPet)
    ScrollView scrollViewCadastroPet;
    @BindView(R.id.imageButtonNascimentoCalendar)
    ImageButton imageButtonNascimento;
    @BindView(R.id.textViewSaibaMaisCadastro)
    TextView textViewSaibaMaisCadastro;

    String myFormat = "dd/MM/yyyy";
    DateFormat df = new SimpleDateFormat(myFormat);
    FirebaseStorage storage;
    StorageReference storageRef;
    private String idUser = null;
    private  Bitmap imgCacheProfile;

    public CadastroPetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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


        View view = inflater.inflate(R.layout.fragment_cadastro_pet, container, false);
        ButterKnife.bind(this, view);
        ( (GeneralActivity)getActivity()).updateToolbarTitle("Adicionar Pet");
        loadViews();

        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //((GeneralActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ico_close);

        int login_mode = getArguments().getInt("LOGIN");
        String first_access = getArguments().getString("FIRST_ACCESS");

        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        textViewSaibaMaisCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), RacaFragment.class);
                i.putExtra("RACA", spinnerRaca.getSelectedItem().toString());
                i.putExtra("ESPECIE", spinnerEspecie.getSelectedItemPosition()+1);
                startActivityForResult(i,0);
            }
        });

        checkBoxDataNasc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editTextBDay.setEnabled(false);
                    editTextBDay.setText("");
                } else {
                    editTextBDay.setEnabled(true);
                    editTextBDay.setText(df.format(System.currentTimeMillis()).replace("/",""));
                }
            }
        });

        spinnerEspecie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    /*
                    adapterRaca= ArrayAdapter.createFromResource(getContext(), R.array.dog_list,
                            android.R.layout.simple_spinner_dropdown_item);
                    */

                    adapterRaca = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                            new LocalDatabase(getContext()).buscarRacas(1));
                    spinnerRaca.setAdapter(adapterRaca);
                    imageViewSpecie.setImageResource(R.drawable.img_dog);
                } else if ( position == 1) {/*
                    adapterRaca= ArrayAdapter.createFromResource(getContext(), R.array.cat_list,
                            android.R.layout.simple_spinner_dropdown_item);
                            */
                    adapterRaca = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                            new LocalDatabase(getContext()).buscarRacas(2));
                    spinnerRaca.setAdapter(adapterRaca);
                    imageViewSpecie.setImageResource(R.drawable.img_cat);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imageButtonNascimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bdayChange();
            }
        });


        buttonAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImg();
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImg();
            }
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_salvar_registro, menu);
        final MenuItem addItemMenu = menu.findItem(R.id.menuaddpet_save);
        AppCompatButton buttonAdd = null;

        if (addItemMenu != null) {
            buttonAdd = (AppCompatButton) addItemMenu.getActionView();
            SettingsModel m = new LocalDatabase(getContext()).loadSettings();
            if (m!=null) {
                buttonAdd.setTextSize(m.getFonteSize());
                textViewSaibaMaisCadastro.setTextSize(m.getFonteSize());
            }

            buttonAdd.setText(getResources().getString(R.string.save));
            buttonAdd.setBackgroundColor(Color.TRANSPARENT);
            buttonAdd.setAllCaps(false);
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registerPet(imgCacheProfile);



                }
            });

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

    private void loadViews(){
        adapterGenero= ArrayAdapter.createFromResource(getContext(), R.array.gender,
                android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapterGenero);

        adapterSpecie= ArrayAdapter.createFromResource(getContext(), R.array.especie,
                android.R.layout.simple_spinner_dropdown_item);
        spinnerEspecie.setAdapter(adapterSpecie);

        adapterRaca = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                new LocalDatabase(getContext()).buscarRacas(1));

        spinnerRaca.setAdapter(adapterRaca);


        editTextBDay.setText(df.format(System.currentTimeMillis()).replace("/",""));

    }

    private void bdayChange(){

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));


                editTextBDay.setText(sdf.format(myCalendar.getTime()).replace("/",""));

            }
        };

        DatePickerDialog dataDialog =  new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        dataDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dataDialog.show();
    }

    private void cropImg(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(getContext(), this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();


                try {
                    imageProfile.setImageURI(resultUri);
                    imgCacheProfile = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), resultUri);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void registerPet(final Bitmap bm){
        Pet pet = new Pet();

        final String nameFile = "PST/perfil/" + new Date().getTime() + "_" + idUser + ".WEBP";

        pet.setText_nome(editTextNomePet.getText().toString());
        if (checkBoxDataNasc.isChecked()) {
            pet.setData_nascimento("#null");
        } else {
            pet.setData_nascimento(editTextBDay.getText().toString());
        }



        pet.setGenero(spinnerGenero.getSelectedItem().toString());
        pet.setFk_familia(spinnerEspecie.getSelectedItemPosition() + 1);
        pet.setText_descricao(spinnerRaca.getSelectedItem().toString());


        String metodo = "/SP_REGISTRAR_PET?id=" + idUser;
        if (bm != null) {
            pet.setUrl_foto(nameFile);
        }
        String postGson = new Gson().toJson(pet);
       if (TextUtils.isEmpty(editTextNomePet.getText().toString().trim())){
           AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                   .setTitle(R.string.ops)
                   .setMessage(R.string.name_requerid)
                   .setPositiveButton(R.string.understand, null);
           dialog.show();
       } else {
            new PostMethod(postGson, metodo, getContext(), new PostMethod.Callback() {
                @Override
                public void run(String result) {
                   if (result.equals("PET REGISTRADO")) {
                        if (imgCacheProfile != null) {
                            uploadImageFB(bm, nameFile);
                        }

                        getActivity().setResult(101);

                       clearActivity();

                       showDialogYestNo(result);

                   } else {
                       showtAutoDismissDialog(getResources().getString(R.string.error_pet_register));

                   }




                }
            }, "Cadastrando...").execute();
       }

    }

    private void uploadImageFB(Bitmap b, String nameFile){

        new UploadImageFirebase(b, nameFile).execute();


    }


    private void clearActivity(){

        editTextNomePet.setText("");
        editTextBDay.setText("");
        editTextBDay.setEnabled(true);
        imgCacheProfile = null;
        checkBoxDataNasc.setChecked(false);
        imageProfile.setActualImageResource(R.drawable.img_catdog);
        imageProfile.destroyDrawingCache();
        editTextBDay.setText(df.format(System.currentTimeMillis()).replace("/",""));
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        scrollViewCadastroPet.fullScroll(ScrollView.FOCUS_UP);

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

    private void showDialogYestNo(String mensagem){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        TextView msg = new TextView(getContext());
        msg.setText("\n" + getResources().getString(R.string.continue_cadastro) + "\n");
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        dialog.setView(msg)
                .setIcon(R.drawable.ico_carimbo)
                .setTitle(mensagem)
                .setPositiveButton(R.string.yes, null).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        dialog.show();
    }

}
