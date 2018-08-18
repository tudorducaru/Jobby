package tudor.example.jobby.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import tudor.example.jobby.objects.Job;
import tudor.example.jobby.R;

public class AddAJob extends AppCompatActivity {

    // code for the intent
    private static final int TAKE_PICTURE = 1;

    // instance of the toolbar
    private Toolbar mToolbar;

    // EditTexts
    private EditText titleEditText;
    private EditText descriptionEditText;

    // add a picture image view
    private ImageView addPictureImageView;

    // loaded picture image view
    private ImageView loadedPictureImageView;

    // the progress bar
    private ProgressBar progressBar;

    // database instance
    private FirebaseDatabase mFirebaseDatabase;

    // database reference
    private DatabaseReference mJobsReference;

    // job's category
    private String category = "Sanitary";

    // the spinner
    private Spinner categorySpinner;

    // name of the picture file
    private String filename;

    // picture uri
    private Uri imageUri;

    // file instance of the image
    private File photo;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 0){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // check if the request comes from picture taking
        if(requestCode == TAKE_PICTURE){

            // check if the request was successful
            if(resultCode == Activity.RESULT_OK){

                // set the image in the image view
                loadedPictureImageView.setImageURI(imageUri);

                // hide the add a picture image view
                addPictureImageView.setVisibility(View.GONE);

                // set the listener on the loaded picture
                loadedPictureImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { checkPermissions();
                        }
                    });
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ajob);

        // find and set the Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("Add a job");

        // find the views
        titleEditText = findViewById(R.id.addAJobTitle);
        descriptionEditText = findViewById(R.id.addAJobDescription);
        addPictureImageView = findViewById(R.id.add_a_job_picture_image_view);
        loadedPictureImageView = findViewById(R.id.add_job_loaded_image_view);
        progressBar = findViewById(R.id.add_a_job_progress_bar);

        // initially, hide the progress bar
        progressBar.setVisibility(View.GONE);

        // find the spinner
        categorySpinner = findViewById(R.id.addAJobSpinner);

        // set the spinner up
        setupSpinner();

        // set the listener on the ImageView
        addPictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissions();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate the layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_job_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // there is only one option, add job menu item
        addJob();

        return super.onOptionsItemSelected(item);
    }

    // helper function to add job
    private void addJobInfo(final String imageUrl){

        if(!(titleEditText.getText().toString().equals("")) && !(descriptionEditText.getText().toString().equals(""))){

            // get the database
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            // get the references
            mJobsReference = mFirebaseDatabase.getReference("jobs");
            DatabaseReference currentUserReference = mFirebaseDatabase.getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            // query the current user reference
            currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // check if data was retrieved
                    if(dataSnapshot.getValue() != null){

                        // get the address and the phone number
                        String address = (String) dataSnapshot.child("mAddress").getValue();
                        String phoneNumber = String.valueOf(dataSnapshot.child("mPhoneNumber").getValue());

                        // create a Job object
                        Job job = new Job(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                titleEditText.getText().toString(),
                                descriptionEditText.getText().toString(),
                                address,
                                phoneNumber,
                                0,
                                imageUrl,
                                category);

                        // add the job
                        mJobsReference.push().setValue(job);

                        // load finished, so hide the progress bar
                        progressBar.setVisibility(View.GONE);

                        // get out
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    // helper function to add the job image to the database
    private void addJob(){

        // load started, so start the progress bar
        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();

        // if image not loaded, don't load image
        if(imageUri == null){

            // add job info without url
            addJobInfo("");

        } else {

            // generate a random id
            String randomID = String.valueOf(java.util.UUID.randomUUID());

            // get a storage reference
            final StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("job_pics")
                    .child(randomID);

            // UPLOAD IMAGE
            final UploadTask uploadTask = storageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // get the url of the profile pic
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            // add the job info as well
                            addJobInfo(uri.toString());
                        }
                    });
                }
            });
        }
    }

    // helper method to check permissions for taking photo
    private void checkPermissions(){

        if(Build.VERSION.SDK_INT >= 23){
            Log.i("Build", ">23");
            if ((checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);

            } else {
                takePhoto();
            }

        } else {
            takePhoto();
        }
    }

    // helper method to start taking a photo
    private void takePhoto(){

        // create an intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // set a filename
        filename = String.valueOf(java.util.UUID.randomUUID());

        // create a photo object
        photo = new File(Environment.getExternalStorageDirectory(), filename);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));

        // get the uri of the photo
        imageUri = Uri.fromFile(photo);

        // start activity for result
        startActivityForResult(intent, TAKE_PICTURE);
    }

    // method to set up the spinner
    private void setupSpinner(){
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        final ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.job_category, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        categorySpinner.setAdapter(categorySpinnerAdapter);

        // Set the integer mSelected to the constant values
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {

                    if (selection.equals("IT-Software")) {
                        category = "IT-Software";
                    } else if (selection.equals("Electrocasnice")) {
                        category = "Electrocasnice";
                    } else if (selection.equals("Meseriasi-Constructori")){
                        category = "Meseriasi-Constructori";
                    } else if (selection.equals("Cursuri-Meditatii")){
                        category = "Cursuri-Meditatii";
                    } else if (selection.equals("Curatenie-Servicii menaj")){
                        category = "Curatenie-Servicii menaj";
                    } else if (selection.equals("Curierat-Servicii auto")){
                        category = "Curierat-Servicii auto";
                    } else if (selection.equals("Bone-Babysitter")){
                        category = "Bone-Babysitter";
                    } else if (selection.equals("Animale de companie")){
                        category = "Animale de companie";
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = "Sanitary";
            }
        });
    }
}
