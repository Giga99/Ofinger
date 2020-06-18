package aplikacija.apl.ofinger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.models.Cloth;

public class ClothSearchAdapter extends RecyclerView.Adapter<ClothSearchAdapter.ViewHolder> {
    private List<Cloth> cloths;
    private Context context;
    private ClothAdapter.ItemClicked activity;

    public interface ItemClicked{
        void onItemClicked(int index);
    }

    public ClothSearchAdapter(Context context, List<Cloth> list) {
        cloths = list;
        this.context = context;
        activity = (ClothAdapter.ItemClicked) context;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cloth_search, parent, false);

        return new ClothSearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(cloths.get(position));

        /**
         * Izbor prve slike koja je za dato odelo
         */
        Picasso.get().load(cloths.get(position).getClothProfile()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return cloths.size();
    }
}
