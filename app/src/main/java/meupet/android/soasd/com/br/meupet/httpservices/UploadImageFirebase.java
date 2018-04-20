package meupet.android.soasd.com.br.meupet.httpservices;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Created by SOA - Development on 19/02/2018.
 */

public class UploadImageFirebase extends AsyncTask<String, Void, String> {
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Bitmap b = null;
    private String nameFile;

    public UploadImageFirebase(Bitmap b, String nameFile){
        this.b = b;
        this.nameFile = nameFile;
        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();
    }


    @Override
    protected String doInBackground(String... strings) {

        StorageReference mountainsRef = storageRef.child(nameFile);
        Double newWitdh = b.getWidth() * 0.30;
        Double newHeight = b.getHeight() * 0.30;
        int w = newWitdh.intValue();
        int h = newHeight.intValue();

        Bitmap bitmap = Bitmap.createScaledBitmap(b, w, h, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, baos);




        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });


        return null;
    }
}


