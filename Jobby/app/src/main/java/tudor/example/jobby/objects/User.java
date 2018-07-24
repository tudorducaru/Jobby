package tudor.example.jobby.objects;

public class User {

    // class's variables
    private double mRating;
    private int mRatingCount;
    private int mJobsCompleted;
    private String mProfilePicUrl;
    private String mEmail;
    private String mAddress;
    private int mPhoneNumber;

    // empty constructor
    public User(){

    }

    // the constructor
    public User(double rating, int ratingCount, int jobsCompleted, String profilePicUrl, String email, String address, int phoneNumber){

        // asign the parameters to the variables
        mRating = rating;
        mRatingCount = ratingCount;
        mJobsCompleted = jobsCompleted;
        mProfilePicUrl = profilePicUrl;
        mEmail = email;
        mAddress = address;
        mPhoneNumber = phoneNumber;

    }

    // get the rating
    public double getmRating(){
        return mRating;
    }

    // get the rating count
    public int getmRatingCount(){
        return mRatingCount;
    }

    // get the number of jobs completed
    public int getmJobsCompleted(){
        return mJobsCompleted;
    }

    // get the profile pic download url
    public String getmProfilePicUrl(){
        return mProfilePicUrl;
    }

    // get the email
    public String getmEmail(){
        return mEmail;
    }

    public String getmAddress() {
        return mAddress;
    }

    public int getmPhoneNumber() {
        return mPhoneNumber;
    }
}
