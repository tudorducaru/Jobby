package tudor.example.jobby.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import tudor.example.jobby.objects.Job;
import tudor.example.jobby.objects.User;
import tudor.example.jobby.utilities.JobAdapter;
import tudor.example.jobby.R;
import tudor.example.jobby.activities.AddAJob;
import tudor.example.jobby.activities.MyBids;

public class RequestFragment extends Fragment {

    // the View
    private View mFragmentView;

    // requires empty constructor
    public RequestFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // get the View
        mFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        // get the FAB and add the listener
        FloatingActionButton fab = mFragmentView.findViewById(R.id.fabJob);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            startAddAJobActivity();

            }
        });

        // get database reference
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs");

        // get the list view
        ListView requestsListView = (ListView) mFragmentView.findViewById(R.id.requestsListView);

        // ArrayList that will store the requests
        final ArrayList<Job> requestList = new ArrayList<>();

        // get an adapter
        final JobAdapter adapter = new JobAdapter(getActivity(), requestList);

        // ArrayList of push IDs
        final ArrayList<String> pushID = new ArrayList<>();

            // add the value event listener
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // clear the lists
                    pushID.clear();
                    requestList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Job currentJob = child.getValue(Job.class);
                        if (currentJob.getmUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            // add the job
                            requestList.add(currentJob);
                            pushID.add(child.getKey());
                        }
                    }

                    // reverse the arrays
                    Collections.reverse(requestList);
                    Collections.reverse(pushID);

                    // notify the adapter
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        // set a listener on the listView
        requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // create an Intent to go to bids activity
                Intent intent = new Intent(getActivity(), MyBids.class);
                intent.putExtra("key", pushID.get(i));

                // start the intent
                startActivity(intent);
            }
        });

        // set the adapter to the list view
        requestsListView.setAdapter(adapter);

        return mFragmentView;
    }

    // helper function to fire up add a job activity
    private void startAddAJobActivity(){

        // get a reference to the current user's node in the database
        DatabaseReference currentUserReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // query the reference
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // check that data was retrieved
                if(dataSnapshot.getValue() != null){

                    // parse data into a user object
                    User currentUser = dataSnapshot.getValue(User.class);

                    // get the address and the phone number
                    String address = currentUser.getmAddress();
                    String phoneNumber = currentUser.getmPhoneNumber();

                    // check that data was inserted
                    if(address.equals("") || phoneNumber.equals("")){

                        // create an alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });

                        builder.setMessage("Please insert personal information");

                        builder.show();
                    } else {

                        // create intent to go to add a job activity
                        Intent intent = new Intent(getActivity(), AddAJob.class);

                        // fire up the intent
                        startActivity(intent);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
