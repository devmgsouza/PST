package meupet.android.soasd.com.br.meupet.httpservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Marcio on 26/07/2017.
 */

public class BuscarCEP extends AsyncTask<String, Void, String> {
    String cep;
    Context context;
    Callback callback;
    ProgressDialog pdia;
    private static String TAG = "GetMethod";

    public BuscarCEP(String cep, Context context, Callback callback){
        this.cep = cep;
        this.callback = callback;
        pdia = new ProgressDialog(context);
    }

    @Override
    protected String doInBackground(String... params) {

        String url = "https://viacep.com.br/ws/" + cep + "/json";


        String json = "";
        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

            try {
                request.setRequestMethod("GET");
                request.setConnectTimeout(15000);
                request.setReadTimeout(15000);
                request.setRequestProperty("Content-Type", "application/json");
                request.connect();
                int responceCode = request.getResponseCode();


            if (responceCode == HttpURLConnection.HTTP_OK) {

            } else {

            }



               json = getStringFromInputStream(request.getInputStream());

            } finally {
                request.disconnect();
            }

        } catch (IOException ex) {




        }


        return json;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        pdia.setMessage("Pesquisando...");
        pdia.show();

    }

    @Override
    protected void onPostExecute(String resultado){
        callback.run(resultado);
        pdia.dismiss();
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public interface Callback {
        void run(String result);
    }





}
