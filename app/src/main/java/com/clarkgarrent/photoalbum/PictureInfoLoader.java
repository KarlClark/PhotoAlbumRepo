package com.clarkgarrent.photoalbum;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.clarkgarrent.photoalbum.Models.LoaderResult;
import com.clarkgarrent.photoalbum.Models.PictureInfo;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This AsyncTaskLoader downloads json data from the web and deserializes it.  The methods
 * in this class pretty much follow the Android documentation.
 */

public class PictureInfoLoader extends AsyncTaskLoader<LoaderResult> {

    private LoaderResult mLoaderResult = null;
    private long mTimeOfLastLoad = 0L;
    public static final int IO_ERROR_CODE = -1;
    public static final int GSON_ERROR_CODE = -2;
    public static final String TAG = "## My Info ##";

    public PictureInfoLoader(Context context){
        super(context);
    }

    @Override
    protected void onStartLoading(){

        // If we don't have any cached data or the data is too old then download from
        // internet, otherwise deliver the cached data.

        if (mLoaderResult == null || (SystemClock.uptimeMillis() - mTimeOfLastLoad)/(1000 *60) > 60 ){
            forceLoad();
        }else{
            deliverResult(mLoaderResult);
        }
    }

    @Override
    public void deliverResult(LoaderResult loaderResult){

        // Cache the data then deliver the result.

        mLoaderResult = loaderResult;
        mTimeOfLastLoad = SystemClock.uptimeMillis();

        if (isStarted()){
            super.deliverResult(loaderResult);
        }
    }

    @Override
    public LoaderResult loadInBackground(){

        // Use OkHttp to download the json data, and gson to deserialize it.  If
        // at any step we have and error return a LoaderResult instance with the
        // error information.  Otherwise return a LoaderResult containing  an
        // ArrayList of PictureInfo.

        OkHttpClient client = new OkHttpClient();
        Response response;
        String json;

        // Build OkHttp request
        final Request request = new Request.Builder()
                .url("https://s3.amazonaws.com/sc.va.util.weatherbug.com/interviewdata/mobilecodingchallenge/sampledata.json")
                .build();
        try {
            // Download json data
            response = client.newCall(request).execute();
            if ( ! response.isSuccessful()){
                return new LoaderResult(response.isSuccessful(), response.code(), response.message(), null);
            }
            json = response.body().string(); // this can also get an IOException
        } catch (IOException e) {
            return new LoaderResult(false, IO_ERROR_CODE, e.getMessage(), null);
        }

        // Deserialize the json using Gson.
        ArrayList<PictureInfo> items;
        Gson gson = new Gson();
        Type arrayListType = new TypeToken<ArrayList<PictureInfo>>(){}.getType();
        try {
            items = gson.fromJson(json, arrayListType);
        } catch(JsonParseException e){
            return new LoaderResult(false, GSON_ERROR_CODE, e.getMessage(), null);
        }

        return new LoaderResult(response.isSuccessful(), response.code(), response.message(), items);
    }
}
