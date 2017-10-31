package picnroll.shilpa_cispl.com.picnroll.navigationFiles;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import picnroll.shilpa_cispl.com.picnroll.LoginActivity;

import picnroll.shilpa_cispl.com.picnroll.ProfileActivity;
import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.customgallery.FolderImagesActivity;
import picnroll.shilpa_cispl.com.picnroll.folderimages.FolderImages;
import picnroll.shilpa_cispl.com.picnroll.galleries.GalleryListAdapter;
import picnroll.shilpa_cispl.com.picnroll.userlistview.UsersListActivity;


public class NavActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    FloatingActionButton fab;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    String userId, profileImageUrl;
    String menuItemTime;


    //firebase auth object
    private FirebaseAuth firebaseAuth;
    private Firebase mRef, mSharedID, mShareToken;
    private DatabaseReference mDatabase;

    ImageView profileImage;
    ArrayList<String> imageKeys = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    ArrayList<String> sharedUsersIdArray = new ArrayList<>();
    ArrayList<String> sharedUserToken = new ArrayList<>();
    ArrayList<String> selectedFolderImages = new ArrayList<>();
    ArrayList<String> strArr = new ArrayList<>();
    ArrayList<Integer> Folder_images = new ArrayList<Integer>();

    ListView lv_gallery_names;
    GalleryListAdapter list_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        Firebase.setAndroidContext(this);
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();
        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        mTitle = mDrawerTitle = getTitle();
        final ProgressDialog mProgressDialog = new ProgressDialog(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.header_list, null, false);
        profileImage = (ImageView) listHeaderView.findViewById(R.id.circleView);
        lv_gallery_names = (ListView) findViewById(R.id.lv_languages);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        final String deviceRefreshedToken = updateToken();

        //Read profile image url from firebase
        mRef = new Firebase("https://androidpicnroll.firebaseio.com/Users/" + userId + "/profileImageUrl");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                profileImageUrl = (String) dataSnapshot.getValue();
                new DownloadImage().execute(profileImageUrl);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mDrawerList.addHeaderView(listHeaderView);

        ArrayList<ItemObject> listViewItems = new ArrayList<ItemObject>();

        listViewItems.add(new ItemObject("Gallery", R.drawable.appicon));
        listViewItems.add(new ItemObject("MyProfile", R.drawable.myprofile1));
        listViewItems.add(new ItemObject("Map", R.drawable.map1));
        listViewItems.add(new ItemObject("Logout", R.drawable.logout));

        mDrawerList.setAdapter(new CustomAdapter(this, listViewItems));
        View v = (View) findViewById(R.id.listFolders);
        v.setVisibility(View.VISIBLE);
        mDrawerToggle = new ActionBarDrawerToggle(NavActivity.this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                View v = (View) findViewById(R.id.listFolders);
                v.setVisibility(View.VISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                View v = (View) findViewById(R.id.listFolders);
                v.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };


        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFragment(position);
            }
        });

        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRef = new Firebase("https://androidpicnroll.firebaseio.com/Albums/" + userId + "");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                strArr.clear();
                if (dataSnapshot.getChildrenCount() == 0) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(NavActivity.this);
                    builder1.setTitle("No folders");
                    builder1.setMessage("Add New Folder");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    mProgressDialog.dismiss();

                                }
                            });

                    builder1.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    builder1.setIcon(R.drawable.appicon);
                    builder1.show();

                } else {
                    mProgressDialog.dismiss();

                    for (com.firebase.client.DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        strArr.add(String.valueOf(childSnapshot.getValue()));
                        Folder_images.add(R.drawable.folder);

                    }

                    list_adapter = new GalleryListAdapter(NavActivity.this, strArr, Folder_images);
                    lv_gallery_names.setAdapter(list_adapter);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        //Get all imagekeys at your "Files" root node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference countRef = ref.child("Files").child(userId);
        countRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                                Object obj = objSnapshot.getKey();
                                imageKeys.add(String.valueOf(obj));
                                imageUrl.add(String.valueOf(objSnapshot.getValue()));
                            }
                        } else {

                            imageKeys = null;
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        lv_gallery_names.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {

////GET ALL SHAREDUSERID
//                mSharedID = new Firebase("https://androidpicnroll.firebaseio.com/SharedUsers/" + userId + "/" + strArr.get(i) + "");
//
//                mSharedID.addValueEventListener(new com.firebase.client.ValueEventListener() {
//                    @Override
//                    public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
//
//                        if (dataSnapshot.exists()) {
//                            for (com.firebase.client.DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//
//                                sharedUsersIdArray.add(String.valueOf(childDataSnapshot.getValue()));
//                                mShareToken = new Firebase("https://androidpicnroll.firebaseio.com/Users/" + String.valueOf(childDataSnapshot.getValue()) + "/deviceToken");
//                                mShareToken.addValueEventListener(new com.firebase.client.ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(com.firebase.client.DataSnapshot snapshot) {
//                                        sharedUserToken.add(String.valueOf(snapshot.getValue()));
//                                    }
//
//                                    @Override
//                                    public void onCancelled(FirebaseError firebaseError) {
//
//                                    }
//                                });
//                            }
//                        } else {
//                            Log.d("tag", "NODATA");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(FirebaseError firebaseError) {
//
//                    }
//                });
//
//                AlertDialog.Builder builder1 = new AlertDialog.Builder(NavActivity.this);
//                builder1.setMessage("Want to share folder?");
//                builder1.setCancelable(false);
//
//                builder1.setNegativeButton(
//                        "No",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//
//                                //Get all shared user's usedId
//                                //Get all imagekeys at your "Files" root node
//                                Intent uploadphoto = new Intent(NavActivity.this, FolderImagesActivity.class);
//                                uploadphoto.putExtra("selectedFolderName", strArr.get(i));
//                                uploadphoto.putExtra("selectedFolderPosition", String.valueOf(i));
//                                uploadphoto.putExtra("deviceRefreshedToken", deviceRefreshedToken);
//                                uploadphoto.putStringArrayListExtra("sharedUsersIdArray", sharedUsersIdArray);
//                                uploadphoto.putStringArrayListExtra("imageKeys", imageKeys);
//                                uploadphoto.putStringArrayListExtra("imageUrl", imageUrl);
//                                uploadphoto.putStringArrayListExtra("sharedUserToken", sharedUserToken);
//
//                                startActivity(uploadphoto);
//
//                                dialog.cancel();
//                            }
//                        });
//
//                builder1.setPositiveButton(
//                        "Yes",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                ArrayList<String> sortedArr = new ArrayList<String>();
//                                if (imageKeys != null) {
//                                    for (int k = 0; k < imageKeys.size(); k++) {
//                                        if ((imageKeys.get(k).contains((strArr.get(i))))) {
//                                            sortedArr.add(imageKeys.get(k));
//                                        }
//                                    }
//                                }
//                                    if (sortedArr.size() > 0){
//
//                                        Intent userlist = new Intent(NavActivity.this, UsersListActivity.class);
//                                        userlist.putExtra("selectedFolderName", strArr.get(i));
//                                        startActivity(userlist);
//                                    }
//                                    else {
//
//                                        Intent uploadphoto = new Intent(NavActivity.this, FolderImagesActivity.class);
//                                            uploadphoto.putExtra("selectedFolderName", strArr.get(i));
//                                            uploadphoto.putExtra("selectedFolderPosition", String.valueOf(i));
//                                            uploadphoto.putExtra("deviceRefreshedToken", deviceRefreshedToken);
//                                            uploadphoto.putStringArrayListExtra("sharedUsersIdArray", sharedUsersIdArray);
//                                            startActivity(uploadphoto);
//
//                                    }
//
//                                dialog.cancel();
//                            }
//                        });
//
//                builder1.setIcon(R.drawable.appicon);
//                builder1.show();
            }
        });

    }

    private void selectItemFragment(int position) {
        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {

            case 1:

//                Intent ii = new Intent(NavActivity.this, DashboardActivity.class);
//                startActivity(ii);
                //fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).commit();

                break;
            case 2:
//                View v = (View) findViewById(R.id.listFolders);
//                v.setVisibility(View.INVISIBLE);
//                fragment = new DefaultFragment();
//                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).commit();

                Intent i = new Intent(NavActivity.this, ProfileActivity.class);
                startActivity(i);
                break;
            case 3:
                Intent iii = new Intent(NavActivity.this, MapsActivity.class);
                startActivity(iii);
                break;

            case 4:
                //logging out the user
                firebaseAuth.signOut();
                //closing activity
                finish();
                //starting login activity
                Intent profile = new Intent(NavActivity.this, LoginActivity.class);
                startActivity(profile);
                break;
        }

        mDrawerList.setItemChecked(position, true);
//        setTitle(titles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    public void onClick(View view) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_folder_nav_popup, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                mDatabase.child("Albums").child(userId).child(String.valueOf(String.valueOf(UUID.randomUUID()))).setValue(userInput.getText().toString());
                                //  result.setText(userInput.getText());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
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

    private String updateToken() {
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myref = database.getReference();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myref.child("Users").child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().child("deviceToken").setValue(refreshedToken);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("User", databaseError.getMessage());
            }
        });

        return refreshedToken;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


