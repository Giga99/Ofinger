package com.example.ofinger;

import android.app.Application;

import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Image;
import com.example.ofinger.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ApplicationClass extends Application {

    public static FirebaseUser currentUser;
    public static User otherUser;
    public static DatabaseReference currentUserReference;
    public static DatabaseReference allUsers;
    public static List<Cloth> mainCloths;
    public static List<Cloth> followingCloth;
    public static List<Cloth> searchCloth;
    public static List<Cloth> profileCloth;
    public static List<Cloth> userCloths;
    public static List<Cloth> soldCloths;
    public static List<Cloth> wishCloths;
    public static Image currentImage;
    public static boolean sold = false;
    public static boolean wish = false;
}
