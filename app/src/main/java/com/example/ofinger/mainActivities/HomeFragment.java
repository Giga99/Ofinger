package com.example.ofinger.mainActivities;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
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
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private RecyclerView list;
    private ClothAdapter adapter;
    private LinearLayoutManager manager;
    private View view;
    private List<Cloth> cloths;
    private List<String> followingList;

    private SpeedDialView speedDial;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        ApplicationClass.categoryList.clear();
        ApplicationClass.subCategoryList.clear();

        list = view.findViewById(R.id.list);
        list.setHasFixedSize(true);
        manager = new LinearLayoutManager(HomeFragment.this.getActivity());
        //manager = new GridLayoutManager(ListFrag.this.getActivity(), 2, GridLayoutManager.VERTICAL, false);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        list.setLayoutManager(manager);

        speedDial = view.findViewById(R.id.speedDial);

        cloths = new ArrayList<>();

        if(!HomeFragment.this.getActivity().isDestroyed()) {
            adapter = new ClothAdapter(HomeFragment.this.getContext(), cloths);
            list.setAdapter(adapter);
        }

        getFollowing();

        speedDial.addActionItem(new SpeedDialActionItem.Builder(R.id.sort, R.drawable.sort).create());
        speedDial.addActionItem(new SpeedDialActionItem.Builder(R.id.category, R.drawable.category).create());

        speedDial.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()){
                    case R.id.sort:
                        PopupMenu popupMenu = new PopupMenu(HomeFragment.this.getContext(), getView().findViewById(actionItem.getId()));
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
                        chooseCategory();
                        break;
                }
                return true;
            }
        });

        return view;
    }

    /**
     * Izbor kategorije dijaalog
     */
    private void chooseCategory() {
        View view = LayoutInflater.from(HomeFragment.this.getContext()).inflate(R.layout.dialog_category, null);
        final CheckBox manCloth = view.findViewById(R.id.manCloth);
        final CheckBox womanCloth = view.findViewById(R.id.womanCloth);
        final CheckBox manShoes = view.findViewById(R.id.manShoes);
        final CheckBox womanShoes = view.findViewById(R.id.womanShoes);
        final CheckBox accessories = view.findViewById(R.id.accessories);
        final TextView headerSubCategory = view.findViewById(R.id.headerSubCategory);
        final LinearLayout subcategoryManCloth = view.findViewById(R.id.subcategoryManCloth);
        final LinearLayout subcategoryWomanCloth = view.findViewById(R.id.subcategoryWomanCloth);
        final LinearLayout subcategoryManShoes = view.findViewById(R.id.subcategoryManShoes);
        final LinearLayout subcategoryWomanShoes= view.findViewById(R.id.subcategoryWomanShoes);
        final LinearLayout subcategoryAccessories = view.findViewById(R.id.subcategoryAccessories);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        final CheckBox manClothTshirt = view.findViewById(R.id.manClothTshirt);
        final CheckBox manClothTracksuit = view.findViewById(R.id.manClothTracksuit);
        final CheckBox manClothJeans = view.findViewById(R.id.manClothJeans);
        final CheckBox manClothPants = view.findViewById(R.id.manClothPants);
        final CheckBox manClothHoodies = view.findViewById(R.id.manClothHoodies);
        final CheckBox manClothShorts = view.findViewById(R.id.manClothShorts);
        final CheckBox manClothJackets = view.findViewById(R.id.manClothJackets);
        final CheckBox manClothShirting = view.findViewById(R.id.manClothShirting);
        final CheckBox manClothSweaters = view.findViewById(R.id.manClothSweaters);
        final CheckBox manClothCoat = view.findViewById(R.id.manClothCoat);
        final CheckBox manClothSports = view.findViewById(R.id.manClothSports);
        final CheckBox manClothSuit = view.findViewById(R.id.manClothSuit);
        final CheckBox manClothVest = view.findViewById(R.id.manClothVest);
        final CheckBox manClothUnderwear = view.findViewById(R.id.manClothUnderwear);
        final CheckBox manClothTrunks = view.findViewById(R.id.manClothTrunks);
        final CheckBox manClothPajamas = view.findViewById(R.id.manClothPajamas);
        final CheckBox manClothOther = view.findViewById(R.id.manClothOther);

        final CheckBox womanClothTshirt = view.findViewById(R.id.womanClothTshirt);
        final CheckBox womanClothDresses = view.findViewById(R.id.womanClothDresses);
        final CheckBox womanClothSkirts = view.findViewById(R.id.womanClothSkirts);
        final CheckBox womanClothOveralls = view.findViewById(R.id.womanClothOveralls);
        final CheckBox womanClothTracksuit = view.findViewById(R.id.womanClothTracksuit);
        final CheckBox womanClothJeans = view.findViewById(R.id.womanClothJeans);
        final CheckBox womanClothPants = view.findViewById(R.id.womanClothPants);
        final CheckBox womanClothBlouses = view.findViewById(R.id.womanClothBlouses);
        final CheckBox womanClothTunics = view.findViewById(R.id.womanClothTunics);
        final CheckBox womanClothWedding = view.findViewById(R.id.womanClothWedding);
        final CheckBox womanClothTights = view.findViewById(R.id.womanClothTights);
        final CheckBox womanClothHoodies = view.findViewById(R.id.womanClothHoodies);
        final CheckBox womanClothShorts = view.findViewById(R.id.womanClothShorts);
        final CheckBox womanClothJackets = view.findViewById(R.id.womanClothJackets);
        final CheckBox womanClothShirting = view.findViewById(R.id.womanClothShirting);
        final CheckBox womanClothSweaters = view.findViewById(R.id.womanClothSweaters);
        final CheckBox womanClothCoat = view.findViewById(R.id.womanClothCoat);
        final CheckBox womanClothSports = view.findViewById(R.id.womanClothSports);
        final CheckBox womanClothSuit = view.findViewById(R.id.womanClothSuit);
        final CheckBox womanClothVest = view.findViewById(R.id.womanClothVest);
        final CheckBox womanClothUnderwear = view.findViewById(R.id.womanClothUnderwear);
        final CheckBox womanClothTrunks = view.findViewById(R.id.womanClothTrunks);
        final CheckBox womanClothPajamas = view.findViewById(R.id.womanClothPajamas);
        final CheckBox womanClothOther = view.findViewById(R.id.womanClothOther);

        final CheckBox manShoesSneakers = view.findViewById(R.id.manShoesSneakers);
        final CheckBox manShoesShoes = view.findViewById(R.id.manShoesShoes);
        final CheckBox manShoesBoots = view.findViewById(R.id.manShoesBoots);
        final CheckBox manShoesSlippers = view.findViewById(R.id.manShoesSlippers);
        final CheckBox manShoesSandals = view.findViewById(R.id.manShoesSandals);
        final CheckBox manShoesOther = view.findViewById(R.id.manShoesOther);

        final CheckBox womanShoesSneakers = view.findViewById(R.id.womanShoesSneakers);
        final CheckBox womanShoesShoes = view.findViewById(R.id.womanShoesShoes);
        final CheckBox womanShoesBoots = view.findViewById(R.id.womanShoesBoots);
        final CheckBox womanShoesSlippers = view.findViewById(R.id.womanShoesSlippers);
        final CheckBox womanShoesSandals = view.findViewById(R.id.womanShoesSandals);
        final CheckBox womanShoesBallet = view.findViewById(R.id.womanShoesBallet);
        final CheckBox womanShoesOther = view.findViewById(R.id.womanShoesOther);

        final CheckBox accessoriesBracelets = view.findViewById(R.id.accessoriesBracelets);
        final CheckBox accessoriesWatch = view.findViewById(R.id.accessoriesWatch);
        final CheckBox accessoriesNecklaces = view.findViewById(R.id.accessoriesNecklaces);
        final CheckBox accessoriesRings = view.findViewById(R.id.accessoriesRings);
        final CheckBox accessoriesEarrings = view.findViewById(R.id.accessoriesEarrings);
        final CheckBox accessoriesGlasses = view.findViewById(R.id.accessoriesGlasses);
        final CheckBox accessoriesOther = view.findViewById(R.id.accessoriesOther);

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeFragment.this.getContext());
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                readCloth(false, false, false, false);
            }
        });

        /**
         * Provera da li je vec izabrana kategorija
         */
        for(String category : ApplicationClass.categoryList){
            if(category.equals("manCloth")){
                manCloth.setChecked(true);
                headerSubCategory.setVisibility(View.VISIBLE);
                subcategoryManCloth.setVisibility(View.VISIBLE);
            }
            if(category.equals("womanCloth")){
                womanCloth.setChecked(true);
                headerSubCategory.setVisibility(View.VISIBLE);
                subcategoryWomanCloth.setVisibility(View.VISIBLE);
            }
            if(category.equals("manShoes")){
                manShoes.setChecked(true);
                headerSubCategory.setVisibility(View.VISIBLE);
                subcategoryManShoes.setVisibility(View.VISIBLE);
            }
            if(category.equals("womanShoes")){
                womanShoes.setChecked(true);
                headerSubCategory.setVisibility(View.VISIBLE);
                subcategoryWomanShoes.setVisibility(View.VISIBLE);
            }
            if(category.equals("accessories")){
                accessories.setChecked(true);
                headerSubCategory.setVisibility(View.VISIBLE);
                subcategoryAccessories.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Provera da li je vec izabrana potkategorija
         */
        for(String category : ApplicationClass.subCategoryList){
            if(category.equals("manTshirt")) manClothTshirt.setChecked(true);
            if(category.equals("manTracksuit")) manClothTracksuit.setChecked(true);
            if(category.equals("manJeans")) manClothJeans.setChecked(true);
            if(category.equals("manPants")) manClothPants.setChecked(true);
            if(category.equals("manHoodies")) manClothHoodies.setChecked(true);
            if(category.equals("manShorts")) manClothShorts.setChecked(true);
            if(category.equals("manJackets")) manClothJackets.setChecked(true);
            if(category.equals("manShirting")) manClothShirting.setChecked(true);
            if(category.equals("manSweaters")) manClothSweaters.setChecked(true);
            if(category.equals("manCoat")) manClothCoat.setChecked(true);
            if(category.equals("manSports")) manClothSports.setChecked(true);
            if(category.equals("manSuit")) manClothSuit.setChecked(true);
            if(category.equals("manVest")) manClothVest.setChecked(true);
            if(category.equals("manUnderwear")) manClothUnderwear.setChecked(true);
            if(category.equals("manTrunks")) manClothTrunks.setChecked(true);
            if(category.equals("manPajamas")) manClothPajamas.setChecked(true);
            if(category.equals("manOther")) manClothOther.setChecked(true);

            if(category.equals("womanTshirt")) womanClothTshirt.setChecked(true);
            if(category.equals("womanDresses")) womanClothDresses.setChecked(true);
            if(category.equals("womanSkirts")) womanClothSkirts.setChecked(true);
            if(category.equals("womanOveralls")) womanClothOveralls.setChecked(true);
            if(category.equals("womanTracksuit")) womanClothTracksuit.setChecked(true);
            if(category.equals("womanJeans")) womanClothJeans.setChecked(true);
            if(category.equals("womanPants")) womanClothPants.setChecked(true);
            if(category.equals("womanBlouses")) womanClothBlouses.setChecked(true);
            if(category.equals("womanTunics")) womanClothTunics.setChecked(true);
            if(category.equals("womanWedding")) womanClothWedding.setChecked(true);
            if(category.equals("womanTights")) womanClothTights.setChecked(true);
            if(category.equals("womanHoodies")) womanClothHoodies.setChecked(true);
            if(category.equals("womanShorts")) womanClothShorts.setChecked(true);
            if(category.equals("womanJackets")) womanClothJackets.setChecked(true);
            if(category.equals("womanShirting")) womanClothShirting.setChecked(true);
            if(category.equals("womanSweaters")) womanClothSweaters.setChecked(true);
            if(category.equals("womanCoat")) womanClothCoat.setChecked(true);
            if(category.equals("womanSports")) womanClothSports.setChecked(true);
            if(category.equals("womanSuit")) womanClothSuit.setChecked(true);
            if(category.equals("womanVest")) womanClothVest.setChecked(true);
            if(category.equals("womanUnderwear")) womanClothUnderwear.setChecked(true);
            if(category.equals("womanTrunks")) womanClothTrunks.setChecked(true);
            if(category.equals("womanPajamas")) womanClothPajamas.setChecked(true);
            if(category.equals("womanOther")) womanClothOther.setChecked(true);

            if(category.equals("manSneakers")) manShoesSneakers.setChecked(true);
            if(category.equals("manShoes")) manShoesShoes.setChecked(true);
            if(category.equals("manBoots")) manShoesBoots.setChecked(true);
            if(category.equals("manSlippers")) manShoesSlippers.setChecked(true);
            if(category.equals("manSandals")) manShoesSandals.setChecked(true);
            if(category.equals("manOther")) manShoesOther.setChecked(true);

            if(category.equals("womanSneakers")) womanShoesSneakers.setChecked(true);
            if(category.equals("womanShoes")) womanShoesShoes.setChecked(true);
            if(category.equals("womanBoots")) womanShoesBoots.setChecked(true);
            if(category.equals("womanSlippers")) womanShoesSlippers.setChecked(true);
            if(category.equals("womanSandals")) womanShoesSandals.setChecked(true);
            if(category.equals("womanBallet")) womanShoesBallet.setChecked(true);
            if(category.equals("womanOther")) womanShoesOther.setChecked(true);

            if(category.equals("Bracelets")) accessoriesBracelets.setChecked(true);
            if(category.equals("Watch")) accessoriesWatch.setChecked(true);
            if(category.equals("Necklaces")) accessoriesNecklaces.setChecked(true);
            if(category.equals("Rings")) accessoriesRings.setChecked(true);
            if(category.equals("Earrings")) accessoriesEarrings.setChecked(true);
            if(category.equals("Glasses")) accessoriesGlasses.setChecked(true);
            if(category.equals("Other")) accessoriesOther.setChecked(true);
        }

        manCloth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manCloth.isChecked()){
                    headerSubCategory.setVisibility(View.VISIBLE);
                    subcategoryManCloth.setVisibility(View.VISIBLE);
                    ApplicationClass.categoryList.add("manCloth");
                } else {
                    subcategoryManCloth.setVisibility(View.GONE);
                    if(!womanCloth.isChecked() && !manShoes.isChecked() && !womanShoes.isChecked() && !accessories.isChecked()) {
                        headerSubCategory.setVisibility(View.GONE);
                        ApplicationClass.subCategoryList.clear();
                    }
                    ApplicationClass.categoryList.remove("manCloth");
                }
            }
        });

        womanCloth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanCloth.isChecked()){
                    headerSubCategory.setVisibility(View.VISIBLE);
                    subcategoryWomanCloth.setVisibility(View.VISIBLE);
                    ApplicationClass.categoryList.add("womanCloth");
                } else {
                    subcategoryWomanCloth.setVisibility(View.GONE);
                    if(!manCloth.isChecked() && !manShoes.isChecked() && !womanShoes.isChecked() && !accessories.isChecked()) {
                        headerSubCategory.setVisibility(View.GONE);
                        ApplicationClass.subCategoryList.clear();
                    }
                    ApplicationClass.categoryList.remove("womanCloth");
                }
            }
        });

        manShoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manShoes.isChecked()){
                    headerSubCategory.setVisibility(View.VISIBLE);
                    subcategoryManShoes.setVisibility(View.VISIBLE);
                    ApplicationClass.categoryList.add("manShoes");
                } else {
                    subcategoryManShoes.setVisibility(View.GONE);
                    if(!womanCloth.isChecked() && !manCloth.isChecked() && !womanShoes.isChecked() && !accessories.isChecked()) {
                        headerSubCategory.setVisibility(View.GONE);
                        ApplicationClass.subCategoryList.clear();
                    }
                    ApplicationClass.categoryList.remove("manShoes");
                }
            }
        });

        womanShoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoes.isChecked()){
                    headerSubCategory.setVisibility(View.VISIBLE);
                    subcategoryWomanShoes.setVisibility(View.VISIBLE);
                    ApplicationClass.categoryList.add("womanShoes");
                } else {
                    subcategoryWomanShoes.setVisibility(View.GONE);
                    if(!womanCloth.isChecked() && !manShoes.isChecked() && !manCloth.isChecked() && !accessories.isChecked()) {
                        headerSubCategory.setVisibility(View.GONE);
                        ApplicationClass.subCategoryList.clear();
                    }
                    ApplicationClass.categoryList.remove("womanShoes");
                }
            }
        });

        accessories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessories.isChecked()){
                    headerSubCategory.setVisibility(View.VISIBLE);
                    subcategoryAccessories.setVisibility(View.VISIBLE);
                    ApplicationClass.categoryList.add("accessories");
                } else {
                    subcategoryAccessories.setVisibility(View.GONE);
                    if(!womanCloth.isChecked() && !manShoes.isChecked() && !womanShoes.isChecked() && !manCloth.isChecked()) {
                        headerSubCategory.setVisibility(View.GONE);
                        ApplicationClass.subCategoryList.clear();
                    }
                    ApplicationClass.categoryList.remove("accessories");
                }
            }
        });

        manClothTshirt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothTshirt.isChecked()) ApplicationClass.subCategoryList.add("manTshirt");
                else ApplicationClass.subCategoryList.remove("manTshirt");
            }
        });

        manClothTracksuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothTracksuit.isChecked()) ApplicationClass.subCategoryList.add("manTracksuit");
                else ApplicationClass.subCategoryList.remove("manTracksuit");
            }
        });

        manClothJeans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothJeans.isChecked()) ApplicationClass.subCategoryList.add("manJeans");
                else ApplicationClass.subCategoryList.remove("manJeans");
            }
        });

        manClothPants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothPants.isChecked()) ApplicationClass.subCategoryList.add("manPants");
                else ApplicationClass.subCategoryList.remove("manPants");
            }
        });

        manClothHoodies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothHoodies.isChecked()) ApplicationClass.subCategoryList.add("manHoodies");
                else ApplicationClass.subCategoryList.remove("manhHoodies");
            }
        });

        manClothShorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothShorts.isChecked()) ApplicationClass.subCategoryList.add("manShorts");
                else ApplicationClass.subCategoryList.remove("manShorts");
            }
        });

        manClothJackets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothJackets.isChecked()) ApplicationClass.subCategoryList.add("manJackets");
                else ApplicationClass.subCategoryList.remove("manJackets");
            }
        });

        manClothShirting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothShirting.isChecked()) ApplicationClass.subCategoryList.add("manShirting");
                else ApplicationClass.subCategoryList.remove("manShirting");
            }
        });

        manClothSweaters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothSweaters.isChecked()) ApplicationClass.subCategoryList.add("manSweaters");
                else ApplicationClass.subCategoryList.remove("manSweaters");
            }
        });

        manClothCoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothCoat.isChecked()) ApplicationClass.subCategoryList.add("manCoat");
                else ApplicationClass.subCategoryList.remove("manCoat");
            }
        });

        manClothSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothSports.isChecked()) ApplicationClass.subCategoryList.add("manSports");
                else ApplicationClass.subCategoryList.remove("manSports");
            }
        });

        manClothSuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothSuit.isChecked()) ApplicationClass.subCategoryList.add("manSuit");
                else ApplicationClass.subCategoryList.remove("manSuit");
            }
        });

        manClothVest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothVest.isChecked()) ApplicationClass.subCategoryList.add("manVest");
                else ApplicationClass.subCategoryList.remove("manVest");
            }
        });

        manClothUnderwear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothUnderwear.isChecked()) ApplicationClass.subCategoryList.add("manUnderwear");
                else ApplicationClass.subCategoryList.remove("manUnderwear");
            }
        });

        manClothTrunks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothTrunks.isChecked()) ApplicationClass.subCategoryList.add("manTrunks");
                else ApplicationClass.subCategoryList.remove("manTrunks");
            }
        });

        manClothPajamas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothPajamas.isChecked()) ApplicationClass.subCategoryList.add("manPajamas");
                else ApplicationClass.subCategoryList.remove("manPajamas");
            }
        });

        manClothOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manClothOther.isChecked()) ApplicationClass.subCategoryList.add("manOther");
                else ApplicationClass.subCategoryList.remove("manOther");
            }
        });

        womanClothTshirt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothTshirt.isChecked()) ApplicationClass.subCategoryList.add("womanTshirt");
                else ApplicationClass.subCategoryList.remove("womanTshirt");
            }
        });

        womanClothDresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothDresses.isChecked()) ApplicationClass.subCategoryList.add("womanDresses");
                else ApplicationClass.subCategoryList.remove("womanDresses");
            }
        });

        womanClothSkirts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothSkirts.isChecked()) ApplicationClass.subCategoryList.add("womanSkirts");
                else ApplicationClass.subCategoryList.remove("womanSkirts");
            }
        });

        womanClothOveralls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothOveralls.isChecked()) ApplicationClass.subCategoryList.add("womanOveralls");
                else ApplicationClass.subCategoryList.remove("womanOveralls");
            }
        });

        womanClothTracksuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothTracksuit.isChecked()) ApplicationClass.subCategoryList.add("womanTracksuit");
                else ApplicationClass.subCategoryList.remove("womanTracksuit");
            }
        });

        womanClothJeans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothJeans.isChecked()) ApplicationClass.subCategoryList.add("womanJeans");
                else ApplicationClass.subCategoryList.remove("womanJeans");
            }
        });

        womanClothPants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothPants.isChecked()) ApplicationClass.subCategoryList.add("womanPants");
                else ApplicationClass.subCategoryList.remove("womanPants");
            }
        });

        womanClothBlouses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothBlouses.isChecked()) ApplicationClass.subCategoryList.add("womanBlouses");
                else ApplicationClass.subCategoryList.remove("womanBlouses");
            }
        });

        womanClothTunics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothTunics.isChecked()) ApplicationClass.subCategoryList.add("womanTunics");
                else ApplicationClass.subCategoryList.remove("womanTunics");
            }
        });

        womanClothWedding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothWedding.isChecked()) ApplicationClass.subCategoryList.add("womanWedding");
                else ApplicationClass.subCategoryList.remove("womanWedding");
            }
        });

        womanClothTights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothTights.isChecked()) ApplicationClass.subCategoryList.add("womanTights");
                else ApplicationClass.subCategoryList.remove("womanTights");
            }
        });

        womanClothHoodies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothHoodies.isChecked()) ApplicationClass.subCategoryList.add("womanHoodies");
                else ApplicationClass.subCategoryList.remove("womanHoodies");
            }
        });

        womanClothShorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothShorts.isChecked()) ApplicationClass.subCategoryList.add("womanShorts");
                else ApplicationClass.subCategoryList.remove("womanShorts");
            }
        });

        womanClothJackets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothJackets.isChecked()) ApplicationClass.subCategoryList.add("womanJackets");
                else ApplicationClass.subCategoryList.remove("womanJackets");
            }
        });

        womanClothShirting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothShirting.isChecked()) ApplicationClass.subCategoryList.add("womanShirting");
                else ApplicationClass.subCategoryList.remove("womanShirting");
            }
        });

        womanClothSweaters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothSweaters.isChecked()) ApplicationClass.subCategoryList.add("womanSweaters");
                else ApplicationClass.subCategoryList.remove("womanSweaters");
            }
        });

        womanClothCoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothCoat.isChecked()) ApplicationClass.subCategoryList.add("womanCoat");
                else ApplicationClass.subCategoryList.remove("womanCoat");
            }
        });

        womanClothSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothSports.isChecked()) ApplicationClass.subCategoryList.add("womanSports");
                else ApplicationClass.subCategoryList.remove("womanSports");
            }
        });

        womanClothSuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothSuit.isChecked()) ApplicationClass.subCategoryList.add("womanSuit");
                else ApplicationClass.subCategoryList.remove("womanSuit");
            }
        });

        womanClothVest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothVest.isChecked()) ApplicationClass.subCategoryList.add("womanVest");
                else ApplicationClass.subCategoryList.remove("womanVest");
            }
        });

        womanClothUnderwear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothUnderwear.isChecked()) ApplicationClass.subCategoryList.add("womanUnderwear");
                else ApplicationClass.subCategoryList.remove("womanUnderwear");
            }
        });

        womanClothTrunks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothTrunks.isChecked()) ApplicationClass.subCategoryList.add("womanTrunks");
                else ApplicationClass.subCategoryList.remove("womanTrunks");
            }
        });

        womanClothPajamas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothPajamas.isChecked()) ApplicationClass.subCategoryList.add("womanPajamas");
                else ApplicationClass.subCategoryList.remove("womanPajamas");
            }
        });

        womanClothOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanClothOther.isChecked()) ApplicationClass.subCategoryList.add("womanOther");
                else ApplicationClass.subCategoryList.remove("womanOther");
            }
        });

        manShoesSneakers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manShoesSneakers.isChecked()) ApplicationClass.subCategoryList.add("manSneakers");
                else ApplicationClass.subCategoryList.remove("manSneakers");
            }
        });

        manShoesShoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manShoesShoes.isChecked()) ApplicationClass.subCategoryList.add("manShoes");
                else ApplicationClass.subCategoryList.remove("manShoes");
            }
        });

        manShoesBoots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manShoesBoots.isChecked()) ApplicationClass.subCategoryList.add("manBoots");
                else ApplicationClass.subCategoryList.remove("manBoots");
            }
        });

        manShoesSlippers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manShoesSlippers.isChecked()) ApplicationClass.subCategoryList.add("manSlippers");
                else ApplicationClass.subCategoryList.remove("manSlippers");
            }
        });

        manShoesSandals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manShoesSandals.isChecked()) ApplicationClass.subCategoryList.add("manSandals");
                else ApplicationClass.subCategoryList.remove("manSandals");
            }
        });

        manShoesOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manShoesOther.isChecked()) ApplicationClass.subCategoryList.add("manOther");
                else ApplicationClass.subCategoryList.remove("manOther");
            }
        });

        womanShoesSneakers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoesSneakers.isChecked()) ApplicationClass.subCategoryList.add("womanSneakers");
                else ApplicationClass.subCategoryList.remove("womanSneakers");
            }
        });

        womanShoesShoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoesShoes.isChecked()) ApplicationClass.subCategoryList.add("womanShoes");
                else ApplicationClass.subCategoryList.remove("womanShoes");
            }
        });

        womanShoesBoots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoesBoots.isChecked()) ApplicationClass.subCategoryList.add("womanBoots");
                else ApplicationClass.subCategoryList.remove("womanBoots");
            }
        });

        womanShoesSlippers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoesSlippers.isChecked()) ApplicationClass.subCategoryList.add("womanSlippers");
                else ApplicationClass.subCategoryList.remove("womanSlippers");
            }
        });

        womanShoesSandals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoesSandals.isChecked()) ApplicationClass.subCategoryList.add("womanSandals");
                else ApplicationClass.subCategoryList.remove("womanSandals");
            }
        });

        womanShoesBallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoesBallet.isChecked()) ApplicationClass.subCategoryList.add("womanBallet");
                else ApplicationClass.subCategoryList.remove("womanBallet");
            }
        });

        womanShoesOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(womanShoesOther.isChecked()) ApplicationClass.subCategoryList.add("womanOther");
                else ApplicationClass.subCategoryList.remove("womanOther");
            }
        });

        accessoriesBracelets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessoriesBracelets.isChecked()) ApplicationClass.subCategoryList.add("Bracelets");
                else ApplicationClass.subCategoryList.remove("Bracelets");
            }
        });

        accessoriesWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessoriesWatch.isChecked()) ApplicationClass.subCategoryList.add("Watch");
                else ApplicationClass.subCategoryList.remove("Watch");
            }
        });

        accessoriesNecklaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessoriesNecklaces.isChecked()) ApplicationClass.subCategoryList.add("Necklaces");
                else ApplicationClass.subCategoryList.remove("Necklaces");
            }
        });

        accessoriesRings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessoriesRings.isChecked()) ApplicationClass.subCategoryList.add("Rings");
                else ApplicationClass.subCategoryList.remove("Rings");
            }
        });

        accessoriesEarrings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessoriesEarrings.isChecked()) ApplicationClass.subCategoryList.add("Earrings");
                else ApplicationClass.subCategoryList.remove("Earrings");
            }
        });

        accessoriesGlasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessoriesGlasses.isChecked()) ApplicationClass.subCategoryList.add("Glasses");
                else ApplicationClass.subCategoryList.remove("Glasses");
            }
        });

        accessoriesOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accessoriesOther.isChecked()) ApplicationClass.subCategoryList.add("Other");
                else ApplicationClass.subCategoryList.remove("Other");
            }
        });
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
                final List<Cloth> mainCloth = new ArrayList<>();
                cloths.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final Cloth cloth = snapshot.getValue(Cloth.class);

                    if(ApplicationClass.subCategoryList.size() == 0 && ApplicationClass.categoryList.size() > 0){
                        for(String category : ApplicationClass.categoryList){
                            if(category.equals(cloth.getCategory())) {
                                if (cloth.getOwnerID().equals(ApplicationClass.currentUser.getUid()) && !cloth.isSold())
                                    cloths.add(cloth);
                                else if (!cloth.isSold()) {
                                    for (String id : followingList) {
                                        if (id.equals(cloth.getOwnerID())) cloths.add(cloth);
                                    }
                                }
                            }
                        }
                    } else if(ApplicationClass.subCategoryList.size() > 0){
                        for(String category : ApplicationClass.subCategoryList){
                            if(category.equals(cloth.getSubcategory())) {
                                if (cloth.getOwnerID().equals(ApplicationClass.currentUser.getUid()) && !cloth.isSold())
                                    cloths.add(cloth);
                                else if (!cloth.isSold()) {
                                    for (String id : followingList) {
                                        if (id.equals(cloth.getOwnerID())) cloths.add(cloth);
                                    }
                                }
                            }
                        }
                    } else {
                        if (cloth.getOwnerID().equals(ApplicationClass.currentUser.getUid()) && !cloth.isSold())
                            cloths.add(cloth);
                        else if (!cloth.isSold()) {
                            for (String id : followingList) {
                                if (id.equals(cloth.getOwnerID())) cloths.add(cloth);
                            }
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
