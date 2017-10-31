package picnroll.shilpa_cispl.com.picnroll.galleries;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.NavActivity;

/**
 * Created by shilpa-cispl on 21/09/17.
 */

public class GalleryListAdapter extends BaseAdapter {
    ArrayList<String> result = new ArrayList<>();
    Context context;
    int [] imageId;
    private static LayoutInflater inflater=null;

    public GalleryListAdapter(NavActivity mainActivity, ArrayList prgmNameList, int[] prgmImages) {
// TODO Auto-generated constructor stub
        result=prgmNameList;
        context=mainActivity;
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
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

    public class Holder
    {
        TextView tv_language;
        ImageView im_language;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
        Holder holder=new Holder();
        View view;
        view = inflater.inflate(R.layout.layout_gallery_list, null);

        holder.tv_language=(TextView) view.findViewById(R.id.tv_language);
        holder.im_language=(ImageView) view.findViewById(R.id.im_language);

        holder.tv_language.setText(result.get(position));
        Picasso.with(context).load(imageId[position]).into(holder.im_language);

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//// TODO Auto-generated method stub
//                Toast.makeText(context, "You Clicked " + result.get(position), Toast.LENGTH_LONG).show();
//            }
//        });



        return view;
    }

}
