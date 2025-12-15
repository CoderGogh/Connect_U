package com.mycom.myapp.storage;

import com.mycom.myapp.storage.StorageException;

public interface StorageClient {
    UploadResult upload(byte[] data, String imageKey) throws StorageException;
    void delete(String imageKey) throws StorageException;
    boolean exists(String imageKey) throws StorageException;
}
