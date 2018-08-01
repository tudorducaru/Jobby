package tudor.example.jobby.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import tudor.example.jobby.objects.Job;
import tudor.example.jobby.utilities.JobAdapter;
import tudor.example.jobby.R;
import tudor.example.jobby.activities.JobActivity;

public class JobFragment extends Fragment {

    // the View
    private View mFragmentView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    // the load more text view
    private TextView loadMoreTextView;

    // the list view
    private ListView jobListView;

    // category accessed
    public static int categoryIndex = -1;

    // firebase database instance
    private FirebaseDatabase database;

    // database reference instance
    private DatabaseReference jobsReference;

    // requires empty constructor
    public JobFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // get the View
        mFragmentView = inflater.inflate(R.layout.fragment_job, container, false);

        // ArrayList that will store the jobs
        final ArrayList<Job> jobList = new ArrayList<>();

        // ArrayList of push IDs
        final ArrayList<String> pushID = new ArrayList<>();

        // get an adapter
        final JobAdapter adapter = new JobAdapter(getActivity(), jobList);

        // find the ListView
        jobListView = (ListView) mFragmentView.findViewById(R.id.jobsListView);

        // set the adapter to the list view
        jobListView.setAdapter(adapter);

        final ArrayList<String> categories = new ArrayList<>();
        categories.add("IT-Software");
        categories.add("Electrocasnice");
        categories.add("Meseriasi-Constructori");
        categories.add("Cursuri-Meditatii");
        categories.add("Curatenie-Servicii menaj");
        categories.add("Curierat-Servicii auto");
        categories.add("Bone-Babysitter");
        categories.add("Animale de companie");

        // get the firebase database
        database = FirebaseDatabase.getInstance();

        // get the database reference
        jobsReference = database.getReference("jobs");

        if(categoryIndex == -1) {

            // create a query on the reference
            jobsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // hide the progress bar
                    ProgressBar progressBar = mFragmentView.findViewById(R.id.jobs_progress_bar);
                    progressBar.setVisibility(View.GONE);

                    // clear the list
                    pushID.clear();
                    jobList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        Log.i("datasnapshot", dataSnapshot.toString());
                        Log.i("ccchild", child.toString());

                        // parse data into a job object
                        Job currentJob = child.getValue(Job.class);

                        // hide accepted jobs
                        int status = currentJob.getmAccepted();
                        if(status == 0){
                            // add the job
                            jobList.add(currentJob);
                            pushID.add(child.getKey());
                        }
                    }

                    // reverse the arrays to show descending order
                    Collections.reverse(jobList);
                    Collections.reverse(pushID);

                    // notify the adapter
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {

            // create a query on the database
            Query categoryQuery = jobsReference.orderByChild("mCategory").equalTo(categories.get(categoryIndex));
            categoryQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // hide the progress bar
                    ProgressBar progressBar = mFragmentView.findViewById(R.id.jobs_progress_bar);
                    progressBar.setVisibility(View.GONE);

                    // clear the list
                    pushID.clear();
                    jobList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Job currentJob = child.getValue(Job.class);
                        if(categoryIndex != -1) {
                            if (currentJob.getmCategory().equals(categories.get(categoryIndex))) {

                                // add the job
                                jobList.add(currentJob);
                                pushID.add(child.getKey());
                            }
                        }
                    }

                    // notify the adapter
                    adapter.notifyDataSetChanged();

                    // reverse the arrays to show descending order
                    Collections.reverse(jobList);
                    Collections.reverse(pushID);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // set the listener on the listView
        jobListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // create an intent to go to job activity
                Intent intent = new Intent(getActivity(), JobActivity.class);
                intent.putExtra("key", pushID.get(i));

                // start the activity
                startActivity(intent);

            }
        });

        return mFragmentView;
    }

}
