package com.example.ofinger.mainActivities;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.models.Cloth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    RecyclerView list;
    ClothAdapter adapter;
    LinearLayoutManager manager;
    View view;
    List<Cloth> cloths;
    List<String> followingList;

    ImageButton sort;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        list = view.findViewById(R.id.list);
        list.setHasFixedSize(true);
        manager = new LinearLayoutManager(HomeFragment.this.getActivity());
        //manager = new GridLayoutManager(ListFrag.this.getActivity(), 2, GridLayoutManager.VERTICAL, false);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        list.setLayoutManager(manager);

        cloths = new ArrayList<>();

        adapter = new ClothAdapter(HomeFragment.this.getContext(), cloths);
        list.setAdapter(adapter);

        sort = view.findViewById(R.id.sort);

        getFollowing();

        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(HomeFragment.this.getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.priceDown:
                                readCloth(false, false, false, true);
                                return true;
                            case R.id.priceUp:
                                readCloth(false, false, true, false);
                                return true;
                            case R.id.nameUp:
                                readCloth(true, false, false, false);
                                return true;
                            case R.id.nameDown:
                                readCloth(false, true, false, false);
                                return true;
                            default:
                                return false;

                        }
                    }
                });
                popupMenu.inflate(R.menu.sort_menu);
                popupMenu.show();
            }
        });

        return view;
    }

    void getFollowing(){
        followingList = new ArrayList<>();

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following");
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                readCloth(false, false, false, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Pravljenje liste svih odela za Main stranu
     */
    void readCloth(final boolean nameUp, final boolean nameDown, final boolean priceUp, final boolean priceDown){
        Query query;
        if(nameUp || nameDown) {
            query = FirebaseDatabase.getInstance().getReference("Cloth").orderByChild("name");
        } else if(priceUp || priceDown){
            query = FirebaseDatabase.getInstance().getReference("Cloth").orderByChild("price");
        } else {
            query = FirebaseDatabase.getInstance().getReference("Cloth");
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Cloth> mainCloth = new ArrayList<>();
                cloths.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Cloth cloth = snapshot.getValue(Cloth.class);
                    if(cloth.getOwnerID().equals(ApplicationClass.currentUser.getUid()) && !cloth.isSold()) cloths.add(cloth);
                    else if(!cloth.isSold()) {
                        for (String id : followingList) {
                            if (id.equals(cloth.getOwnerID())) cloths.add(cloth);
                        }
                    }
                    mainCloth.add(cloth);
                }

                if(nameUp || priceUp) Collections.reverse(cloths);

                ApplicationClass.followingCloth = cloths;
                ApplicationClass.mainCloths = mainCloth;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeFragment.this.getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void status(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            status("offline");
        }
    }
}
