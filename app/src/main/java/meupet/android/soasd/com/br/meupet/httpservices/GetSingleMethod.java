package meupet.android.soasd.com.br.meupet.httpservices;

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

public class GetSingleMethod extends AsyncTask<String, Void, String> {
    String parametro;
    Context context;
    Callback callback;


    public GetSingleMethod(String parametro, Context context, Callback callback){
        this.parametro = parametro;
        this.callback = callback;

    }

    @Override
    protected String doInBackground(String... params) {

        String url = "https://ec2-18-231-108-89.sa-east-1.compute.amazonaws.com:8443/pst/rest/resources" + parametro;
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


    }

    @Override
    protected void onPostExecute(String resultado){
        callback.run(resultado);

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
