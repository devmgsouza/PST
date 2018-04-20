package meupet.android.soasd.com.br.meupet.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import br.com.soasd.meupet.GerarQRCode;
import br.com.soasd.meupet.Pet;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;
import meupet.android.soasd.com.br.meupet.httpservices.GetSingleMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PostMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PostSingleMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PrivateGetMethod;
import meupet.android.soasd.com.br.meupet.httpservices.UploadImageFirebase;
import meupet.android.soasd.com.br.meupet.httpservices.WriteBitmap;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class PetViewFragment extends BaseFragment {
    private Calendar myCalendar = Calendar.getInstance();
    String myFormat = "dd/MM/yyyy";
    DateFormat df = new SimpleDateFormat(myFormat);

    @BindView(R.id.editTextPetFalescimento)
    EditText editTextPetFalecimento;
    @BindView(R.id.editTextPetNascimento)
    EditText editTextPetNascimento;
    @BindView(R.id.editTextPetNome)
    EditText editTextPetNome;
    @BindView(R.id.checkBoxDataNasc)
    CheckBox checkBoxDataNasc;
    @BindView(R.id.switchPetVivo)
    Switch switchPetVivo;
    @BindView(R.id.switchPetLocalizado)
    Switch switchPetLocalizado;
    @BindView(R.id.spinnerPetRaca)
    Spinner spinnerPetRaca;
    ArrayAdapter adapterPetRaca;
    @BindView(R.id.progressBarQRCode)
    ProgressBar progressBarQRCode;
    @BindView(R.id.imageProfilePetView)
    SimpleDraweeView imageProfile;
    @BindView(R.id.backgroundTopView)
    SimpleDraweeView backgroundTopView;
    @BindView(R.id.imageViewQRCode)
    ImageView imageViewQRCode;
    @BindView(R.id.buttonLoja)
    Button buttonLoja;
    @BindView(R.id.imageButtonDataNascimento)
    ImageButton imageButtonDataNascimento;
    @BindView(R.id.imageButtonDataFalescimento)
    ImageButton imageButtonDataFalescimento;
    @BindView(R.id.textViewSaibaMais)
    TextView textViewSaibaMais;
    @BindView(R.id.imageButtonTouch)
    ImageButton imageButtonTouch;
    private static final int REQUEST_PERMISSIONS_CODE = 128;
    int cropCamMode = 0;
    FirebaseStorage storage;
    StorageReference storageRef;
    private Bitmap imgCacheProfile;
    private Bitmap imgCacheTop;
    private String petGson = "";
    private Pet pet;
    private String idUser;
    public PetViewFragment() {
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
        View view = inflater.inflate(R.layout.fragment_pet_view, container, false);
        ButterKnife.bind(this, view);
        petGson = getArguments().getString("PETGSON");
        int login_mode = getArguments().getInt("LOGIN");
        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }



        pet = new Gson().fromJson(petGson, Pet.class);
        imageProfile.setDrawingCacheEnabled(true);
        loadData(pet);


        ((GeneralActivity)getActivity()).updateToolbarTitle("Informações do Pet");
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewSaibaMais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), RacaFragment.class);
                i.putExtra("RACA", spinnerPetRaca.getSelectedItem().toString());
                i.putExtra("ESPECIE", pet.getFk_familia());
                startActivityForResult(i,0);
            }
        });


        switchPetVivo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTextPetFalecimento.setVisibility(EditText.GONE);
                    switchPetLocalizado.setVisibility(Switch.VISIBLE);
                    imageButtonDataFalescimento.setVisibility(View.GONE);
                } else {
                    editTextPetFalecimento.setVisibility(EditText.VISIBLE);
                    imageButtonDataFalescimento.setVisibility(View.VISIBLE);
                    switchPetLocalizado.setVisibility(Switch.GONE);
                }
            }
        });

        switchPetLocalizado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showtAutoDismissDialog(R.string.searching_title, getResources().getString(R.string.searching_msg), 7000);
                }
            }
        });


        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImg(1);
            }
        });

        backgroundTopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImg(2);
            }
        });

        checkBoxDataNasc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editTextPetNascimento.setEnabled(false);
                    editTextPetNascimento.setText("");
                    imageButtonDataNascimento.setEnabled(false);
                } else {
                    editTextPetNascimento.setEnabled(true);
                    imageButtonDataNascimento.setEnabled(true);
                    editTextPetNascimento.setText(df.format(System.currentTimeMillis()).replace("/",""));
                }
            }
        });

        imageViewQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionToSave()) {
                    saveQRCode("PST_" + pet.getText_nome() + "_QRCODE.PNG");
                    showtAutoDismissDialog(R.string.app_name, "Salvando na galeria...", 1250);
                }
            }
        });


        buttonLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setResult(110);
                getActivity().finish();
            }
        });

        imageButtonDataFalescimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataChange(1);
            }
        });

        imageButtonDataNascimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataChange(0);
            }
        });


        imageButtonTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImg(2);
            }
        });
        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!checkBoxDataNasc.isChecked()) {
                    if (checkData(editTextPetNascimento.getText().toString())) {
                        salvarPet();
                    }
                } else {
                    salvarPet();
                }


                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                try {
                    if (cropCamMode == 1) {
                        imageProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imgCacheProfile = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), resultUri);
                        imageProfile.setImageURI(resultUri);
                        imageProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } else {
                        imgCacheTop = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), resultUri);
                        backgroundTopView.setImageURI(resultUri);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void dataChange(final int value){

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));


                if (value == 1) {
                    editTextPetFalecimento.setText(sdf.format(myCalendar.getTime()).replace("/",""));
                } else  {
                    editTextPetNascimento.setText(sdf.format(myCalendar.getTime()).replace("/", ""));
                }


            }
        };

        DatePickerDialog dataDialog =  new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        dataDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dataDialog.show();
    }

    private void loadSpinner(int especie){
        if (especie == 1) {
            adapterPetRaca = ArrayAdapter.createFromResource(getContext(), R.array.dog_list,
                    android.R.layout.simple_spinner_dropdown_item);

        } else {
            adapterPetRaca = ArrayAdapter.createFromResource(getContext(), R.array.cat_list,
                    android.R.layout.simple_spinner_dropdown_item);
        }
        spinnerPetRaca.setAdapter(adapterPetRaca);
        spinnerPetRaca.setEnabled(false);
        editTextPetNascimento.setText(df.format(System.currentTimeMillis()).replace("/",""));
        editTextPetFalecimento.setText(df.format(System.currentTimeMillis()).replace("/", ""));
    }

    private void loadImage(String url, final int img){


            StorageReference pathReference = storageRef.child(url);

            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (img == 1) {
                        imageProfile.setImageURI(uri);
                        //imageProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } else {
                        backgroundTopView.setImageURI(uri);
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    if (img == 1) {
                        imageProfile.setActualImageResource(R.drawable.ic_nopic);
                    }
                }
            });

    }

    private void cropImg(int type){
        cropCamMode = type;
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("Capturar imagem")
                .start(getContext(), this);


    }

    private void uploadImageFB(final Bitmap b, final String nameFile){
        new UploadImageFirebase(b, nameFile).execute();
    }

    private void loadData(Pet pet){

        if(!(pet.getUrl_foto().length() < 5)){
            loadImage(pet.getUrl_foto(), 1);
        } else {
            imageProfile.setActualImageResource(R.drawable.ic_nopic);
        }

        if(!(pet.getUrl_capa().length() < 5)){
            loadImage(pet.getUrl_capa(), 2);
        } else {
            if (pet.getGenero().equals("Macho")){
                backgroundTopView.setBackground(getContext().getResources().getDrawable(R.drawable.image_background));

            } else {
                backgroundTopView.setBackground(getContext().getResources().getDrawable(R.drawable.image_background_roxo));
            }

        }


        loadSpinner(pet.getFk_familia());
        editTextPetNome.setText(pet.getText_nome());
        if (pet.getData_nascimento().length() < 2){
            checkBoxDataNasc.setChecked(true);
            editTextPetNascimento.setEnabled(false);
            editTextPetNascimento.setText("");
        } else {
            editTextPetNascimento.setEnabled(true);
            editTextPetNascimento.setText(pet.getData_nascimento().replace("/",""));
            checkBoxDataNasc.setChecked(false);

        }
       int adapterPosition = adapterPetRaca.getPosition(pet.getText_descricao());
        spinnerPetRaca.setSelection(adapterPosition);

        if (pet.getPet_vivo() == 1){
            switchPetVivo.setChecked(true);
        } else {
            switchPetVivo.setChecked(false);
            editTextPetFalecimento.setVisibility(View.VISIBLE);
        }

        if (pet.getPet_localizado() == 1) {
            switchPetLocalizado.setChecked(false);
        } else {
            switchPetLocalizado.setChecked(true);
        }
        loadQRCode(pet.getCodigo_validador());
    }

    private void showtAutoDismissDialog(int title, String mensagem, long time){
        TextView msg = new TextView(getContext());
        msg.setText("\n" + mensagem + "\n");
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ico_carimbo)
                .setTitle(title)

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

        handler.postDelayed(runnable, time);
    }

    private void loadQRCode(String codigo_validador){
        progressBarQRCode.setVisibility(View.VISIBLE);
        String metodo = "/SP_GERAR_QRCODE?pet=" + codigo_validador;
        GetSingleMethod get = new GetSingleMethod(metodo, getContext(), new GetSingleMethod.Callback() {
            @Override
            public void run(String result) {
                GerarQRCode qrCode = new Gson().fromJson(result, GerarQRCode.class);

                String content = "Nome: " + qrCode.getText_nome_pet();
                content = content + "\nRaça: " + qrCode.getText_raca();
                content = content + "\nDono: " + qrCode.getText_nome_proprietario();
                content = content + "\nContato: " + qrCode.getText_fone();
                content = content + "\n\nPara mais informações baixe nosso App:";
                content = content + "\nPET Smart Tag";
                content = content + "\nou acesse: ";
                content = content + "http://petsmarttag.com\n e insira o código " + qrCode.getCodigo_validador();
                Bitmap bitmap = getQRCode(content);

                String textDown = qrCode.getCodigo_validador();
                new WriteBitmap(bitmap, pet.getText_nome(), textDown, new WriteBitmap.Callback() {
                    @Override
                    public void run(Bitmap result) {
                        imageViewQRCode.setImageBitmap(result);
                    }
                }, getContext()).execute();


                progressBarQRCode.setVisibility(View.GONE);
            }
        });
        get.execute();
    }

    private Bitmap getQRCode(String content){
        Bitmap bmp = null;
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }


        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bmp;

    }

    public void saveQRCode(String fileName) {

        Bitmap b = Bitmap.createBitmap(imageViewQRCode.getWidth(), imageViewQRCode.getHeight(),  Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(b);
        imageViewQRCode.draw(canvas);
        OutputStream fOut = null;

        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();




        if(isSDSupportedDevice && isSDPresent)
        {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + " /DCIM/PST");
            dir.mkdirs();
            try {
                fOut = new FileOutputStream(dir.getAbsolutePath() +"/"+ fileName);
                b.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(), dir.getAbsolutePath() +"/"+ fileName, fileName , "PST_QR_CODE_" + pet.getText_nome());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
        {

            File dir = new File ( "/sdcard/DCIM/PST");
            dir.mkdirs();
            try {
                fOut = new FileOutputStream( dir.getAbsolutePath() +"/"+ fileName);
                b.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(), dir.getAbsolutePath() +"/"+ fileName, fileName , "PST_QR_CODE_" + pet.getText_nome());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }





    }

    private void updatePet(final Pet updatePet){



            String postGson = new Gson().toJson(updatePet);

            String metodo = "/SP_ATUALIZAR_DADOS_PET";

            new PostSingleMethod(postGson, metodo, getContext(), new PostSingleMethod.Callback() {

                @Override
                public void run(String result) {

                    if (result.equals("Cadastro Pet Atualizado")) {
                        getActivity().setResult(101);
                        if (imgCacheProfile != null) {

                            uploadImageFB(imgCacheProfile, updatePet.getUrl_foto());
                            if (pet.getUrl_foto().length() > 5) {
                                deleteFileFB(pet.getUrl_foto(), 1);
                            }

                        }
                        if (imgCacheTop != null) {

                            uploadImageFB(imgCacheTop, updatePet.getUrl_capa());
                            if (pet.getUrl_capa().length() > 5) {
                                deleteFileFB(pet.getUrl_capa(), 2);
                            }

                        }
                        //showtAutoDismissDialog(R.string.app_name, result, 1500);
                        getActivity().finish();
                    } else {
                        showtAutoDismissDialog(R.string.app_name, result, 1500);
                    }


                }
            }).execute();

    }

    private void deleteFileFB(String url, int param){
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

    private boolean checkData(String s){
        boolean b = false;
        DateFormat df = new SimpleDateFormat ("dd/MM/yyyy");
        df.setLenient (false);
        try {
            df.parse (s);
            b = true;
        } catch (ParseException ex) {
            showtAutoDismissDialog(R.string.app_name, "Insira uma data válida", 1500);
        }
        return b;
    }

    private void salvarPet(){
        Pet p = new Pet();

        if (switchPetVivo.isChecked()) {
            p.setPet_vivo(1);
            p.setData_falecimento("#NULL");
        } else {
            p.setPet_vivo(0);
            p.setData_falecimento(editTextPetFalecimento.getText().toString());
        }

        if (switchPetLocalizado.isChecked()) {
            p.setPet_localizado(0);
        } else {
            p.setPet_localizado(1);
        }
        if (checkBoxDataNasc.isChecked()) {
            p.setData_nascimento("#NULL");
        } else {
            p.setData_nascimento(editTextPetNascimento.getText().toString());
        }
        p.setCodigo_validador(pet.getCodigo_validador());

        if (imgCacheProfile!=null) {
            final String nameFile = "PST/perfil/" + new Date().getTime() + "_" + idUser + ".WEBP";
            p.setUrl_foto(nameFile);
        } else {
            p.setUrl_foto(pet.getUrl_foto());
        }
        if (imgCacheTop!=null) {
            final String nameFile = "PST/capa/" + new Date().getTime() + "_" + idUser + "_capa.WEBP";
            p.setUrl_capa(nameFile);
        } else {
            p.setUrl_capa(pet.getUrl_capa());
        }
        updatePet(p);
    }

    private boolean checkPermissionToSave(){
        boolean retorno = false;
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )

        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);
        } else {
            retorno = true;
        }
        return retorno;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE){
            if (grantResults[0] == 0 && grantResults[1] == 0) {
                saveQRCode("PST_" + pet.getText_nome() + "_QRCODE.PNG");
                showtAutoDismissDialog(R.string.app_name, "Salvando na galeria...", 1250);
            } else {
                createDialogYes(R.string.error, R.string.necessary_permission, R.string.yes);
            }
        }
    }

    private void createDialogYes(int title, int message, int YES){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);

                    }
                }).show();
    }

}
