package tudor.example.jobby.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import tudor.example.jobby.R;
import tudor.example.jobby.activities.MainActivity;
import tudor.example.jobby.objects.User;

public class ProfileFragment extends Fragment {

    // the View
    private View mFragmentView;

    // EditTexts
    private EditText addressEditText;
    private EditText phoneNumberEditText;

    // profile picture image view
    private ImageView profilePicImageView;

    // text views
    private TextView usernameTextView;
    private TextView ratingTextView;
    private TextView jobsCompletedTextView;

    // the progress bar
    private ProgressBar progressBar;

    // the buttons
    private Button updateInfo;
    private Button signOutButton;

    // SharedPreferences object
    SharedPreferences sharedPreferences;

    // firebase database instance
    private FirebaseDatabase database;

    // reference to the current user's node in the database
    private DatabaseReference currentUserRef;

    // code for the intent
    private static final int TAKE_PICTURE = 1;

    // bitmap of the profile picture
    private Bitmap bitmap = null;

    // requires empty constructor
    public ProfileFragment(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 0){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // check if the request comes from picture taking
        if(requestCode == TAKE_PICTURE){

            // check if the request was successful
            if(resultCode == Activity.RESULT_OK){

                // check if the intent is null
                if(data.getExtras() != null){

                    // create a bitmap
                    bitmap = (Bitmap) data.getExtras().get("data");

                    // crop the bitmap
                    bitmap = getCircularBitmap(bitmap);

                    // set the bitmap in the image view
                    profilePicImageView.setImageBitmap(bitmap);

                    updateDatabase(bitmap);

                    updateNavHeaderImage(bitmap);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final Bundle savedInstanceState) {

        // get the View
        mFragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        findViews();

        // get the database instance
        database = FirebaseDatabase.getInstance();

        // get the current user's reference
        currentUserRef = database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // set the listener on the update button
        updateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateInfo();
            }
        });

        // set the listener on the sign out button
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signOut();
            }
        });

        // set the listener on the image view
        profilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if the sdk is greater than or equal to 23, check if we have permission
                if(Build.VERSION.SDK_INT >= 23){
                    if ((ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                            (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                        requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                0);

                    } else {
                        takePhoto();
                    }

                } else {
                    takePhoto();
                }
            }
        });

        loadImageView();

        loadUserInfo();

        return mFragmentView;
    }

    // helper method to find the views
    private void findViews(){

        // find the EditTexts
        addressEditText = (EditText) mFragmentView.findViewById(R.id.profileAddress);
        phoneNumberEditText = (EditText) mFragmentView.findViewById(R.id.profilePhoneNumber);

        // find the progress bar
        progressBar = mFragmentView.findViewById(R.id.profile_pic_progress_bar);

        // find the text views
        usernameTextView = mFragmentView.findViewById(R.id.my_profile_username_textview);
        ratingTextView = mFragmentView.findViewById(R.id.my_profile_rating_textview);
        jobsCompletedTextView = mFragmentView.findViewById(R.id.my_profile_jobs_completed_textview);

        // find the buttons
        updateInfo = mFragmentView.findViewById(R.id.profileButton);
        signOutButton = mFragmentView.findViewById(R.id.my_profile_sign_out_button);

        // find the image view
        profilePicImageView = (ImageView) mFragmentView.findViewById(R.id.profileFragmentImageView);

    }

    // helper function to load the image view
    private void loadImageView(){

        // query the current user's reference
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // check that data was retrieved
                if(dataSnapshot.getValue() != null){

                    // parse data into a user object
                    User currentUser = dataSnapshot.getValue(User.class);

                    // get the download url
                    String downloadUrl = currentUser.getmProfilePicUrl();

                    // load the picture in the image view
                    if(!downloadUrl.equals("")){
                        Glide.with(getActivity())
                                .load(downloadUrl)
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                        // hide the progress bar
                                        progressBar.setVisibility(View.GONE);

                                        return false;
                                    }
                                })
                                .into(profilePicImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // helper function to load user stats
    private void loadUserInfo(){

        // query the database reference
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // check that the data is not null
                if(dataSnapshot.getValue() != null){

                    // parse data in a user object
                    User currentUser = dataSnapshot.getValue(User.class);

                    // set the username
                    usernameTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

                    // set the rating
                    ratingTextView.setText(currentUser.getmRating() + "★");

                    // set the job count
                    jobsCompletedTextView.setText(String.valueOf(currentUser.getmJobsCompleted()));

                    // set the address
                    addressEditText.setText(currentUser.getmAddress());

                    // set the phone number
                    int phoneNumber = currentUser.getmPhoneNumber();
                    if(phoneNumber != 0){
                        phoneNumberEditText.setText(String.valueOf(currentUser.getmPhoneNumber()));
                    } else {
                        phoneNumberEditText.setText("");
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // helper function to update user information
    private void updateInfo(){

        // get the text from the edit text
        String address = addressEditText.getText().toString();
        int phoneNumber = Integer.parseInt(phoneNumberEditText.getText().toString());

        // update the database
        currentUserRef.child("mAddress").setValue(address);
        currentUserRef.child("mPhoneNumber").setValue(phoneNumber);

    }

    // helper function to sign the user out
    private void signOut(){

        // sign the user out from firebase
        AuthUI.getInstance().signOut(getActivity());

        // go back to main activity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);

    }

    // helper function to start taking a photo
    private void takePhoto(){

        // create an intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // start the activity for result
        startActivityForResult(intent, TAKE_PICTURE);
    }

    // helper function to update the database
    private void updateDatabase(Bitmap bitmap){

        // get the user id
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // get a storage reference
        final StorageReference reference = FirebaseStorage.getInstance().getReference("profile_pics").child(userID);

        // convert the bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteData = baos.toByteArray();

        // upload to firebase
        final UploadTask uploadTask = reference.putBytes(byteData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // get the download url of the profile pic
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();

                        // get the user's id
                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // get a reference to the user in the database
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userID);

                        // update the profile
                        userRef.child("mProfilePicUrl").setValue(downloadUrl);
                    }
                });
            }
        });
    }

    // helper function to update the navigation header when the user updates his profile picture
    private void updateNavHeaderImage(Bitmap bitmap){

        // get the navigation view
        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);

        // get the header
        View header = navigationView.getHeaderView(0);

        // get the image view
        ImageView imageView = header.findViewById(R.id.navHeaderProfileImage);

        // set the image's bitmap
        imageView.setImageBitmap(bitmap);

    }

    // helper function to crop the bitmap
    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}


