package com.riceandbeansand.lentals;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileFragment extends Fragment {
    private String name;
    private String userId;
    private String profileId;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.user_profile, container, false);
//        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("User Profile");

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            name = bundle.getString("name", "UNKNOWN");
            userId = bundle.getString("userId");
            profileId = bundle.getString("profileId", "");
        }

        Log.d("Tag", "Profile ID: " + profileId);

        view.findViewById(R.id.profilePictureContainer).setClipToOutline(true);
        TextView userNameView = (TextView) view.findViewById(R.id.userName);
        userNameView.setText(name);
        ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById(R.id.userProfilePic);
        profilePictureView.setProfileId(profileId);

        final Button messageBtn = (Button) view.findViewById(R.id.message_btn);
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                boolean lesser = currentUserID.compareTo(userId) < 0;
                String chatID = lesser ? currentUserID + userId : userId + currentUserID;
                args.putString("chatID", chatID);
                args.putString("name", name);
                Fragment chatFragment = new ChatFragment();
                chatFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                        .replace(R.id.fragment_container, chatFragment).commit();
            }
        });

        Bundle args = new Bundle();
        args.putString("queryType", "userItems");
        args.putString("userId", userId);
        Fragment userListings = new ListingsFragment();
        userListings.setArguments(args);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.list_container, userListings).commit();

        return view;
    }
}