//Set notification timing
        if (id == R.id.eight_hours) {
            menuItemTime = String.valueOf(3600000);
        } else if (id == R.id.sixteen_hours) {
            menuItemTime = String.valueOf(57600000);

        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class GalleryListAdapter extends BaseAdapter {
        ArrayList<String> result = new ArrayList<>();
        Context context;
        ArrayList<Integer> imageId;
        private LayoutInflater inflater = null;

        public GalleryListAdapter(NavActivity mainActivity, ArrayList prgmNameList, ArrayList<Integer> prgmImages) {
// TODO Auto-generated constructor stub
            result = prgmNameList;
            context = mainActivity;
            imageId = prgmImages;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
// TODO Auto-generated method stub
            return result.size();
        }

        @Override
        public Object getItem(int position) {
// TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
// TODO Auto-generated method stub
            return position;
        }

        public class Holder {
            TextView tv_language;
            ImageView im_folder;
            Button btn_view;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
            Holder holder = new Holder();
            View view;
            view = inflater.inflate(R.layout.layout_gallery_list, null);

            holder.tv_language = (TextView) view.findViewById(R.id.tv_language);
            holder.im_folder = (ImageView) view.findViewById(R.id.im_folder);
            holder.btn_view = (Button) view.findViewById(R.id.btn_view);

            holder.tv_language.setText(result.get(position));
            Picasso.with(context).load(imageId.get(position)).into(holder.im_folder);

            holder.im_folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //GET ALL SHAREDUSERID
                    mSharedID = new Firebase("https://androidpicnroll.firebaseio.com/SharedUsers/" + userId + "/" + strArr.get(position) + "");

                    mSharedID.addValueEventListener(new com.firebase.client.ValueEventListener() {
                        @Override
                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                for (com.firebase.client.DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                                    sharedUsersIdArray.add(String.valueOf(childDataSnapshot.getValue()));
                                    mShareToken = new Firebase("https://androidpicnroll.firebaseio.com/Users/" + String.valueOf(childDataSnapshot.getValue()) + "/deviceToken");
                                    mShareToken.addValueEventListener(new com.firebase.client.ValueEventListener() {
                                        @Override
                                        public void onDataChange(com.firebase.client.DataSnapshot snapshot) {
                                            sharedUserToken.add(String.valueOf(snapshot.getValue()));
                                            Log.d("tag","test--"+sharedUserToken.toString());
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });
                                }
                            } else {
                                Log.d("tag", "NODATA");
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(NavActivity.this);
                    builder1.setMessage("Want to share folder?");
                    builder1.setCancelable(false);

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    //Get all shared user's usedId
                                    //Get all imagekeys at your "Files" root node
                                    Intent uploadphoto = new Intent(NavActivity.this, FolderImagesActivity.class);
                                    uploadphoto.putExtra("selectedFolderName", strArr.get(position));
                                    uploadphoto.putExtra("selectedFolderPosition", String.valueOf(position));
                                    // uploadphoto.putExtra("deviceRefreshedToken", deviceRefreshedToken);
                                    uploadphoto.putStringArrayListExtra("sharedUsersIdArray", sharedUsersIdArray);
                                    uploadphoto.putStringArrayListExtra("imageKeys", imageKeys);
                                    uploadphoto.putStringArrayListExtra("imageUrl", imageUrl);
                                    uploadphoto.putStringArrayListExtra("sharedUserToken", sharedUserToken);
                                    uploadphoto.putExtra("notificationTime", menuItemTime);

                                    startActivity(uploadphoto);

                                    dialog.cancel();
                                }
                            });

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ArrayList<String> sortedArr = new ArrayList<String>();
                                    if (imageKeys != null) {
                                        for (int k = 0; k < imageKeys.size(); k++) {
                                            if ((imageKeys.get(k).contains((strArr.get(position))))) {
                                                sortedArr.add(imageKeys.get(k));
                                            }
                                        }
                                    }
                                    if (sortedArr.size() > 0) {

                                        Intent userlist = new Intent(NavActivity.this, UsersListActivity.class);
                                        userlist.putExtra("selectedFolderName", strArr.get(position));
                                        startActivity(userlist);
                                    } else {

                                        Intent uploadphoto = new Intent(NavActivity.this, FolderImagesActivity.class);
                                        uploadphoto.putExtra("selectedFolderName", strArr.get(position));
                                        uploadphoto.putExtra("selectedFolderPosition", String.valueOf(position));
                                        //       uploadphoto.putExtra("deviceRefreshedToken", deviceRefreshedToken);
                                        uploadphoto.putStringArrayListExtra("sharedUsersIdArray", sharedUsersIdArray);
                                        startActivity(uploadphoto);

                                    }

                                    dialog.cancel();
                                }
                            });

                    builder1.setIcon(R.drawable.appicon);
                    builder1.show();
                }
            });


            holder.btn_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference countRef = ref.child("Files").child(userId);
                    countRef.addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {
                                        for (com.google.firebase.database.DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                                            Object obj = objSnapshot.getKey();
                                            imageKeys.add(String.valueOf(obj));
                                            imageUrl.add(String.valueOf(objSnapshot.getValue()));
                                        }
                                        if (imageKeys != null) {
                                            for (int k = 0; k < imageKeys.size(); k++) {
                                                if ((imageKeys.get(k).contains(strArr.get(position)))) {
                                                    selectedFolderImages.add(imageUrl.get(k));
                                                }
                                            }
                                            if (selectedFolderImages.size() > 0) {
                                                Intent galleryImages = new Intent(NavActivity.this, FolderImages.class);
                                                galleryImages.putExtra("folderName", strArr.get(position));
                                                galleryImages.putStringArrayListExtra("selectedFolderImages", selectedFolderImages);
                                                startActivity(galleryImages);
                                            } else {
                                                Toast.makeText(NavActivity.this, "Add Photos", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        imageKeys = null;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //handle databaseError
                                }
                            });
                }
            });
            return view;
        }


    }

}



