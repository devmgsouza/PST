package meupet.android.soasd.com.br.meupet.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import br.com.soasd.meupet.Pet;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.activities.MainActivity;
import meupet.android.soasd.com.br.meupet.httpservices.DeleteMethod;

/**
 * Created by SOA - Development on 17/02/2018.
 */

public class AdapterPets extends RecyclerView.Adapter<AdapterPets.MeusPetsHolderView>{
    private Context context;
    private List<Pet> meusPets;
    private MeusPetsOnClickListener meusPetsOnClickListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;


    public AdapterPets (Context context, List<Pet> meusPets, MeusPetsOnClickListener
            meusPetsOnClickListener){
        this.context = context;
        this.meusPets = meusPets;
        this.meusPetsOnClickListener = meusPetsOnClickListener;
    }



    @Override
    public MeusPetsHolderView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_petlist, parent, false);
        MeusPetsHolderView holder = new MeusPetsHolderView(view);
        storage = FirebaseStorage.getInstance("gs://meupet-d29ed.appspot.com");
        storageRef = storage.getReference();
        return holder;
    }

    @Override
    public void onBindViewHolder(final MeusPetsHolderView holder, final int position) {
            final Pet mp = meusPets.get(position);

            holder.cardTextViewNome.setText(mp.getText_nome());
            holder.cardTextViewRaca.setText(mp.getText_descricao());
            if(mp.getFk_familia() == 1) {
                holder.cardImageEspecie.setImageResource(R.drawable.img_dog);
            } else {
                holder.cardImageEspecie.setImageResource(R.drawable.img_cat);
            }
            if(!(mp.getUrl_capa().length() < 5)){

                StorageReference pathReference = storageRef.child(mp.getUrl_capa());
                pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                    @Override
                    public void onSuccess(Uri uri) {
                        holder.cardBackgroundLayout.setImageURI(uri);
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        holder.cardProgressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.cardBackgroundLayout.setImageResource(R.drawable.image_background);
                        holder.cardBackgroundLayout.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                });
            } else {
                if (mp.getGenero().equals("Macho")) {
                    holder.cardBackgroundLayout.setBackground(context.getResources().getDrawable(R.drawable.image_background));
                } else {
                    holder.cardBackgroundLayout.setBackground(context.getResources().getDrawable(R.drawable.image_background_roxo));
                }


            }
            if(!(mp.getUrl_foto().length() < 5)){
                holder.cardProgressBar.setVisibility(View.VISIBLE);
                StorageReference pathReference = storageRef.child(mp.getUrl_foto());
                pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                    @Override
                    public void onSuccess(Uri uri) {
                        holder.cardImageProfile.setImageURI(uri);

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        holder.cardProgressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.cardImageProfile.setActualImageResource(R.drawable.ic_nopic);
                    }
                });
            }  else {
                holder.cardImageProfile.setActualImageResource(R.drawable.ic_nopic);
            }









        if (meusPetsOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    meusPetsOnClickListener.onClickPet(holder.itemView, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    meusPetsOnClickListener.onLongClick(holder.itemView, position);
                    return false;
                }


            });
            holder.cardImageButtonMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    meusPetsOnClickListener.onClickMenu(holder.cardImageButtonMore, position);
                }
            });
            holder.cardImageButtonHealth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    meusPetsOnClickListener.onClickHealth(holder.cardImageButtonHealth, position);
                }
            });
            holder.cardImageButtonMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    meusPetsOnClickListener.onClickMap(holder.cardImageButtonMap, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return this.meusPets != null ? this.meusPets.size():0;
    }



    public interface MeusPetsOnClickListener {
        void onClickPet(View view, int index);
        void onLongClick(View view,int position);
        void onClickMenu(View view, int index);
        void onClickHealth(View view, int index);
        void onClickMap(View view, int index);
    }


    public static class MeusPetsHolderView extends RecyclerView.ViewHolder {
        public SimpleDraweeView cardBackgroundLayout;
        public SimpleDraweeView cardImageProfile;
        public ProgressBar cardProgressBar;
        public TextView cardTextViewRaca;
        public TextView cardTextViewNome;
        public ImageView cardImageEspecie;
        public ImageButton cardImageButtonMore;
        public ImageButton cardImageButtonHealth;
        public ImageButton cardImageButtonMap;

        public MeusPetsHolderView(View view) {
            super(view);
            cardBackgroundLayout = view.findViewById(R.id.cardBackGroundImg);
            cardImageProfile = view.findViewById(R.id.cardImageProfile);
            cardProgressBar = view.findViewById(R.id.cardProgressBar);
            cardTextViewNome = view.findViewById(R.id.cardTextViewNome);
            cardTextViewRaca = view.findViewById(R.id.cardTextViewRaca);
            cardImageEspecie = view.findViewById(R.id.cardImageEspecie);
            cardImageButtonMore = view.findViewById(R.id.cardImageButtonMore);
            cardImageButtonHealth = view.findViewById(R.id.cardImageButtonHealth);
            cardImageButtonMap = view.findViewById(R.id.cardImageButtonMap);

        }
    }



}
