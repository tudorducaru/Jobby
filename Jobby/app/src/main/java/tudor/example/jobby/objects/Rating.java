package tudor.example.jobby.objects;


public class Rating {

    // class's fields
    String mName;
    String mRating;

    // empty constructor
    public Rating(){

    }

    // the constructor
    public Rating(String name, String rating){
        mName = name;
        mRating = rating;
    }

    // get the name
    public String getmName(){
        return mName;
    }

    // get the rating of the user
    public String getmRating(){
        return mRating;
    }

}
