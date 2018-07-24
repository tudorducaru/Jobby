package tudor.example.jobby.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tudor.example.jobby.objects.Bid;
import tudor.example.jobby.utilities.BidAdapter;
import tudor.example.jobby.objects.Job;
import tudor.example.jobby.R;

public class JobActivity extends AppCompatActivity {

    // the Views
    private TextView titleTextView;
    private TextView addressTextView;
    private TextView descriptionTextView;
    private Button callButton;
    private Button placeBidButton;

    // dialog's views
    private EditText dialogBidName;
    private EditText dialogBidAmount;

    // bids label text view
    private TextView bidsLabelTextView;

    // the ListView
    private ListView bidsListView;

    // the Toolbar
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("Job details");

        // ArrayList that will have the phone number
        final ArrayList<String> phoneNumber = new ArrayList<>();

        // get the intent
        Intent intent = getIntent();

        // get the id of the current job
        final String key = intent.getStringExtra("key");

        // find the views
        titleTextView = (TextView) findViewById(R.id.jobTitleTextView);
        addressTextView = (TextView) findViewById(R.id.jobAddressTextView);
        descriptionTextView = (TextView) findViewById(R.id.jobDescriptionTextView);
        callButton = (Button) findViewById(R.id.jobCallButton);
        placeBidButton = (Button) findViewById(R.id.jobPlaceBidButton);
        bidsLabelTextView = findViewById(R.id.job_bid_label_text_view);

        // get a database reference
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs");

        // set a listener
        databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Job currentJob = dataSnapshot.getValue(Job.class);
                    titleTextView.setText(currentJob.getmTitle());
                    addressTextView.setText(currentJob.getmAddress());
                    descriptionTextView.setText(currentJob.getmDescription());
                    phoneNumber.add(currentJob.getmPhoneNumber());

                    ImageView imageView = (ImageView) findViewById(R.id.jobActivityImageView);
                    String downloadUrl = currentJob.getmImageUrl();
                    Glide.with(getApplicationContext())
                            .load(downloadUrl)
                            .into(imageView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // place a bid
        placeBidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Inflate the layout
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_bid,null);
                alert.setView(layout);

                // find the views
                dialogBidAmount = (EditText) layout.findViewById(R.id.bidDialogAmount);

                // set the positive button
                alert.setPositiveButton("Place", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // get the values
                        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        String amount = dialogBidAmount.getText().toString();

                        // get the user's id
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // form the Bid object
                        Bid bid = new Bid(userId, name, amount);

                        // add the bid
                        databaseReference.child(key).child("bids").push().setValue(bid);

                        dialogInterface.dismiss();
                    }
                });

                // set the negative button
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.setCancelable(false);

                // Create and show the AlertDialog
                final AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });

        // find the ListView
        bidsListView = (ListView) findViewById(R.id.jobBidsListView);

        bidsListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        // array list that will store the bids
        final ArrayList<Bid> bids = new ArrayList<>();

        // get an adapter
        final BidAdapter adapter = new BidAdapter(this, bids);

        // get a database reference
        final DatabaseReference bidReference = databaseReference.child(key).child("bids");
        bidReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // clear the list
                bids.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    // get the current bid
                    Bid currentBid = child.getValue(Bid.class);
                    bids.add(currentBid);
                }

                // if there are no bids, hide the label
                if(bids.size() == 0){
                    bidsLabelTextView.setVisibility(View.GONE);
                } else {
                    bidsLabelTextView.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // set the adapter to the listview
        bidsListView.setAdapter(adapter);

        // initiate a call
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber.get(0), null));
                startActivity(intent);
            }
        });

    }

}
