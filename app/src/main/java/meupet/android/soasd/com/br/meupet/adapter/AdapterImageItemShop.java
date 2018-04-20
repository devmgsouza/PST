package meupet.android.soasd.com.br.meupet.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by SOA - Development on 21/03/2018.
 */

public class AdapterImageItemShop extends RecyclerView.Adapter<AdapterImageItemShop.ImageHolderView>  {

    private List<String> url;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Context context;
    ImageOnClickListener imageOnClickListener;

    public AdapterImageItemShop(Context context, List<String> url, ImageOnClickListener imageOnClickListener) {
        this.url = url;
        this.imageOnClickListener = imageOnClickListener;
        this.context = context;

        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public ImageHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_imageview, parent, false);
        ImageHolderView holder = new ImageHolderView(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageHolderView holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);
        String addr = url.get(position);


        if (!addr.equals("#NULL")) {

            StorageReference pathReference = storageRef.child(addr);
            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                @Override
                public void onSuccess(Uri uri) {
                    holder.imageView.setImageURI(uri);


                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    holder.progressBar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    holder.imageView.setImageResource(R.drawable.ic_nopic);
                    e.printStackTrace();
                }
            });


        } else {
            holder.imageView.setImageResource(R.drawable.ic_nopic);
            holder.progressBar.setVisibility(View.GONE);
        }





    }

    @Override
    public int getItemCount() {
        return this.url != null ? this.url.size():0;
    }




    public interface ImageOnClickListener {
        void onClick(View view, int index);

    }

    public static class ImageHolderView extends RecyclerView.ViewHolder {
        public SimpleDraweeView imageView;
        public ProgressBar progressBar;
        public ImageHolderView(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBarImage);
            imageView = view.findViewById(R.id.imageViewItemShop);
        }

    }
}
