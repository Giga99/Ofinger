package aplikacija.apl.ofinger.mainActivities;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.startActivities.StartActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuestFragment extends Fragment {

    private View view;
    private ImageView ivBack;

    public GuestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_guest, container, false);
        ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationClass.currentUserReference.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        ApplicationClass.currentUser.delete();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getContext(), StartActivity.class));
                        GuestFragment.this.getActivity().finish();
                    }
                });
            }
        });
        return view;
    }

}
