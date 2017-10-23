package com.clarkgarrent.photoalbum.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.clarkgarrent.photoalbum.Models.LoaderResult;
import com.clarkgarrent.photoalbum.Models.PictureInfo;
import com.clarkgarrent.photoalbum.MyRecyclerViewAdapter;
import com.clarkgarrent.photoalbum.PictureInfoLoader;
import com.clarkgarrent.photoalbum.R;


/**
 * This activity displays a set of images along with some text information.  When the
 * device is in portrait mode the images are displayed in a list layout. When the
 * device is in landscape mode the images are displayed in a grid layout.
 * RecyclerViews are used for these layouts.
 * The image meta data and the images are downloaded from a website. The meta data is in
 * json format.  An AsyncTaskLoader is used to download the json and deserialize it into
 * model objects.  The AsyncTaskLoader uses OkHttp and GSON to do this.  The actual
 * images are downloaded in the RecyclerView adapter using Picasso.
 */

public class PhotoDisplayActivity extends Activity implements LoaderManager.LoaderCallbacks<LoaderResult>{

    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mMyRecyclerViewAdapter;
    private CardView mCvDetails;
    private Button mBtnDetails;
    private TextView mTvTitle;
    private TextView mTvDiscription;
    private static final int CONNECTION_ACTIVITY_TAG = 1;
    private static final String TAG = "## My Info ##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        prepareViews();

        if (savedInstanceState == null) {
            // If the program is just starting up, we want to check for an internet connection.
            // We start a separate activity to do this.  If the started activity returns
            // RESULT_OK then we have an internet connection.  The loader will be started
            // in onActivityResult();
            Intent intent = new Intent(this, ConnectionActivity.class);
            startActivityForResult(intent, CONNECTION_ACTIVITY_TAG);
        } else{
            // Don't check for internet connection on each configuration change.
            // Just start the loader.
            Log.i(TAG,"initLoader called from onCreate");
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK){
            // We have internet connection, so start the loader.
            getLoaderManager().initLoader(0, null, PhotoDisplayActivity.this);
        } else {
            finish();
        }
    }

    @Override
    public Loader<LoaderResult> onCreateLoader(int id, Bundle args){
        Log.i(TAG,"onCreateLoader");
        return new PictureInfoLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult result){
        Log.i(TAG,"onLoadFinished");
        // Loader has returned data.

        // FIrst check for an error in downloading the data.
        if ( ! result.isSuccessful()){
            processError(result.getStatusCode(), result.getMessage());
            return;
        }

        // Create a RecyclerView adapter and give it the list of PictureInfo stored
        // in result.
        mMyRecyclerViewAdapter = new MyRecyclerViewAdapter(result.getPictureInfos());

        // Pass an OnItemClickListener to the adapter.  When the user clicks an image, display
        // the title and description of clicked image in a CardView that sits on top of the
        // RecyclerView.  If the CardView is not visible make it visible.  If the CardView is
        // already visible, fade it in and out as a visual indication that it is displaying
        // new data.
        mMyRecyclerViewAdapter.setOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final PictureInfo pictureInfo) {

                if (mCvDetails.getVisibility() == View.GONE) {
                    mCvDetails.setVisibility(View.VISIBLE);
                    mTvTitle.setText(pictureInfo.getTitle());
                    mTvDiscription.setText(pictureInfo.getDescription());
                }else {
                    AlphaAnimation textAnimation = new AlphaAnimation(1.0f, 0.7f);
                    textAnimation.setDuration(200);
                    textAnimation.setRepeatCount(1);
                    textAnimation.setRepeatMode(Animation.REVERSE);

                    AlphaAnimation cardviewAnimatiion = new AlphaAnimation(1.0f, 0.7f);
                    cardviewAnimatiion.setDuration(200);
                    cardviewAnimatiion.setRepeatCount(1);
                    cardviewAnimatiion.setRepeatMode(Animation.REVERSE);
                    cardviewAnimatiion.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {}

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            mTvTitle.setText(pictureInfo.getTitle());
                            mTvDiscription.setText(pictureInfo.getDescription());
                        }
                    });
                    mCvDetails.startAnimation(cardviewAnimatiion);
                    mTvDiscription.startAnimation(textAnimation);
                    mTvTitle.startAnimation(textAnimation);

                }

            }
        });

        // Set the adapter on the RecyclerView.
        mRecyclerView.setAdapter(mMyRecyclerViewAdapter);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult> loader) {
        Log.i(TAG,"onLoaderReset loader= " + loader);
        //  Remove references to loader data.  The RecycleView adapter
        // has a reference to the loader data.  So remove references
        // to the adapter.
        mRecyclerView.setAdapter(null);
        mMyRecyclerViewAdapter = null;
    }

    private void processError(int code, String errorMsg){

        // Construct a message based on the error message and then
        // display it in am Alert Dialog.  Close the app after user
        // responds to the dialog.

        String displayMsg;

        switch (code) {
            case PictureInfoLoader.IO_ERROR_CODE:
                displayMsg = errorMsg;
                break;
            case PictureInfoLoader.GSON_ERROR_CODE:
                displayMsg = "Error parsing json, " + errorMsg;
                break;
            default:
                displayMsg = "Error code " + code + ", " + errorMsg;
        }

        displayMsg = displayMsg + "\n\nApp will close.";

         new AlertDialog.Builder(this)
                .setMessage(displayMsg)
                .setCancelable(false)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void prepareViews(){
        mTvTitle = (TextView)findViewById(R.id.tvTitle);
        mTvDiscription = (TextView)findViewById(R.id.tvDescription);
        mCvDetails = (CardView)findViewById(R.id.cvDetails);

        // The following button is a little X on the CardView used
        // to display image details.  The CardView sits on top of
        // the RecyclerView and should disappear when the button is
        // pressed.
        mBtnDetails = (Button)findViewById(R.id.btnDetails);
        mBtnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCvDetails.setVisibility(View.GONE);
            }
        });

        // Set the LayoutManager on the RecyclerView depending on the device orientation.
        mRecyclerView = (RecyclerView)findViewById(R.id.rvCards);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i(TAG,"Portrait");
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            Log.i(TAG,"Landscape");
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.span_size)));
        }
    }
}
