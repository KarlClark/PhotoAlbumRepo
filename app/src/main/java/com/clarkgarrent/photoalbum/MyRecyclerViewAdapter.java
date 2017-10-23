package com.clarkgarrent.photoalbum;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.clarkgarrent.photoalbum.Models.PictureInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

    private List<PictureInfo> mPictureInfos;
    private static final String mUrlFirstPart = "https://s3.amazonaws.com/sc.va.util.weatherbug.com/interviewdata/mobilecodingchallenge/";
    private static final String TAG ="## My Info ##";
    private OnItemClickListener itemClickListener;  // Reference to object listening for item clicks.

    // Interface implemented by object that wants to listen for item clicks.
    public interface OnItemClickListener {
        // Return the picture info for the clicked on image.
        void onItemClick(PictureInfo pictureInfo);
    }

    // Called by an object to register itself as an item click listener.
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public MyRecyclerViewAdapter(List<PictureInfo> pictureInfos){
        mPictureInfos = pictureInfos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivPicture;
        private TextView tvTitle;
        private HorizontalScrollView hsvTextScroll;

        private ViewHolder(View view) {
            super(view);
            ivPicture = (ImageView)view.findViewById(R.id.ivPicture);
            tvTitle = (TextView)view.findViewById(R.id.tvTitle);
            hsvTextScroll = (HorizontalScrollView)view.findViewById(R.id.hsvTextScroll);

            // Set the OnClickListener for this view to return the picture info for
            // this view to our itemClickListener.
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null){
                        itemClickListener.onItemClick(mPictureInfos.get(getLayoutPosition()));
                    }
                }
            });

            // We want the TextView to be scrollable.  When the device is in portrait mode the
            // TextView can hold multiple lines, so we want it to scroll vertically.  In landscape
            // mode there is only one line of text so we want it to scroll horizontally.  TextViews
            // can scroll vertically on their own with the right attributes.  To scroll
            // horizontally we need to use a ScrollView.  We need to make sure the appropriate view
            // receives the touch event by listening to its touch event and setting
            // the requestDisallowInterceptTouchEvent(true) on its parent.
            if(view.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                tvTitle.setMovementMethod(new ScrollingMovementMethod());
                tvTitle.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.i(TAG, "tvTitle onTouch");
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
            } else {
                hsvTextScroll.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Disallow the touch request for parent scroll on touch of child view
                        Log.i(TAG, v.getId() + " " + ((View) v.getParent()).getId() + " " + ((View) v.getParent().getParent()).getId());
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.thumb_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position){
        // This method must download images from the internet.  It uses the Picasso
        // library to do this.  When we download the image we want to resize it to a small
        // resolution that still looks good.  Pick this resolution based on the screen size.
        Context context = viewHolder.tvTitle.getContext();
        int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int resolution =100;
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE){
            resolution = 200;
        }

        // Put data in views.
        viewHolder.tvTitle.setText(mPictureInfos.get(position).getTitle());
        viewHolder.ivPicture.setImageResource(android.R.color.transparent);
        Picasso.with(context)
                .load(mUrlFirstPart + mPictureInfos.get(position).getFilename())
                .resize(resolution,0)
               .into(viewHolder.ivPicture);
    }

    @Override
    public int getItemCount() {
        return mPictureInfos.size();
    }

}
