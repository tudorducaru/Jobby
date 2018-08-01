package tudor.example.jobby.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tudor.example.jobby.objects.Bid;
import tudor.example.jobby.utilities.BidAdapter;
import tudor.example.jobby.R;

public class MyBids extends AppCompatActivity {

    // the Toolbar
    private Toolbar mToolbar;

    // the key
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bids);

        // set the Toolbar and the title
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("Bids on my request");

        // get the intent and the key
        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        // ArrayList that will contain the bids
        final ArrayList<Bid> bidList = new ArrayList<>();

        // find the list
        ListView bidListView = (ListView) findViewById(R.id.bidsListView);

        // get the adapter
        final BidAdapter adapter = new BidAdapter(this, bidList);

        // get a database reference
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs").child(key);

        // get the elements in the database
        databaseReference.child("bids").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // clear the list
                bidList.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    // get the current bid
                    Bid currentBid = child.getValue(Bid.class);
                    bidList.add(currentBid);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // the alert dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        // set the listview's listener
        bidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // get the bid clicked
                Bid clickedBid = bidList.get(i);

                // get the id and the name of the user that made the bid
                String bidderId = clickedBid.getmUserId();
                String bidderName = clickedBid.getmName();

                // create an intent to go bidder's profile
                Intent intent = new Intent(getApplicationContext(), BidderProfile.class);

                // put bidder's id and name in the intent
                intent.putExtra("name", bidderName);
                intent.putExtra("id", bidderId);

                // put the key of the job in the intent
                intent.putExtra("key", key);

                // start the activity
                startActivity(intent);
            }
        });

        // set the list view's adapter
        bidListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mybids_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs");

        if(item.getItemId() == R.id.myBids_menu_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to delete this request?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    databaseReference.child(key).removeValue();
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
