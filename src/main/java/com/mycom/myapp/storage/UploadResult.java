package com.mycom.myapp.storage;

public class UploadResult {
    private final long size;

    public UploadResult(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }
}
