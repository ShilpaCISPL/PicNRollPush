package picnroll.shilpa_cispl.com.picnroll;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;

import picnroll.shilpa_cispl.com.picnroll.navigationFiles.NavActivity;

public class ProfileActivity extends AppCompatActivity {

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private TextView textViewUserEmail,textViewUserName;

    ImageView profileImage;
    private Firebase mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the user is not logged in
        //that means current user will return null
        if(firebaseAuth.getCurrentUser() == null){
            //closing this activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LoginActivity.class));
        }

        //getting current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
       String userId = currentFirebaseUser.getUid();

        //initializing views
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        textViewUserName = (TextView) findViewById(R.id.textViewName);
        profileImage = (ImageView) findViewById(R.id.circleProfile);

        //displaying logged in user name
        textViewUserName.setText(user.getDisplayName());
        textViewUserEmail.setText(user.getEmail());


        //Read profile image url from firebase
        mRef = new Firebase("https://androidpicnroll.firebaseio.com/Users/" + userId + "/profileImageUrl");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
               String profileImageUrl = (String) dataSnapshot.getValue();
                Log.d("tag","profile--"+profileImageUrl);
                 new DownloadImage().execute(profileImageUrl);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    // DownloadImage AsyncTask
    class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {
            String imageURL = URL[0];
            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            //  image.setImageBitmap(result);

            profileImage.setImageBitmap(result);

        }
    }



}
