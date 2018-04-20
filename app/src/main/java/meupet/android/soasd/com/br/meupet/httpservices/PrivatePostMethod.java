package meupet.android.soasd.com.br.meupet.httpservices;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by Marcio on 22/09/2017.
 */

public class PrivatePostMethod extends AsyncTask<String, Void, String> {
    String postGson;
    String metodo;
    Context context;
    public PrivatePostMethod(String postGson, String metodo, Context context) {
        this.postGson = postGson;
        this.metodo = metodo;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {

        String url = "https://ec2-18-231-108-89.sa-east-1.compute.amazonaws.com:8443/pst/rest/resources" + metodo;

        String resultado = "";

        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

            try {
                // Define que a conexão pode enviar informações e obtê-las de volta:
                request.setDoOutput(true);
                request.setDoInput(true);
                request.setReadTimeout(15000);
                request.setConnectTimeout(15000);
                // Define o content-type:
                request.setRequestProperty("Content-Type", "application/json");
                // Define o método da requisição:
                request.setRequestMethod("POST");
                // Conecta na URL:
                request.connect();
                //Define os dados a serem enviados através do POST
                DataOutputStream dos = new DataOutputStream(request.getOutputStream());
                dos.write(postGson.getBytes());
                InputStream in = new BufferedInputStream(request.getInputStream());
                //Recebe o retorno do POST
                resultado = getStringFromInputStream(in);

                //Caso houver Array, converte-la após o retorno
                //JsonArray array = new JsonParser().parse(result).getAsJsonArray();

            } finally {
                request.disconnect();
            }

        } catch (IOException ex) {

        }
        return resultado;
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
