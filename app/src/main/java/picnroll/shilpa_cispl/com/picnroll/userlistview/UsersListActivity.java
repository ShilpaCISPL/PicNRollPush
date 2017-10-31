package picnroll.shilpa_cispl.com.picnroll.userlistview;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.NavActivity;

public class UsersListActivity extends AppCompatActivity {


    List<DataAdapter> ListOfdataAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    RecyclerView.Adapter recyclerViewadapter;
    ArrayList<String> ImageTitleNameArrayListForClick;
    private Firebase mRef, mRef1;
    private DatabaseReference mDatabase;
    ArrayList<String> userName = new ArrayList<>();
    ArrayList<String> profileImageUrl = new ArrayList<>();
    ArrayList<String> userIdArray = new ArrayList<>();
    ArrayList<String> imageKeysFromDB = new ArrayList<>();
    ArrayList<String> imageUrlFromDB = new ArrayList<>();
    ArrayList<String> shareImageUrls = new ArrayList<>();
    ArrayList<String> usersEmail = new ArrayList<>();
    ArrayList<String> sharedFolderUserId = new ArrayList<>();
    ArrayList<String> sharedUsersIdArray = new ArrayList<>();
    ArrayList<String> DevicePhoneNumbersArray = new ArrayList<>();
    ArrayList<String> DBPhoneNumbersArray = new ArrayList<>();
    ArrayList<String> inviteFriendsArray = new ArrayList<>();
    ArrayList<Integer> inviteFriendsArrayImage = new ArrayList<>();
    String userId, selectedFolderName;
    public static final int REQUEST_READ_CONTACTS = 79;
    String formatPhoneNumber;
    ArrayList<String> SortedArray = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Firebase.setAndroidContext(this);

        selectedFolderName = getIntent().getStringExtra("selectedFolderName");

        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();


