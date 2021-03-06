package tudor.example.jobby.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;

import tudor.example.jobby.fragments.JobFragment;
import tudor.example.jobby.fragments.ProfileFragment;
import tudor.example.jobby.R;
import tudor.example.jobby.fragments.RequestFragment;
import tudor.example.jobby.objects.User;

public class MainActivity extends AppCompatActivity {

    // constant for the sign in code
    private static final int RC_SIGN_IN = 123;

    // FirebaseAuth instance
    public FirebaseAuth mFirebaseAuth;

    // drawer
    public DrawerLayout mDrawerLayout;
    public NavigationView navigationView;

    // nav header
    public View navigationHeader;
    public TextView navHeaderRatingTextView;
    public ImageView navHeaderImageView;

    // toggle and toolbar
    public ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;

    // fragment transacion
    public FragmentTransaction fragmentTransaction;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the result is from sing in
        if(requestCode == RC_SIGN_IN){
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_CANCELED){

                finish();
            } else {
                // reference to "users" node
                DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");

                // get the current user's id
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // get a reference to the current user's node
                final DatabaseReference currentUserReference = usersReference.child(currentUserId);

                // query the reference
                currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.getValue() == null){

                            // get the current user's email
                            String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                            // create a user object
                            final User currentUserProfile = new User(0, 0, 0, "", currentUserEmail, "", "", "");

                            // get the firebase id
                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {

                                    String token = task.getResult().getToken();

                                    // set the token
                                    currentUserProfile.setmToken(token);

                                    // add the user in the database
                                    currentUserReference.setValue(currentUserProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // initialize the layout
                                            initializeLayout();
                                        }
                                    });
                                }
                            });


                        } else {

                            // initialize the layout
                            initializeLayout();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the FirebaseApp
        FirebaseApp.initializeApp(this);

        // initialize the FirebaseAuth instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // there is already a signed in user
        if(mFirebaseAuth.getCurrentUser() != null){
            // initialize layout
            initializeLayout();
        } else {

            Log.i("start", "login screen");

            // start the sign in flow
            startLoginScreen();
        }

    }

    public void startLoginScreen(){

        // create an intent
        Intent firebaseIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.logo)
                .setTheme(R.style.AppTheme)
                .setAvailableProviders(
                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                .build();

        startActivityForResult(firebaseIntent, RC_SIGN_IN);
    }

    public void initializeLayout(){

        // get the toolbar and set it
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // get the drawer layout
        mDrawerLayout = findViewById(R.id.drawer_layout);

        // get the toggle
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        // get the navigation view
        navigationView = findViewById(R.id.navigation_view);

        // display original color of icon
        //navigationView.setItemIconTintList(null);

        // get and set the nav header's textView
        View navigationHeader = navigationView.getHeaderView(0);
        TextView nameNavHeaderTextView = navigationHeader.findViewById(R.id.navHeaderNameTextView);
        nameNavHeaderTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        // get the image view
        navHeaderImageView = navigationHeader.findViewById(R.id.navHeaderProfileImage);

        // load the image
        loadImage();

        // add the drawer listener and sync the toggle
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        // display the hamburger icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the fragment transaction and set a fragment
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, new JobFragment());
        fragmentTransaction.commit();
        getSupportActionBar().setTitle("Toate anunturile");

        // put a listener on the navigation view
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();

                switch(item.getItemId()){
                    case R.id.drawerAllJobs :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Toate anunturile");
                        JobFragment.categoryIndex = -1;
                        break;
                    case R.id.drawerAnunturileMele :
                        fragmentTransaction.replace(R.id.main_container, new RequestFragment()).commit();
                        getSupportActionBar().setTitle("Anunturile mele");
                        break;
                    case R.id.drawerProfile :
                        fragmentTransaction.replace(R.id.main_container, new ProfileFragment()).commit();
                        getSupportActionBar().setTitle("Setari");
                        break;
                    case R.id.drawer_it_menu :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("IT-Software");
                        JobFragment.categoryIndex = 0;
                        break;
                    case R.id.drawer_electrocasnice :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Electrocasnice");
                        JobFragment.categoryIndex = 1;
                        break;
                    case R.id.drawer_constructii :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Meseriasi-Constructori");
                        JobFragment.categoryIndex = 2;
                        break;
                    case R.id.drawer_cursuri :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Cursuri-Meditatii");
                        JobFragment.categoryIndex = 3;
                        break;
                    case R.id.drawer_curatenie :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Curatenie-Servicii menaj");
                        JobFragment.categoryIndex = 4;
                        break;
                    case R.id.drawer_curierat :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Curierat-Servicii auto");
                        JobFragment.categoryIndex = 5;
                        break;
                    case R.id.drawer_babysitter :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Bone-Babysitter");
                        JobFragment.categoryIndex = 6;
                        break;
                    case R.id.drawer_animale :
                        fragmentTransaction.replace(R.id.main_container, new JobFragment()).commit();
                        getSupportActionBar().setTitle("Animale de companie");
                        JobFragment.categoryIndex = 7;
                        break;
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // load the image in the nav drawer
    private void loadImage(){

        // get a reference to the current user's node in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // query the reference
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // check that data was retrieved
                if(dataSnapshot.getValue() != null){

                    // parse data into a user object
                    User currentUser = dataSnapshot.getValue(User.class);

                    // get the download url
                    String downloadUrl = currentUser.getmProfilePicUrl();

                    // load the picture into the image view
                    if(!downloadUrl.equals("")) {
                        Glide.with(MainActivity.this)
                                .load(downloadUrl)
                                .into(navHeaderImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
