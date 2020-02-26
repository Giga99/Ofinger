package com.example.ofinger.mainActivities;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.ofinger.adapters.UserAdapter;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    private RecyclerView searchClothList;
    private ClothAdapter clothAdapter;
    private List<Cloth> cloths;

    private RecyclerView searchUsersList;
    private UserAdapter userAdapter;
    private List<User> users;

    private EditText etSearch;
    private ImageButton ivSearchCloth, ivSearchUsers;

    private SpeedDialView speedDial;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        ivSearchCloth = view.findViewById(R.id.ivSearchCloth);
        ivSearchUsers = view.findViewById(R.id.ivSearchUsers);

        speedDial = view.findViewById(R.id.speedDial);

        searchClothList = view.findViewById(R.id.searchClothList);
        searchClothList.setHasFixedSize(true);
        LinearLayoutManager searchClothManager = new LinearLayoutManager(SearchFragment.this.getContext());
        searchClothManager.setReverseLayout(true);
        searchClothManager.setStackFromEnd(true);
        searchClothList.setLayoutManager(searchClothManager);
        cloths = new ArrayList<>();
        clothAdapter = new ClothAdapter(SearchFragment.this.getContext(), cloths);
        searchClothList.setAdapter(clothAdapter);

        searchUsersList = view.findViewById(R.id.searchUsersList);
        searchUsersList.setHasFixedSize(true);
        searchUsersList.setLayoutManager(new LinearLayoutManager(SearchFragment.this.getContext()));
        users = new ArrayList<>();
        userAdapter = new UserAdapter(SearchFragment.this.getContext(), users);
        searchUsersList.setAdapter(userAdapter);

        ivSearchCloth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUsersList.setVisibility(View.GONE);
                searchClothList.setVisibility(View.VISIBLE);
            }
        });

        ivSearchUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUsersList.setVisibility(View.VISIBLE);
                searchClothList.setVisibility(View.GONE);
            }
        });

        readCloth(false, false, false, false);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchCloth(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readUsers();
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        speedDial.addActionItem(new SpeedDialActionItem.Builder(R.id.sort, R.drawable.sort).create());
        speedDial.addActionItem(new SpeedDialActionItem.Builder(R.id.category, R.drawable.category).create());

        speedDial.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()){
                    case R.id.sort:
                        PopupMenu popupMenu = new PopupMenu(SearchFragment.this.getContext(), getView().findViewById(actionItem.getId()));
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
                        break;
                    case R.id.category:
                        break;
                }
                return true;
            }
        });

        return view;
    }

    private void searchCloth(final String s){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cloth");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cloths.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Cloth cloth = snapshot.getValue(Cloth.class);
                    if(cloth.getSearchName().contains(s.toLowerCase())) cloths.add(cloth);
                }

                clothAdapter.notifyDataSetChanged();
                ApplicationClass.searchCloth = cloths;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readCloth(final boolean nameUp, final boolean nameDown, final boolean priceUp, final boolean priceDown){
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
                if(etSearch.getText().toString().equals("")){
                    cloths.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Cloth cloth = snapshot.getValue(Cloth.class);
                        cloths.add(cloth);
                    }

                    if(nameUp || priceUp) Collections.reverse(cloths);

                    clothAdapter.notifyDataSetChanged();
                    ApplicationClass.searchCloth = cloths;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SearchFragment.this.getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchUser(String s){
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("searchName").startAt(s).endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(!user.getId().equals(ApplicationClass.currentUser.getUid()) && !user.getEmail().equals("Gost")) users.add(user);
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(etSearch.getText().toString().equals("")){
                    users.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        if(!user.getId().equals(ApplicationClass.currentUser.getUid()) && !user.getEmail().equals("Gost")) users.add(user);
                    }

                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkTypingStatus (String typing){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
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
        checkTypingStatus("noOne");
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }


}
