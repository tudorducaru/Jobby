package tudor.example.jobby.utilities;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("firebase token", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    // helper function to send the firebase registration token to the database
    private void sendRegistrationToServer(String token){

        // get a firebase instances
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // get the current user's id
        //String currentUserID = auth.getCurrentUser().getUid();

        // create a reference to the user's node in the database
        //DatabaseReference userRef = database.getReference("users").child(currentUserID);

        // update the token
        //userRef.child("firebaseToken").setValue(token);

    }
}


