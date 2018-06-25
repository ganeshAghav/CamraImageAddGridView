package com.everestadvanced.camraimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 06-03-2018.
 */

class GridViewAdapter extends BaseAdapter
{
    private List<String> imgPic;
    Context context;

    GridViewAdapter(List<String> thePic ,Context cont) {

        imgPic = thePic;
        context=cont;
    }

    @Override
    public int getCount() {

        if(imgPic != null)
            return imgPic.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {

        return position;
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater infla;
        infla = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=infla.inflate(R.layout.main1,null);

        Bitmap bm;

        ImageView imageView=(ImageView)view.findViewById(R.id.imageView1);

        try
        {
            File f = new File(imgPic.get(position).toString());

            if(f!=null)
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bm = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                imageView.setImageBitmap(bm);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return imageView;

    }
}