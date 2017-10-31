package picnroll.shilpa_cispl.com.picnroll.customgallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.NavActivity;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.Utility;
import picnroll.shilpa_cispl.com.picnroll.userlistview.UsersListActivity;

public class FolderImagesActivity extends AppCompatActivity implements View.OnClickListener {

    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;

    ImageView imgSinglePick;
    FloatingActionButton fab;

    ViewSwitcher viewSwitcher;
    ImageLoader imageLoader;
    String dir, extension, userId, selectedFolderName, selectedFolderIndex, notificationTime;
    File newdir;
    Uri filePath;
    ArrayList<String> sharedFolderUserId = new ArrayList<>();

    FirebaseUser currentFirebaseUser;
    DatabaseReference ref, refCamera;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://androidpicnroll.appspot.com");
    private Firebase mRef;
    ArrayList<String> imageKeys = new ArrayList<>();
    ArrayList<String> imageValues = new ArrayList<>();
    ArrayList<String> folderImageValues = new ArrayList<>();
    ArrayList<String> sharedDeviceToken = new ArrayList<>();
    final JSONArray jsonArray = new JSONArray();


    //push notification
    String refreshedToken, message;
    OkHttpClient mClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_folder_images);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();
        ref = FirebaseDatabase.getInstance().getReference();
        refCamera = FirebaseDatabase.getInstance().getReference();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        notificationTime = getIntent().getStringExtra("notificationTime");
        refreshedToken = getIntent().getStringExtra("deviceRefreshedToken");
        selectedFolderName = getIntent().getStringExtra("selectedFolderName");
        selectedFolderIndex = getIntent().getStringExtra("selectedFolderPosition");
        sharedFolderUserId = getIntent().getStringArrayListExtra("sharedUsersIdArray");
        sharedDeviceToken = getIntent().getStringArrayListExtra("sharedUserToken");


        if (notificationTime == null) {
            notificationTime = String.valueOf(1000);
        }

        //  Log.d("tag","sharedUserToken--"+sharedDeviceToken.toString());

        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        newdir = new File(dir);
        newdir.mkdirs();
        initImageLoader();
        init();


        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolderDB/";
        newdir = new File(dir);
        newdir.mkdirs();

        mRef = new Firebase("https://androidpicnroll.firebaseio.com/Files/" + userId + "");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {


                for (com.firebase.client.DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    imageKeys.add(childDataSnapshot.getKey());
                    imageValues.add(String.valueOf(childDataSnapshot.getValue()));
                    Log.d("tag", "images-->" + childDataSnapshot.getKey() + "\n" + String.valueOf(childDataSnapshot.getValue()));
                }

                for (int a = 0; a < imageKeys.size(); a++) {
                    if (imageKeys.get(a).contains(selectedFolderName)) {
                        folderImageValues.add(imageValues.get(a));
                        Log.d("tag", "folderImages-->" + folderImageValues.toString() + "\n");
                        String file = dir + String.valueOf(UUID.randomUUID()) + ".jpg";
                        File newfile = new File(file);

                        ArrayList<String> localFiles = new ArrayList<String>();
                        localFiles.add((String.valueOf(Uri.fromFile(newfile))));
                        Log.d("dbimage", "dbimage" + String.valueOf(Uri.fromFile(newfile)) + "\n" + localFiles.toString());
                        try {
                            newfile.createNewFile();
                        } catch (IOException e) {
                        }
                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        ArrayList<CustomGallery> dataT = new ArrayList<>();
        for (String string : folderImageValues) {
            CustomGallery item = new CustomGallery();
            item.sdcardPath = string;
            dataT.add(item);
        }
        viewSwitcher.setDisplayedChild(0);
        adapter.addAll(dataT);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_upload_photo, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // finish();
            Intent gotonav = new Intent(FolderImagesActivity.this, NavActivity.class);
            startActivity(gotonav);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {

            boolean result = Utility.checkPermission(FolderImagesActivity.this);
            if (result) {
                Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
                startActivityForResult(i, 200);
                return true;
            }
        } else if (id == android.R.id.home) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void init() {

        handler = new Handler();
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        adapter.setMultiplePick(false);
        gridGallery.setAdapter(adapter);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.setDisplayedChild(1);

        imgSinglePick = (ImageView) findViewById(R.id.imgSinglePick);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int count = 0;

        //Gallery image added
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            final String[] all_path = data.getStringArrayExtra("all_path");

            ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

            for (String string : all_path) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = string;

                // count++;
                dataT.add(item);
                //message = String.valueOf(count++);

                //Read image name and insert in DB
                int lastDot = string.lastIndexOf('/');
                if (lastDot == -1) {
                    // No dots - what do you want to do?
                } else {
                    extension = string.substring(lastDot);

                }
                // item.sdcardPath = string;
                filePath = Uri.fromFile(new File(string));


                if (filePath != null) {

                    // pd.show();

                    StorageReference childRef = storageRef.child("Files").child(userId).child(extension);

                    //uploading the image
                    UploadTask uploadTask = childRef.putFile(filePath);


                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            ref.child("Files").child(userId).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());
                            //Share camera photo with already shared users
                            if (sharedFolderUserId == null) {
                                Log.d("tag", "NoDATA");
                            } else {

                                for (int i = 0; i < sharedFolderUserId.size(); i++) {
                                    ref.child("Files").child(sharedFolderUserId.get(i)).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());
                                    Log.d("tag", "Camera" + ref);
                                }
                            }
                            Toast.makeText(FolderImagesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //  pd.dismiss();
                            Toast.makeText(FolderImagesActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            sendPushNotification("You have " + all_path.length + " new photos");

//            //Send push notification for every new image added to a folder
//            for (int i = 0; i < sharedDeviceToken.size(); i++) {
//                Log.d("tag", "test-" + Long.parseLong(notificationTime) + "\n" + sharedDeviceToken.toString());
//                // sendNotification("You have " + all_path.length + " new photos", sharedDeviceToken.get(i));
//                jsonArray.put(sharedDeviceToken.get(i));
//                final long period = 1000;
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        // do your task here
//                        sendMessage(jsonArray, "PicNRoll", "You have " + all_path.length + " new photos", "Http:\\google.com", "My Name is Vishal");
//                    }
//                }, 0, Long.parseLong(notificationTime));
//            }
            viewSwitcher.setDisplayedChild(0);
            adapter.addAll(dataT);
        }

        //Camera image added
        else if (requestCode == 300 && resultCode == Activity.RESULT_OK) {


            viewSwitcher.setDisplayedChild(1);
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imgSinglePick.setImageBitmap(photo);

            String file = dir + String.valueOf(UUID.randomUUID()) + ".jpg";
            File newfile = new File(file);
            Log.d("CameraDemo", "cameraimage" + String.valueOf(Uri.fromFile(newfile)));
            try {
                newfile.createNewFile();
            } catch (IOException e) {
            }

            int lastDot = String.valueOf(Uri.fromFile(newfile)).lastIndexOf('/');
            if (lastDot == -1) {
                // No dots - what do you want to do?
            } else {
                extension = String.valueOf(Uri.fromFile(newfile)).substring(lastDot);
                Log.d("tag", "extensioncamera" + extension);
            }
            filePath = (Uri.fromFile(newfile));

            if (filePath != null) {
                // pd.show();

                StorageReference childRefCamera = storageRef.child("Files").child(userId).child(extension);
                //uploading the image
                UploadTask uploadTaskCamera = childRefCamera.putFile(filePath);

                uploadTaskCamera.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //  pd.dismiss();

                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        refCamera.child("Files").child(userId).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());

                        //Share camera photo with already shared users
                        if (sharedFolderUserId == null) {
                            Log.d("tag", "NoDATA");
                        } else {

                            for (int i = 0; i < sharedFolderUserId.size(); i++) {

                                refCamera.child("Files").child(sharedFolderUserId.get(i)).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());

                                Log.d("tag", "refCamera" + refCamera);
                            }
                        }
                        Toast.makeText(FolderImagesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  pd.dismiss();
                        Toast.makeText(FolderImagesActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            sendPushNotification("One new photo");
//            for (int i = 0; i < sharedDeviceToken.size(); i++) {
//                jsonArray.put(sharedDeviceToken.get(i));
//
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        // do your task here
//                        sendMessage(jsonArray, "PicNRoll", "One new photo", "Http:\\google.com", "My Name is Vishal");
//                    }
//                }, 0, Long.parseLong(notificationTime));
//
//            }
        }
    }



    public void sendPushNotification(final String body){
        //Send push notification for every new image added to a folder
        for (int i = 0; i < sharedDeviceToken.size(); i++) {
            Log.d("tag", "test-" + Long.parseLong(notificationTime) + "\n" + sharedDeviceToken.toString());
            // sendNotification("You have " + all_path.length + " new photos", sharedDeviceToken.get(i));
            jsonArray.put(sharedDeviceToken.get(i));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // do your task here
                    sendMessage(jsonArray, "PicNRoll",body , "Http:\\google.com", "My Name is Vishal");
                }
            }, 0, Long.parseLong(notificationTime));
        }

    }








    public void sendMessage(final JSONArray recipients, final String title, final String body, final String icon, final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("message", message);
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());
                    Log.d("Main Activity", "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    Toast.makeText(FolderImagesActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(FolderImagesActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {

        String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, bodyString);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + "AAAAAmwdLUI:APA91bFx7wtj-oyZarP0oyLbTQpMmPSM65h39n37dA959f2ReG27EyqGNIJHjKti4s5QJW2e18OTyGdqoipNSMH1LuXx7woqlTfaTA6DfENyUAr45IIquXyH0djK81DHO5mxbF88hqDS")
                .build();
        okhttp3.Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public void onClick(View view) {

        boolean result = Utility.checkPermission(FolderImagesActivity.this);
        if (result) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 300);
        }

    }


////Send push notification for every new image added by gallery or camera to a folder
//    public void sendNotification(final String message, final String s){
//      //  for (int i =0;i<sharedDeviceToken.size(); i++) {
//
//
//            //Send a push notification to user with total count of new images added
//            RequestQueue queue = Volley.newRequestQueue(FolderImagesActivity.this);
//
//            StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.0.116/serverfirebase.php", new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//
//                //    Toast.makeText(FolderImagesActivity.this, "Response" + response, Toast.LENGTH_SHORT).show();
//                    Log.d("My success", "" + response);
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                   // Toast.makeText(FolderImagesActivity.this, "my error :" + error, Toast.LENGTH_LONG).show();
//                    Log.i("My error", "" + error);
//                }
//            }) {
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> map = new HashMap<String, String>();
//                    map.put("message", message);
//                    map.put("refreshedToken", s);
//                    Log.d("tag","tokenI--"+s);
//
//                    return map;
//                }
//            };
//            queue.add(request);
//     //   }
//
//
//    }

    @Override
    public void onBackPressed() {

    }
}
