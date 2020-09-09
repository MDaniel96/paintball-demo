package demo.app.paintball.util.services.ble.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SensorTagRangingData {
    private int mTS;
    private int[] mRanges;
    private long mCreatedTs;

    public SensorTagRangingData() {
        this.mCreatedTs = System.currentTimeMillis();
    }

    public SensorTagRangingData(int ts, int[] ranges) {
        this.mTS = ts;
        this.mRanges = ranges;

        this.mCreatedTs = System.currentTimeMillis();
    }

    /**
     * Get the timestamp of the beginning of the superframe containing the ranging information
     *
     * @return The timestamp measured by a 16 bit wide counter running at 32768/33 Hz
     */
    public int getTS() {
        return mTS;
    }

    /**
     * Get anchor distances from the tag
     *
     * @return The ranges in mm. The index i is the i-th anchor distance from the tag
     */
    public int[] getRanges() {
        return mRanges;
    }

    public long getCreatedTs() {
        return mCreatedTs;
    }

    public static SensorTagRangingData parse(byte[] bytes, int anchorCount) {
        SensorTagRangingData data = new SensorTagRangingData();

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        data.mTS = bb.getShort() & 0xFFFF;
        data.mRanges = new int[anchorCount * (anchorCount - 1) / 2 + 1];
        for (int i = 0; i < data.mRanges.length; i++) {
            data.mRanges[i] = bb.getShort();
        }

        return data;
    }
}
