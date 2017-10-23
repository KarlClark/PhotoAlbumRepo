package com.clarkgarrent.photoalbum.Models;

import java.util.ArrayList;

/**
 * Created by karlc on 10/15/2017.
 */

public class LoaderResult {

    // Class used to return results from PictureInfoLoader.

    private ArrayList<PictureInfo> pictureInfos;
    private boolean successful;
    private int statusCode;
    private String message;

    public LoaderResult(boolean successful, int statusCode, String message, ArrayList<PictureInfo> pictureInfos) {
        this.successful = successful;
        this.statusCode = statusCode;
        this.message = message;
        this.pictureInfos = pictureInfos;
    }

    public ArrayList<PictureInfo> getPictureInfos() {
        return pictureInfos;
    }

    public void setPictureInfos(ArrayList<PictureInfo> pictureInfos) {
        this.pictureInfos = pictureInfos;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
