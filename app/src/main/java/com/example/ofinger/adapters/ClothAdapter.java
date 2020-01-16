package com.example.ofinger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.models.Cloth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ClothAdapter extends RecyclerView.Adapter<ClothAdapter.ViewHolder> {
    private List<Cloth> cloths;
    ItemClicked activity;

    public interface ItemClicked{
        void onItemClicked(int index);
    }

    public ClothAdapter(Context context, List<Cloth> list) {
        cloths = list;
        activity = (ItemClicked) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvClothName, tvOwnerName, tvPrice;
        ImageView ivCloth, ivWish;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            tvClothName = itemView.findViewById(R.id.tvClothName);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivCloth = itemView.findViewById(R.id.ivCloth);
            ivWish = itemView.findViewById(R.id.ivWish);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked(cloths.indexOf((Cloth) v.getTag()));
                }
            });
        }
    }

    @NonNull
    @Override
    public ClothAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);

        return new ClothAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ClothAdapter.ViewHolder holder, final int position) {
        holder.itemView.setTag(cloths.get(position));

        /**
         * Izbor prve slike koja je za dato odelo
         */
        Picasso.get().load(cloths.get(position).getUrls().get(0)).into(holder.ivCloth);

        holder.tvClothName.setText(cloths.get(position).getName());
        holder.tvOwnerName.setText(cloths.get(position).getOwnerUsername());
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("sr_Latn_RS", "RS"));
        String price = format.format(cloths.get(position).getPrice());
        holder.tvPrice.setText(price);

        if(cloths.get(position).getOwnerID().equals(ApplicationClass.currentUser.getUid())){
            holder.ivWish.setVisibility(View.GONE);
        } else {
            isLike(cloths.get(position).getObjectId(), holder.ivWish);
            holder.ivWish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.ivWish.getTag().equals("notwish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(cloths.get(position).getObjectId()).setValue(true);
                    } else if (holder.ivWish.getTag().equals("wish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(cloths.get(position).getObjectId()).removeValue();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cloths.size();
    }

    private void isLike(final String clothid, final ImageView imageView){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(clothid).exists()){
                    imageView.setImageResource(R.drawable.wishlist);
                    imageView.setTag("wish");
                } else {
                    imageView.setImageResource(R.drawable.notwishlist);
                    imageView.setTag("notwish");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
