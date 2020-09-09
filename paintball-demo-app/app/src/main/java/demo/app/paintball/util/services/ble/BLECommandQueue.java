package demo.app.paintball.util.services.ble;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class BLECommandQueue {
    private static final String TAG = BLECommandQueue.class.getSimpleName();

    public abstract static class BLECommand {
        public abstract boolean runCommand();

        public void commandCallback(int status, byte[] value) {
        }

        ;
    }

    private Queue<BLECommand> mCommandQueue = new LinkedList<>();
    private BLECommand mRunningCommand = null;

    public void add(BLECommand cmd) {
        if (mRunningCommand == null) {
            if (!cmd.runCommand()) {
                mRunningCommand = null;
                Log.e(TAG, "Command cannot be run!");
            } else {
                mRunningCommand = cmd;
            }
        } else {
            mCommandQueue.add(cmd);
        }
    }

    public void onCommandReady(int status, byte[] value) {
        if (mRunningCommand != null) {
            mRunningCommand.commandCallback(status, value);
            if (!mCommandQueue.isEmpty()) {
                mRunningCommand = mCommandQueue.poll();
                if (!mRunningCommand.runCommand()) {
                    mRunningCommand = null;
                    Log.e(TAG, "Command cannot be run!");
                }
            } else {
                mRunningCommand = null;
            }
        }
    }
}
