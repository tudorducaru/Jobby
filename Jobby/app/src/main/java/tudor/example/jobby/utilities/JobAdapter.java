package tudor.example.jobby.utilities;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tudor.example.jobby.R;
import tudor.example.jobby.objects.Job;

public class JobAdapter extends ArrayAdapter {

    // constructor
    public JobAdapter (Activity context , ArrayList<Job> jobs){
        super(context, 0, jobs);
    }

    // the image view of the icon
    private ImageView iconImageView;

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

        // get the job details
        String title = currentJob.getmTitle();
        String address = currentJob.getmAddress();
        String category = currentJob.getmCategory();

        // get the views
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.job_title_listItem);
        TextView addressTextView = (TextView) listItemView.findViewById(R.id.job_address_listItem);
        iconImageView = listItemView.findViewById(R.id.job_icon_listitem);

        // set the text in the TextViews
        titleTextView.setText(title);
        addressTextView.setText(address);

        setIcon(category);

        // return the list item
        return listItemView;

    }

    // helper function to set the icon according to the category
    private void setIcon(String category){

        if(category.equals("IT-Software")){
            iconImageView.setBackgroundResource(R.drawable.categorie_itsoftware);
        } else if(category.equals("Electrocasnice")){
            iconImageView.setBackgroundResource(R.drawable.categorie_electrocasnice);
        } else if(category.equals("Meseriasi-Constructori")){
            iconImageView.setBackgroundResource(R.drawable.categorie_constructii);
        } else if(category.equals("Cursuri-Meditatii")){
            iconImageView.setBackgroundResource(R.drawable.categorie_cursuri);
        } else if(category.equals("Curatenie-Servicii menaj")){
            iconImageView.setBackgroundResource(R.drawable.categorie_curatenie);
        } else if(category.equals("Curierat-Servicii auto")){
            iconImageView.setBackgroundResource(R.drawable.categorie_livrari);
        } else if(category.equals("Bone-Babysitter")){
            iconImageView.setBackgroundResource(R.drawable.categorie_babysitter);
        } else if(category.equals("Animale de companie")){
            iconImageView.setBackgroundResource(R.drawable.categorie_animale);
        }

    }

}
