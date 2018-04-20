package meupet.android.soasd.com.br.meupet.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import br.com.soasd.meupet.BuscarPet;
import br.com.soasd.meupet.Health;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.GeneralActivity;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.PostMethod;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class HealthFragment extends Fragment {
    @BindView(R.id.spinnerHealth)
    Spinner spinnerHealth;
    ArrayAdapter spinnerAdapter;
    @BindView(R.id.editTextHealthDescricao)
    EditText editTextHealthDescricao;
    @BindView(R.id.editTextDataAplicacao)
    EditText editTextDataAplicacao;
    @BindView(R.id.editTextDataAplicacaoFutura)
    EditText editTextDataAplicacaoFutura;
    @BindView(R.id.imageButtonCalendarAplicacao)
    ImageButton imageButtonCalendarAplicacao;
    @BindView(R.id.imageButtonCalendarReaplic)
    ImageButton imageButtonCalendarReaplic;
    @BindView(R.id.editTextObs)
    EditText editTextObs;
    @BindView(R.id.imageButtonReminder)
    ImageButton imageButtonReminder;

    private Calendar myCalendar = Calendar.getInstance();
    String myFormat = "dd/MM/yyyy";
    DateFormat df = new SimpleDateFormat(myFormat);
    private String petGson;
    private BuscarPet pet;
    static String hGson;
    Health healthUpdate;
    private String idUser;
    static int HEALTH_UPDATE;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public HealthFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        ButterKnife.bind(this, view);
        ((GeneralActivity)getActivity()).updateToolbarTitle("Adicionar cuidado");
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((GeneralActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        petGson = getArguments().getString("PETGSON");
        pet = new Gson().fromJson(petGson, BuscarPet.class);
        HEALTH_UPDATE = getArguments().getInt("HEALTH_UPDATE");
        int login_mode = getArguments().getInt("LOGIN");
        if (login_mode == 1) {
            idUser = AccessToken.getCurrentAccessToken().getUserId();
        } else {
            idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        loadSpinner();

        if (HEALTH_UPDATE == 1) {
            hGson = getArguments().getString("HEALTH");
            healthUpdate = new Gson().fromJson(hGson, Health.class);
            loadUpdate(healthUpdate);
        }




        imageButtonCalendarAplicacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataChangeAplicacao();
            }
        });

        imageButtonCalendarReaplic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataChangeReaplicacao();
            }
        });

        imageButtonReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkData(editTextDataAplicacaoFutura.getText().toString())){
                    String d = editTextDataAplicacaoFutura.getText().toString() + " 08:00:00";
                    try {
                        Date date = df.parse(d);

                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.getTime())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.getTime())
                                .putExtra(CalendarContract.Events.TITLE, spinnerHealth.getSelectedItem().toString())
                                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);





                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                    catch (ParseException e) {
                            e.printStackTrace();
                        }

                }
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
            }
            buttonAdd.setText(getResources().getString(R.string.save));
            buttonAdd.setBackgroundColor(Color.TRANSPARENT);
            buttonAdd.setAllCaps(false);
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        addCuidado(HEALTH_UPDATE);
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

    private void dataChangeAplicacao(){

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));


                editTextDataAplicacao.setText(sdf.format(myCalendar.getTime()).replace("/",""));

            }
        };

        DatePickerDialog dataDialog =  new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        dataDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dataDialog.show();
    }

    private void dataChangeReaplicacao(){

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));


                editTextDataAplicacaoFutura.setText(sdf.format(myCalendar.getTime()).replace("/",""));

            }
        };

        DatePickerDialog dataDialog =  new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        dataDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dataDialog.show();
    }

    private void loadSpinner(){
        spinnerAdapter= ArrayAdapter.createFromResource(getContext(), R.array.health,
                android.R.layout.simple_spinner_dropdown_item);
        spinnerHealth.setAdapter(spinnerAdapter);
        editTextDataAplicacao.setText(df.format(System.currentTimeMillis()).replace("/",""));
    }

    private void addCuidado(int action){
        Health h = new Health();
        h.setCodigo_validador(pet.getCodigo_validador());
        h.setText_cuidado(spinnerHealth.getSelectedItem().toString());
        h.setText_descricao(editTextHealthDescricao.getText().toString());
        h.setData_aplicacao(editTextDataAplicacao.getText().toString());
        h.setData_reaplicacao(editTextDataAplicacaoFutura.getText().toString());
        h.setText_obs(editTextObs.getText().toString());
        if (h.getText_descricao().length() < 1) {
            showtAutoDismissDialog("Insira uma descrição antes de salvar");
        } else {
            if (checkData(h.getData_aplicacao())) {

                if (h.getData_reaplicacao().contains("__")) {
                    h.setData_reaplicacao("#null");
                    saveCuidado(h, action);
                } else {
                    if (checkData(h.getData_reaplicacao())){
                        saveCuidado(h, action);
                    }
                }
            }
        }
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


    private boolean checkData(String s){
        boolean b = false;
        DateFormat df = new SimpleDateFormat ("dd/MM/yyyy");
        df.setLenient (false);
        try {
            df.parse (s);
            b = true;
        } catch (ParseException ex) {
            showtAutoDismissDialog("Insira uma data válida");
        }
        return b;
    }


    private void saveCuidado(Health h, int action) {

        String metodo;

        if (action == 0) {
            metodo = "/SP_REGISTRAR_CUIDADO";
        } else {
            h.setPk_health(healthUpdate.getPk_health());
            metodo = "/SP_ATUALIZAR_CUIDADO";
        }

        String postGson = new Gson().toJson(h);
        PostMethod p = new PostMethod(postGson, metodo, getContext(), new PostMethod.Callback() {
            @Override
            public void run(String result) {
                showtAutoDismissDialog(result);
                if (result.equals("CUIDADO REGISTRADO") || result.equals("CUIDADO ATUALIZADO")) {
                    getActivity().setResult(501);
                    clearFields();
                }
            }
        }, "Salvando...");
        p.execute();
    }

    private void clearFields(){
        editTextDataAplicacao.setText("");
        editTextDataAplicacaoFutura.setText("");
        editTextHealthDescricao.setText("");
        editTextObs.setText("");
    }

    private void loadUpdate(Health h) {
        editTextObs.setText(h.getText_obs());
        editTextHealthDescricao.setText(h.getText_descricao());
        editTextDataAplicacao.setText(h.getData_aplicacao().replace("/", ""));
        if (!(h.getData_reaplicacao().equals("#null"))) {
            editTextDataAplicacaoFutura.setText(h.getData_reaplicacao().replace("/", ""));
        }
        int adapterPosition = spinnerAdapter.getPosition(h.getText_cuidado());
        spinnerHealth.setSelection(adapterPosition);

    }


}
