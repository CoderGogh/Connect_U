package com.mycom.myapp.storage;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GcsStorageClient implements StorageClient {

    private final Storage storage;
    private final String bucketName;

    @Override
    public UploadResult upload(byte[] data, String imageKey) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, imageKey)
                .setContentType("image/jpeg")
                .build();

        storage.create(blobInfo, data);
        return new UploadResult(imageKey, data.length);
    }

    @Override
    public void delete(String imageKey) {
        storage.delete(bucketName, imageKey);
    }

    @Override
    public boolean exists(String imageKey) {
        return storage.get(bucketName, imageKey) != null;
    }

    // public url 사용 x --> Signed URL 사용
//    @Override
//    public String getPublicUrl(String key) {
//        return "https://storage.googleapis.com/" + bucketName + "/" + key;
//    }

    @Override
    public String getSignedUrl(String key) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, key).build();
        URL url = storage.signUrl(blobInfo, 7, TimeUnit.DAYS);
        return url.toString();
    }
}