        mDatabase = FirebaseDatabase.getInstance().getReference();
        ImageTitleNameArrayListForClick = new ArrayList<>();
        ListOfdataAdapter = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview1);
        recyclerView.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);
        final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        //Read profile imageurl and username
        mRef = new Firebase("https://androidpicnroll.firebaseio.com/Users");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    mProgressDialog.dismiss();
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        usersEmail.add(String.valueOf(childDataSnapshot.child("Email").getValue()));
                        profileImageUrl.add(String.valueOf(childDataSnapshot.child("profileImageUrl").getValue()));
                        DBPhoneNumbersArray.add(String.valueOf(childDataSnapshot.child("PhoneNumber").getValue()));

                        //Check contacts access permission
                        if (ActivityCompat.checkSelfPermission(UsersListActivity.this, android.Manifest.permission.READ_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {
                            SortedArray = getContacts(DBPhoneNumbersArray);
                            Log.d("tag","sortedarr--"+SortedArray.toString());

                            //Delete duplicate entries from Array
                            Object[] st = SortedArray.toArray();
                            for (Object s : st) {
                                if (SortedArray.indexOf(s) != SortedArray.lastIndexOf(s)) {
                                    SortedArray.remove(SortedArray.lastIndexOf(s));
                                }
                            }
                            //Get only names of phonenumber which are there in phone and DB
                            for (int i = 0; i < SortedArray.size(); i++) {
                             //   inviteFriendsArray = SortedArray;
                                if ((SortedArray.get(i).contains(String.valueOf(childDataSnapshot.child("PhoneNumber").getValue())))) {
                                    userName.add(String.valueOf(childDataSnapshot.child("Name").getValue()));

                                    userIdArray.add(childDataSnapshot.getKey());
                                    Log.d("tag","tess--"+userName.toString() +String.valueOf(childDataSnapshot.child("PhoneNumber").getValue()));


//                                    //Phone numbers who are not using PicNRoll App
//                                    inviteFriendsArray.remove(SortedArray.get(i));
//                                    inviteFriendsArrayImage.add(R.drawable.folder);
//
//                                    JSONArray jArray = new JSONArray(inviteFriendsArray);
//                                    ParseJSonPhone(jArray);
                                }

                            }

                        } else {
                            requestLocationPermission();
                        }

                    }
                    JSONArray jsArray = new JSONArray(userName);
                    ParseJSonResponse(jsArray);


                } else {
                    mProgressDialog.show();
                }


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Read imageurls of loggedin user
        mRef1 = new Firebase("https://pick-n-roll.firebaseio.com/Files/" + userId + "");

        mRef1.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    imageKeysFromDB.add(childDataSnapshot.getKey());
                    imageUrlFromDB.add(String.valueOf(childDataSnapshot.getValue()));
                }
                for (int j = 0; j < imageKeysFromDB.size(); j++) {
                    if (imageKeysFromDB.get(j).contains(selectedFolderName)) {
                        shareImageUrls.add(imageUrlFromDB.get(j));

                    } else {

                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Get shareduserdid's
        DatabaseReference shareduseref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference sharedRef = shareduseref.child("SharedUsers").child(userId).child(String.valueOf(selectedFolderName));
        sharedRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            //data exists, do something
                            for (com.google.firebase.database.DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                                Object obj = objSnapshot.getValue();
                                sharedFolderUserId.add(String.valueOf(obj));

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }


    protected void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!

        } else {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getContacts(DBPhoneNumbersArray);

                } else {

                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }

    }


    //Read all contacts
    public ArrayList<String> getContacts(ArrayList<String> DBPhoneNumbersArray) {
        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        StringBuffer output = new StringBuffer();

        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                    }
                    phoneCursor.close();

                }
                formatPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");
                DevicePhoneNumbersArray.add(formatPhoneNumber);
            }
        }
        return DevicePhoneNumbersArray;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_users_list, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // finish();
                Intent gotonav = new Intent(UsersListActivity.this, NavActivity.class);
                startActivity(gotonav);
                return true;
            case R.id.action_share:

                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                // Add data to the intent, the receiving app will decide
                // what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, "Title Of The Post");
                share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/search?q=happeninz&hl=en");

                startActivity(Intent.createChooser(share, "Share link!"));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void ParseJSonResponse(JSONArray array) {
        for (int i = 0; i < userName.size(); i++) {
            DataAdapter GetDataAdapter2 = new DataAdapter();
            GetDataAdapter2.setImageTitle(userName.get(i));
            GetDataAdapter2.setImageUrl(profileImageUrl.get(i));
            ListOfdataAdapter.add(GetDataAdapter2);
        }
        recyclerViewadapter = new RecyclerViewAdapter(ListOfdataAdapter, this);
        recyclerView.setAdapter(recyclerViewadapter);
    }

    //Display Phone numbers who are not using PicNRoll App
    public void ParseJSonPhone(JSONArray array) {
        for (int i = 0; i < inviteFriendsArray.size(); i++) {
            DataAdapter GetDataAdapter2 = new DataAdapter();
            GetDataAdapter2.setImageTitle(inviteFriendsArray.get(i));
            GetDataAdapter2.setImageUrl("http://findicons.com/files/icons/808/on_stage/128/symbol_add.png");
            ListOfdataAdapter.add(GetDataAdapter2);
        }
        recyclerViewadapter = new RecyclerViewAdapter(ListOfdataAdapter, this);
        recyclerView.setAdapter(recyclerViewadapter);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        Context context;
        List<DataAdapter> dataAdapters;
        ImageLoader imageLoader;

        public RecyclerViewAdapter(List<DataAdapter> getDataAdapter, Context context) {
            super();
            this.dataAdapters = getDataAdapter;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }


        @Override
        public void onBindViewHolder(ViewHolder Viewholder, int position) {

            DataAdapter dataAdapterOBJ = dataAdapters.get(position);

            imageLoader = ImageAdapter.getInstance(context).getImageLoader();

            imageLoader.get(dataAdapterOBJ.getImageUrl(),
                    ImageLoader.getImageListener(
                            Viewholder.VollyImageView,//Server Image
                            R.mipmap.ic_launcher,//Before loading server image the default showing image.
                            android.R.drawable.ic_dialog_alert //Error image if requested image dose not found on server.
                    )
            );


            Viewholder.VollyImageView.setImageUrl(dataAdapterOBJ.getImageUrl(), imageLoader);

            StringBuilder sb = new StringBuilder(dataAdapterOBJ.getImageTitle());
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            Viewholder.ImageTitleTextView.setText(sb);
        }

        @Override
        public int getItemCount() {

            return dataAdapters.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView ImageTitleTextView;
            public NetworkImageView VollyImageView;

            public ViewHolder(final View itemView) {

                super(itemView);

                ImageTitleTextView = (TextView) itemView.findViewById(R.id.ImageNameTextView);

                VollyImageView = (NetworkImageView) itemView.findViewById(R.id.VolleyImageView);
//
//                VollyImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        getLayoutPosition();
//                        Log.d("tag","position is--"+usersEmail.get(getLayoutPosition()));
//
//
//                        Intent email = new Intent(Intent.ACTION_SEND);
//                        email.putExtra(Intent.EXTRA_SUBJECT, "PicNRoll");
//                        email.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/search?q=happeninz&hl=en");
//
//                        //need this to prompts email client only
//                        email.setType("message/rfc822");
//
//                        startActivity(Intent.createChooser(email, "Choose an Email client :"));
//                    }
//                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int pos = getAdapterPosition();

                        if (sharedFolderUserId.contains(userIdArray.get(pos))) {

                            LayoutInflater li = LayoutInflater.from(UsersListActivity.this);
                            View promptsView = li.inflate(R.layout.userslist_popup, null);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    UsersListActivity.this);

                            // set prompts.xml to alertdialog builder
                            alertDialogBuilder.setView(promptsView);

                            Button btn_ok = (Button) promptsView.findViewById(R.id.ok);
                            Button btn_cancel = (Button) promptsView.findViewById(R.id.cancel);


                            final AlertDialog alertDialog = alertDialogBuilder.create();

                            btn_ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                }
                            });

                            btn_cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                }
                            });


                            // show it
                            alertDialog.show();

                        } else {

                            mDatabase.child("Albums").child(userIdArray.get(pos)).child(String.valueOf(UUID.randomUUID())).setValue(selectedFolderName);
                            mDatabase.child("SharedUsers").child(userId).child(selectedFolderName).child(String.valueOf(UUID.randomUUID())).setValue(userIdArray.get(pos));
                            for (int m = 0; m < shareImageUrls.size(); m++) {
                                mDatabase.child("Files").child(userIdArray.get(pos)).child(String.valueOf(UUID.randomUUID())).setValue(shareImageUrls.get(m));

                            }

                        }

                    }
                });

            }


        }
    }


    @Override
    public void onBackPressed() {
        // do nothing.
    }


}



