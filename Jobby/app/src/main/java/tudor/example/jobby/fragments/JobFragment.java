package tudor.example.jobby.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tudor.example.jobby.objects.Job;
import tudor.example.jobby.utilities.JobAdapter;
import tudor.example.jobby.R;
import tudor.example.jobby.activities.JobActivity;

public class JobFragment extends Fragment {

    // the View
    private View mFragmentView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    // category accessed
    public static int categoryIndex = -1;

    // requires empty constructor
    public JobFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // get the View
        mFragmentView = inflater.inflate(R.layout.fragment_job, container, false);

        // find the ListView
        ListView jobListView = (ListView) mFragmentView.findViewById(R.id.jobsListView);

        // ArrayList that will store the jobs
        final ArrayList<Job> jobList = new ArrayList<>();

        // ArrayList of push IDs
        final ArrayList<String> pushID = new ArrayList<>();

        // get an adapter
        final JobAdapter adapter = new JobAdapter(getActivity(), jobList);

        final ArrayList<String> categories = new ArrayList<>();
        categories.add("Sanitary");
        categories.add("Joinery");
        categories.add("Engineering");
        categories.add("Appliance");
        categories.add("IT/Software");

        // get a database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs");
        if(categoryIndex == -1) {
            databaseReference.addValueEventListener(new ValueEventListener() {
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
                        jobList.add(currentJob);
                        adapter.notifyDataSetChanged();
                        pushID.add(child.getKey());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            databaseReference.addValueEventListener(new ValueEventListener() {
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
                                jobList.add(currentJob);
                                adapter.notifyDataSetChanged();
                                pushID.add(child.getKey());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        // set the adapter to the list view
        jobListView.setAdapter(adapter);

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
