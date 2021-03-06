package aplikacija.apl.ofinger;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import aplikacija.apl.ofinger.adapters.ImageVideoAdapter;
import aplikacija.apl.ofinger.models.Cloth;
import aplikacija.apl.ofinger.models.ImageVideo;
import aplikacija.apl.ofinger.models.User;

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
    public static ImageVideo currentImageVideo;
    public static List<ImageVideo> imageVideosAddingCloth;
    public static List<ImageVideo> imageVideosClothInfo;
    public static int position;
    public static List<String> urls;
    public static boolean currentUserCloth = false;
    public static boolean lastImage = false;
    public static boolean sold = false;
    public static List<String> categoryList = new ArrayList<>();
    public static List<String> subCategoryList = new ArrayList<>();
    public static String currentActivityZoomImage;
    public static ImageVideoAdapter adapterClothInfoImageVideo;
    public static ImageVideoAdapter adapterAddingClothImageVideo;
}
