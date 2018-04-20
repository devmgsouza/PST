package meupet.android.soasd.com.br.meupet.httpservices;




import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.Gson;

import br.com.soasd.meupet.Usuario;


/**
 * Created by Marcio on 29/11/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
      AtualizarToken(FirebaseInstanceId.getInstance().getToken());

    }

     private void AtualizarToken(String token){
     if (!isLoggedIn().equals("#NULL")){
         String metodo = "/SP_ATUALIZAR_TOKEN";
         Usuario usuario = new Usuario();
         usuario.setToken_gcm(token);
         usuario.setFb_id(isLoggedIn());
         String postGson = new Gson().toJson(usuario);
         new PrivatePostMethod(postGson, metodo, this).execute();

     }



    }


    private String isLoggedIn() {
        String retorno = "#NULL";
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (accessToken != null) {
            retorno = accessToken.getUserId();

        } else if (currentUser != null) {
            retorno = currentUser.getUid();

        }
        return retorno;
    }


}
