package com.example.sgannouncementsystem;

public class Upload {
    private String mName;
    private String mImageUrl;

    public Upload() {
    }

    public Upload(String mName, String mImageUrl) {
        if (mName.trim().equals("")){
            mName = "No Name";
        }
        this.mName = mName;
        this.mImageUrl = mImageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setmame(String mName) {
        this.mName = mName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }
}
