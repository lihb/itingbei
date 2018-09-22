package com.lihb.babyvoice;

public class DataManager {

    private DataManager() {
    }

    private static class Holder {
        private static final DataManager INSTANCE = new DataManager();
    }

    public static DataManager getInstance() {
        return Holder.INSTANCE;
    }

    private boolean isTransferDataStarted = false;

    public boolean isTransferDataStarted() {
        return isTransferDataStarted;
    }

    public void setTransferDataStarted(boolean transferDataStarted) {
        isTransferDataStarted = transferDataStarted;
    }
}
