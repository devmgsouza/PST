package meupet.android.soasd.com.br.meupet.httpservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by Marcio on 26/07/2017.
 */

public class PrivateGetMethod extends AsyncTask<String, Void, String> {
    String parametro;
    ProgressDialog pdia;
    Context context;


    public PrivateGetMethod(String parametro, Context context){
        this.parametro = parametro;
        this.context = context;
    }


    @Override
    protected String doInBackground(String... params) {
        parametro = parametro.replace(" ","%20");

        String url = "https://ec2-18-231-108-89.sa-east-1.compute.amazonaws.com:8443/pst/rest/resources" + parametro;

        String json = "";

        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

            try {
                request.setRequestMethod("GET");
                request.setConnectTimeout(5000);
                request.setReadTimeout(5000);
                request.connect();
                InputStream is = request.getInputStream();

                json = getStringFromInputStream(is);
                is.close();

            } finally {
                request.disconnect();
            }

        } catch (IOException ex) {


            json = "ERRO";
        }


        return json;
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







}
