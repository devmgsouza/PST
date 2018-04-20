package meupet.android.soasd.com.br.meupet.httpservices;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.com.soasd.meupet.RegistroLeitura;
import meupet.android.soasd.com.br.meupet.R;

import meupet.android.soasd.com.br.meupet.activities.Login;



/**
 * Created by Marcio on 29/11/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        if (remoteMessage.getData().size() > 0) {
        }
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getBody());
        }

    }



    private void sendNotification(String body, String contents) {

        PendingIntent pendingIntent = null;
        String codigo_validador = contents.substring(contents.length()-6);
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (!isLoggedIn().equals("#NULL")) {

            String metodo = "/SP_BUSCAR_LOCALIZACAO?codigo=" + codigo_validador + "&id=" + isLoggedIn();

            try {
                String result = new PrivateGetMethod(metodo, this).execute().get();

                Type listType = new TypeToken<ArrayList<RegistroLeitura>>() {
                }.getType();
                List<RegistroLeitura> leituras = new Gson().fromJson(result, listType);
                if (leituras.size() > 0) {
                    RegistroLeitura l = leituras.get(0);
                    String label = "(Uma leiturada TAG foi realizada no dia " + l.getData_registro()
                            + ", Bem aqui!)";
                    String latlon = l.getLatitude() + "," + l.getLongitude();
                    String uriString = "geo:0,0?q=" + latlon + label;
                    Uri uri = Uri.parse(uriString);
                    intent = new Intent(android.content.Intent.ACTION_VIEW, uri);

                }
            } catch (InterruptedException e) {
                intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } catch (ExecutionException e) {
                intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            startActivity(intent);

        } else {
            intent = new Intent(getApplicationContext(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(),  "default")
                .setSmallIcon(R.mipmap.st_ico)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(body)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setLights(Color.BLUE, 2000, 2000)
                .setContentIntent(pendingIntent);


        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);


        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
        wl.acquire(10000);
        PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

        wl_cpu.acquire(10000);



        Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(this, notificationSound);
        ringtone.play();


        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(35352, notifBuilder.build());

    }


    private String isLoggedIn() {
        String retorno = "#NULL";
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (accessToken != null) {
            retorno = accessToken.getToken();
        } else if (currentUser != null) {
            retorno = currentUser.getUid();
        }
        return retorno;
    }






}
