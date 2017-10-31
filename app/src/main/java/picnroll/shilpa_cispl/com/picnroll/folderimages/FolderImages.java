package picnroll.shilpa_cispl.com.picnroll.folderimages;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.customgallery.FolderImagesActivity;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.NavActivity;
import picnroll.shilpa_cispl.com.picnroll.userlistview.UsersListActivity;

public class FolderImages extends AppCompatActivity {


    List<FolderDataAdapter> ListOfdataAdapter;
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerViewadapter;
    ArrayList<String> ImageTitleNameArrayListForClick;
    ArrayList<String> profileImageUrl = new ArrayList<>();
    ArrayList<String> imageKeys = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    ArrayList<String> sortedArr = new ArrayList<>();
    String selectedFolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_images);

        Firebase.setAndroidContext(this);
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentFirebaseUser.getUid();
        ImageTitleNameArrayListForClick = new ArrayList<>();
        ListOfdataAdapter = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview1);
        recyclerView.setHasFixedSize(true);
        //   layoutManagerOfrecyclerView = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        selectedFolderName = getIntent().getStringExtra("folderName");
        sortedArr = getIntent().getStringArrayListExtra("selectedFolderImages");
        Log.d("tag", "sharedID--" + selectedFolderName + "\n" +sortedArr);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
//Read profile imageurl and username
        ParseJSonResponse();

//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference countRef = ref.child("Files").child(userId);
//        countRef.addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
//
//                        if (dataSnapshot.exists()) {
//                            for (com.google.firebase.database.DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
//                                Object obj = objSnapshot.getKey();
//                                imageKeys.add(String.valueOf(obj));
//                                imageUrl.add(String.valueOf(objSnapshot.getValue()));
//                            }
//                            if (imageKeys != null) {
//                                for (int k = 0; k < imageKeys.size(); k++) {
//                                    if ((imageKeys.get(k).contains(selectedFolderName))) {
//                                        sortedArr.add(imageKeys.get(k));
//                                    }
//                                }
//                                ParseJSonResponse();
//                            }
//                        } else {
//                            imageKeys = null;
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        //handle databaseError
//                    }
//                });
    }

    //Display images in gridview
    public void ParseJSonResponse() {
        for (int i = 0; i < sortedArr.size(); i++) {
            FolderDataAdapter GetDataAdapter2 = new FolderDataAdapter();
            GetDataAdapter2.setImageUrl(sortedArr.get(i));
            ListOfdataAdapter.add(GetDataAdapter2);
        }
        recyclerViewadapter = new RecyclerViewAdapter(ListOfdataAdapter, this);
        recyclerView.setAdapter(recyclerViewadapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_folder_images, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // finish();
                Intent gotonav = new Intent(FolderImages.this, NavActivity.class);
                startActivity(gotonav);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        Context context;
        List<FolderDataAdapter> dataAdapters;
        ImageLoader imageLoader;

        public RecyclerViewAdapter(List<FolderDataAdapter> getDataAdapter, Context context) {
            super();
            this.dataAdapters = getDataAdapter;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_cardview, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }


        @Override
        public void onBindViewHolder(ViewHolder Viewholder, int position) {

            FolderDataAdapter dataAdapterOBJ = dataAdapters.get(position);

            imageLoader = FolderImageAdapter.getInstance(context).getImageLoader();

            imageLoader.get(dataAdapterOBJ.getImageUrl(),
                    ImageLoader.getImageListener(
                            Viewholder.VollyImageView,//Server Image
                            R.mipmap.ic_launcher,//Before loading server image the default showing image.
                            android.R.drawable.ic_dialog_alert //Error image if requested image dose not found on server.
                    )
            );


            Viewholder.VollyImageView.setImageUrl(dataAdapterOBJ.getImageUrl(), imageLoader);

        }

        @Override
        public int getItemCount() {

            return dataAdapters.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public NetworkImageView VollyImageView;

            public ViewHolder(final View itemView) {

                super(itemView);

                VollyImageView = (NetworkImageView) itemView.findViewById(R.id.VolleyImageView);

            }

        }
    }
}
