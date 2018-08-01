package tudor.example.jobby.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tudor.example.jobby.R;
import tudor.example.jobby.objects.User;

public class BidderProfile extends AppCompatActivity {

    // the Toolbar
    private Toolbar mToolbar;

    // text views
    private TextView ratingTextView;
    private TextView ratingCountTextView;
    private TextView nameTextView;
    private TextView jobsCompletedTextView;

    // image view
    private ImageView profilePic;

    // bidder's id, name and image download url
    private String bidderId;
    private String bidderName;
    private String bidderImageUrl;
    private long bidderJobsCompleted;
    private double bidderRating;
    private long bidderRatingCount;

    // variable that stores the key of the job accepted
    private String jobAccepted;

    // the buttons
    private Button acceptBidButton;
    private Button rateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidder_profile);

        // find and set the Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("Bidder");

        // get the intent
        Intent intent = getIntent();

        // get the bidder's id and name
        bidderId = intent.getStringExtra("id");
        bidderName = intent.getStringExtra("name");

        // get the job key
        jobAccepted = intent.getStringExtra("key");

        getJobStatus();

        // find the text views
        ratingTextView = (TextView) findViewById(R.id.bidderRatingTextView);
        ratingCountTextView = (TextView) findViewById(R.id.bidderRatingCountTextView);
        nameTextView = (TextView) findViewById(R.id.bidderNameTextView);
        jobsCompletedTextView = (TextView) findViewById(R.id.bidderProfileJobsCompletedTextView);

        // get the image view
        profilePic = (ImageView) findViewById(R.id.bidderImageView);

        // set the name in the TextView
        nameTextView.setText(bidderName);

        // get the reference to the bidder's profile
        final DatabaseReference bidderReference = FirebaseDatabase.getInstance().getReference("users").child(bidderId);

        final ArrayList<String> email = new ArrayList<>();

        // query the reference
        bidderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get the profile
                User bidder = dataSnapshot.getValue(User.class);

                // get the data
                email.add(bidder.getmEmail());
                bidderJobsCompleted = bidder.getmJobsCompleted();
                bidderRating = (double) bidder.getmRating();
                bidderRatingCount = (long) bidder.getmRatingCount();
                bidderImageUrl = bidder.getmProfilePicUrl();

                // set the data
                if(!bidderImageUrl.equals("")) {
                    Glide.with(getApplicationContext())
                            .load(bidderImageUrl)
                            .into(profilePic);
                }
                ratingTextView.setText("Rating : ⭐" + bidderRating);
                ratingCountTextView.setText("Nr. de rating-uri : " + bidderRatingCount);
                jobsCompletedTextView.setText("Lucrari executate : " + bidderJobsCompleted);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // get the RatingBar
        final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setNumStars(5);

        // get the rate button
        rateButton = (Button) findViewById(R.id.rateButton);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // bid sum
                double bidSum = bidderRating * bidderRatingCount;
                bidSum = bidSum + ratingBar.getRating();
                bidderRatingCount = bidderRatingCount + 1;
                bidSum = bidSum/bidderRatingCount;
                bidSum = bidSum*100;
                bidSum = (int) bidSum;
                bidSum = bidSum/100;

                bidderReference.child("mRating").setValue(bidSum);
                bidderReference.child("mRatingCount").setValue(bidderRatingCount);

            }
        });

        // get the accept bid button
        acceptBidButton = (Button) findViewById(R.id.acceptBidButton);
        acceptBidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateJobStatus();

                // add 1 to the jobs completed
                bidderJobsCompleted = bidderJobsCompleted + 1;

                // add the jobs completed
                bidderReference.child("mJobsCompleted").setValue(bidderJobsCompleted);

                String[] TO = {email.get(0)};

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bid war won");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "You won a job from me!");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(BidderProfile.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // helper function to update the accepted field of the job
    private void updateJobStatus(){

        // get a firebase database instance
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // get a reference to the job
        DatabaseReference jobRef = database.getReference("jobs").child(jobAccepted).child("mAccepted");

        // update the reference
        jobRef.setValue(1);

    }

    // helper function to get the job status
    private void getJobStatus(){

        // get a firebase database instance
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // get a reference to the job
        DatabaseReference jobRef = database.getReference("jobs").child(jobAccepted).child("mAccepted");

        // query the database
        jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long status = (long) dataSnapshot.getValue();

                if(status == 1){

                    // job accepted, invalidate the button
                    acceptBidButton.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
