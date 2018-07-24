package tudor.example.jobby.objects;

public class Bid {

    // Bid's variables
    String mUserId;
    String mName;
    String mAmount;

    // empty constructor
    public Bid(){

    }

    // constructor
    public Bid(String userId, String name, String amount){
        mUserId = userId;
        mName = name;
        mAmount = amount;
    }

    // get the user id
    public String getmUserId(){
        return mUserId;
    }

    // get the name
    public String getmName(){
        return mName;
    }

    // get the amount
    public String getmAmount(){
        return mAmount;
    }

}
