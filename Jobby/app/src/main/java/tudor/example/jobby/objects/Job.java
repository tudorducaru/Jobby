package tudor.example.jobby.objects;

public class Job {

    // class's variables
    private String mUserID;
    private String mTitle;
    private String mDescription;
    private String mAddress;
    private String mPhoneNumber;
    private String mImageUrl;
    private int mAccepted;
    private String mCategory;

    // constructor with no arguments
    public Job(){

    }

    // the constructor
    public Job(String userID, String title, String description, String address, String phoneNumber, int accepted, String imageUrl, String category){

        // set the object's variables
        mUserID = userID;
        mTitle = title;
        mDescription = description;
        mAddress = address;
        mPhoneNumber = phoneNumber;
        mAccepted = accepted;
        mImageUrl = imageUrl;
        mCategory = category;
    }

    // get the userID
    public String getmUserID(){
        return mUserID;
    }

    // get the title
    public String getmTitle(){
        return mTitle;
    }

    // get the description
    public String getmDescription(){
        return mDescription;
    }

    // get the address
    public String getmAddress(){
        return mAddress;
    }

    // get the phone number
    public String getmPhoneNumber(){
        return mPhoneNumber;
    }

    // get the status
    public int getmAccepted(){
        return mAccepted;
    }

    // get the image url
    public String getmImageUrl(){
        return mImageUrl;
    }

    // get the category
    public String getmCategory(){
        return mCategory;
    }

}
