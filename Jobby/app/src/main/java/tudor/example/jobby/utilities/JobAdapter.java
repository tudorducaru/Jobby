package tudor.example.jobby.utilities;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tudor.example.jobby.R;
import tudor.example.jobby.objects.Job;

public class JobAdapter extends ArrayAdapter {

    // constructor
    public JobAdapter (Activity context , ArrayList<Job> jobs){
        super(context, 0, jobs);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.job_list_item, parent, false);
        }

        // get the current job from the list
        Job currentJob = (Job) getItem(position);

        // get the title and the address of the job
        String title = currentJob.getmTitle();
        String address = currentJob.getmAddress();

        // get the TextViews
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.job_title_listItem);
        TextView addressTextView = (TextView) listItemView.findViewById(R.id.job_address_listItem);

        // set the text in the TextViews
        titleTextView.setText(title);
        addressTextView.setText(address);

        // return the list item
        return listItemView;

    }
}
