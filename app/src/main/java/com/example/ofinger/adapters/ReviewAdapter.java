package com.example.ofinger.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ofinger.R;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Review;
import com.example.ofinger.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private List<Review> reviews;
    Activity context;

    public ReviewAdapter(Activity context, List<Review> list){
        reviews = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_review_layout, parent, false);
        return new ReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReviewAdapter.ViewHolder holder, final int position) {
        holder.itemView.setTag(reviews.get(position));

        holder.ownerStars.setRating(reviews.get(position).getStars());
        holder.ownerReview.setText(reviews.get(position).getText());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(user.getId().equals(reviews.get(position).getUserId())) {
                        holder.ownerUsername.setText(user.getUsername());
                        if(!context.isDestroyed()) Glide.with(context).load(user.getImageURL()).into(holder.ivUserImage);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.ownerUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("profile", "yes");
                intent.putExtra("profileid", reviews.get(position).getUserId());
                context.startActivity(intent);
            }
        });

        holder.ivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("profile", "yes");
                intent.putExtra("profileid", reviews.get(position).getUserId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView ownerUsername, ownerReview;
        ImageView ivUserImage;
        RatingBar ownerStars;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            ownerUsername = itemView.findViewById(R.id.ownerUsername);
            ownerReview = itemView.findViewById(R.id.ownerReview);
            ivUserImage = itemView.findViewById(R.id.ivUserImage);
            ownerStars = itemView.findViewById(R.id.ownerStars);
        }
    }
}
