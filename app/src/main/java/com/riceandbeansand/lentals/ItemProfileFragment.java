package com.riceandbeansand.lentals;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;

public class ItemProfileFragment extends Fragment {

    private String itemID;
    private static final String TAG = "DocSnippets";
    DecimalFormat money_format = new DecimalFormat("$0.00");
    private String name;
    private double price;
    private String image;
    private String userName;
    private String descrip;
    private String userId;
    private String currentUserID;
    private String profileId;
    private String profilePicture;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.item_profile, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Item Profile");

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            itemID = bundle.getString("itemID", "");
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        final TextView nameIP = (TextView) view.findViewById(R.id.name_ip);
        final TextView rateIP = (TextView) view.findViewById(R.id.rate_ip);
        final TextView descripIP = (TextView) view.findViewById(R.id.descrip_ip);
        final TextView userNameIP = (TextView) view.findViewById(R.id.userName_ip);
        final ImageView imageIP = (ImageView) view.findViewById(R.id.imageView_ip);
        view.findViewById(R.id.profilePictureContainer).setClipToOutline(true);
        final Button messageBtn = (Button) view.findViewById(R.id.message_btn);
        final ImageView profilePictureIP = (ImageView) view.findViewById(R.id.userProfilePic_ip);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference item = db.collection("items").document(itemID);
        item.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        System.out.println("DOCUMENT EXISTS");
                        name = document.getString("name");
                        price = document.getDouble("price");
                        image = document.getString("image");
                        userName = document.getString("userName");
                        descrip = document.getString("descrip");
                        userId = document.getString("userID");
                        profileId = document.getString("profileID");
                        profilePicture = document.getString("profilePicture");

                        if (currentUserID.equals(userId)) {
                            messageBtn.setVisibility(View.GONE);
                        }

                        nameIP.setText(name);
                        rateIP.setText(money_format.format(price));
                        descripIP.setText(descrip);
                        userNameIP.setText(userName);
                        messageBtn.setText("Message");

                        try{
                            Log.d("TAG", "document: " + userId);
                            if (profilePicture != null && !profilePicture.isEmpty()) {
                                profilePictureIP.setImageBitmap(stringToBitmap(profilePicture));
                            }
                        } catch (Exception e) {

                        }

                        try {
                            if (image != null && !image.isEmpty()) {
                                imageIP.setImageBitmap(stringToBitmap(image));
                            }
                        } catch (Exception e) {

                        }

                    }
                }
            }
        });

        profilePictureIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserID.equals(userId)) {
                    Bundle args = new Bundle();
                    args.putString("queryType", "myItems");
                    args.putString("userId", currentUserID);
                    Fragment myProfile = new ListingsFragment();
                    myProfile.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                            .replace(R.id.fragment_container, myProfile).commit();
                }
                else {
                    Bundle args = new Bundle();
                    args.putString("name", userName);
                    args.putString("userId", userId);
                    args.putString("profileId", profileId);
                    args.putString("profilePicture", profilePicture);
                    Fragment userProfile = new UserProfileFragment(); // userProfile fragment
                    userProfile.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                            .replace(R.id.fragment_container, userProfile).commit();
                }
            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                boolean lesser = currentUserID.compareTo(userId) < 0; //need chatID that is same if currentUserID and userId are swapped. So always put "lesser" id first.
                String chatID = lesser ? currentUserID + userId : userId + currentUserID; //this is how the chatID is defined; not safe since userId might not be defined yet
                args.putString("chatID", chatID);
                args.putString("name", userName);
                Fragment chatFragment = new ChatFragment();
                chatFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                        .replace(R.id.fragment_container, chatFragment).commit();

                //Voiding below for now
                /*
                boolean isFBInstalled = isAppInstalled("com.facebook.orca");

                if (!isFBInstalled) {
                    Toast.makeText(getActivity(),
                            "Facebook messenger isn't installed. Please download the app first.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent= new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "Hello, is this still available?");
                    intent.setType("text/plain");
                    intent.setPackage("com.facebook.orca");

                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(),
                                "Sorry! Can't open Facebook messenger right now. Please try again later.",
                                Toast.LENGTH_SHORT).show();
                    }
                }*/
            }
        });

        return view;
    }

    private Bitmap stringToBitmap(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    private boolean isAppInstalled(String uri) {
        PackageManager pm = getActivity().getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
