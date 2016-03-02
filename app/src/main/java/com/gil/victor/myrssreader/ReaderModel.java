package com.gil.victor.myrssreader;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

public class ReaderModel implements Parcelable {
    private String mTitle, mDate, mStringContent, mLink, mList;
    private Spanned mDescription;

    public ReaderModel(String list, String link) {
        mList = list;
        mLink = link;
    }

    public ReaderModel(String title, String date, Spanned description, String stringContent) {
        mTitle = title;
        mDate = date;
        mDescription = description;
        mStringContent = stringContent;
    }


    protected ReaderModel(Parcel in) {
        mTitle = in.readString();
        mLink = in.readString();
        mDate = in.readString();
        mStringContent = in.readString();
    }

    public ReaderModel() {
    }

    public String getList() {
        return mList;
    }

    public static final Creator<ReaderModel> CREATOR = new Creator<ReaderModel>() {
        @Override
        public ReaderModel createFromParcel(Parcel in) {
            return new ReaderModel(in);
        }

        @Override
        public ReaderModel[] newArray(int size) {
            return new ReaderModel[size];
        }
    };

    public String getStringContent() {
        return mStringContent;
    }

    public void setStringContent(String stringContent) {
        this.mStringContent = stringContent;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getLink() {
        return mLink;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Spanned getDescription() {
        return mDescription;
    }

    public void setDescription(Spanned mDescription) {
        this.mDescription = mDescription;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mLink);
        dest.writeString(mDate);
        dest.writeString(mStringContent);
    }
}