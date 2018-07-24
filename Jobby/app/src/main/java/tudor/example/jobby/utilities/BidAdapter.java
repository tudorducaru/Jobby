package tudor.example.jobby.utilities;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tudor.example.jobby.R;
import tudor.example.jobby.objects.Bid;

public class BidAdapter extends ArrayAdapter {

    // constructor
    public BidAdapter (Activity context , ArrayList<Bid> bids){
        super(context, 0, bids);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.bid_list_item, parent, false);
        }

        // get the current job from the list
        final Bid currentBid = (Bid) getItem(position);

        // get the title and the address of the job
        final String name = currentBid.getmName();
        String amount = currentBid.getmAmount();

        // get the TextViews
        final TextView nameTextView = (TextView) listItemView.findViewById(R.id.bidListItemName);
        TextView amountTextView = (TextView) listItemView.findViewById(R.id.bidListItemAmount);

        // set the text in the TextViews
        nameTextView.setText(name);
        amountTextView.setText(amount + "RON");

        // get the id of the user who placed the bid
        String bidderID = currentBid.getmUserId();

        // get a reference to the user's rating
        DatabaseReference ratingReference = FirebaseDatabase.getInstance().getReference("users").child(bidderID).child("mRating");

        // query the reference
        ratingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get the rating
                String bidderRating = String.valueOf(dataSnapshot.getValue());

                // set the value in the text view
                nameTextView.setText(name + " ⭐" + bidderRating);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // return the list item
        return listItemView;

    }
}
