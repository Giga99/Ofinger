package aplikacija.apl.ofinger.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.models.Cloth;
import aplikacija.apl.ofinger.models.Review;
import aplikacija.apl.ofinger.models.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ClothAdapter extends RecyclerView.Adapter<ClothAdapter.ViewHolder> {
    private List<Cloth> cloths, wishCloths;
    private Context context;
    private ItemClicked activity;
    private boolean wishActivity, changed = false;

    public interface ItemClicked{
        void onItemClicked(int index);
    }

    public ClothAdapter(Context context, List<Cloth> list, boolean wishActivity) {
        cloths = list;
        this.context = context;
        activity = (ItemClicked) context;
        this.wishActivity = wishActivity;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTimestamp, tvOwnerName, tvPrice, tvClothName, numOfWishes, numOfReviews;
        ImageView ivCloth, ivWish;
        CircleImageView ivUserProfImage;
        RatingBar ratingBarOverall;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivCloth = itemView.findViewById(R.id.ivCloth);
            ivWish = itemView.findViewById(R.id.ivWish);
            ivUserProfImage = itemView.findViewById(R.id.ivUserProfImage);
            tvClothName = itemView.findViewById(R.id.tvClothName);
            numOfWishes = itemView.findViewById(R.id.numOfWishes);
            numOfReviews = itemView.findViewById(R.id.numOfReviews);
            ratingBarOverall = itemView.findViewById(R.id.ratingBarOverall);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked(cloths.indexOf((Cloth) v.getTag()));
                }
            });

            itemView.setFocusable(true);
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

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(cloths.get(position).getOwnerID());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                holder.tvOwnerName.setText(user.getUsername());
                if(!user.getImageURL().equals("default")) Picasso.get().load(user.getImageURL()).into(holder.ivUserProfImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Izbor prve slike koja je za dato odelo
         */
        Picasso.get().load(cloths.get(position).getClothProfile()).into(holder.ivCloth);

        final String name = cloths.get(position).getName();
        holder.tvClothName.setText(name);

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("sr_Latn_RS", "RS"));
        final String price = format.format(cloths.get(position).getPrice());
        holder.tvPrice.setText(price);

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(cloths.get(position).getTimestamp()));
        String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.tvTimestamp.setText(time);

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("StarsCloth").child(cloths.get(position).getObjectId());
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.numOfReviews.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Wishes");
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(position < cloths.size()) {
                    int num = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child(cloths.get(position).getObjectId()).exists()) num++;
                    }

                    holder.numOfWishes.setText("" + num);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        wishCloths = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String clothID = snapshot.getKey();
                    for (Cloth cloth : ApplicationClass.mainCloths) {
                        if (cloth.getObjectId().equals(clothID)) {
                            wishCloths.add(cloth);
                            break;
                        }
                    }
                }

                ApplicationClass.wishCloths = wishCloths;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        calculateOverall(position, holder.ratingBarOverall);

        if(cloths.get(position).getOwnerID().equals(ApplicationClass.currentUser.getUid())){
            holder.ivWish.setVisibility(View.GONE);
        } else {
            isWish(cloths.get(position).getObjectId(), holder.ivWish);
            holder.ivWish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.ivWish.getTag().equals("notwish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(cloths.get(position).getObjectId()).setValue(true);
                        notifyDataSetChanged();
                    } else if (holder.ivWish.getTag().equals("wish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(cloths.get(position).getObjectId()).removeValue();
                        int i = 0;
                        while(true){
                            if(ApplicationClass.wishCloths.get(i).getObjectId().equals(cloths.get(position).getObjectId())) break;
                            else i++;
                        }
                        ApplicationClass.wishCloths.remove(i);
                        if(wishActivity) {
                            cloths.remove(position);
                        }
                        changed = true;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    /**
     * Racunanje prosecne ocene
     */
    private void calculateOverall(int index, final RatingBar ratingBarOverall){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(index).getObjectId());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float sum = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Review review = snapshot.getValue(Review.class);
                    sum += review.getStars();
                }
                sum /= (float) dataSnapshot.getChildrenCount();

                ratingBarOverall.setRating(sum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return cloths.size();
    }

    private void isWish(final String clothid, final ImageView button){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(clothid).exists()){
                    Drawable img = context.getDrawable( R.drawable.wishlist );
                    button.setBackground(img);
                    button.setTag("wish");
                } else {
                    Drawable img = context.getDrawable( R.drawable.notwishlist );
                    button.setBackground(img);
                    button.setTag("notwish");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
