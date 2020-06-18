package aplikacija.apl.ofinger.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.mainActivities.MainActivity;
import aplikacija.apl.ofinger.models.Review;
import aplikacija.apl.ofinger.models.User;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private List<Review> reviews;
    private Activity context;
    private String type, id;
    private int index;

    public ReviewAdapter(Activity context, List<Review> list, String type, int index, String id){
        reviews = list;
        this.context = context;
        this.type = type;
        this.index = index;
        this.id = id;
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
                    if(getItemCount() > 0) {
                        if (user.getId().equals(reviews.get(position).getUserId())) {
                            holder.ownerUsername.setText(user.getUsername());
                            if (!context.isDestroyed() && !user.getImageURL().equals("default")) Glide.with(context).load(user.getImageURL()).into(holder.ivUserImage);
                            break;
                        }
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

        if(!reviews.get(position).getUserId().equals(ApplicationClass.currentUser.getUid())) holder.deleteReview.setVisibility(View.GONE);

        holder.deleteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Brisanje ocene");
                builder.setMessage("Da li ste sigurni da zelite da izbrisete ovu ocenu?");

                builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(type.equals("cloth")) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(index).getObjectId());
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Review review = snapshot.getValue(Review.class);
                                        if (review.getUserId().equals(ApplicationClass.currentUser.getUid())) {
                                            snapshot.getRef().removeValue();
                                            notifyDataSetChanged();
                                            reviews.remove(position);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else if (type.equals("user")){
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("StarsUsers").child(id);
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Review review = snapshot.getValue(Review.class);
                                        if (review.getUserId().equals(ApplicationClass.currentUser.getUid())) {
                                            snapshot.getRef().removeValue();
                                            notifyDataSetChanged();
                                            reviews.remove(position);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });

                builder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView ownerUsername, ownerReview;
        ImageView ivUserImage, deleteReview;
        RatingBar ownerStars;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            deleteReview = itemView.findViewById(R.id.deleteReview);
            ownerUsername = itemView.findViewById(R.id.ownerUsername);
            ownerReview = itemView.findViewById(R.id.ownerReview);
            ivUserImage = itemView.findViewById(R.id.ivUserImage);
            ownerStars = itemView.findViewById(R.id.ownerStars);
        }
    }
}
