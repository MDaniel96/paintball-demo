package demo.app.paintball.util.services.ble;

import androidx.annotation.NonNull;

public class UWBNetworkInfo {
    private int mTagID;
    private int mGroupID;
    private int mAnchorCount;
    private int mTagCount;

    public int getTagID() {
        return mTagID;
    }

    public void setTagID(int mTagID) {
        this.mTagID = mTagID;
    }

    public int getGroupID() {
        return mGroupID;
    }

    public void setGroupID(int mGroupID) {
        this.mGroupID = mGroupID;
    }

    public int getAnchorCount() {
        return mAnchorCount;
    }

    public void setAnchorCount(int mAnchorCount) {
        this.mAnchorCount = mAnchorCount;
    }

    public int getTagCount() {
        return mTagCount;
    }

    public void setTagCount(int mTagCount) {
        this.mTagCount = mTagCount;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("network 0x%04X (%d tag, %d anchor), tag_id 0x%04X", mGroupID, mTagCount, mAnchorCount, mTagID);
    }
}
