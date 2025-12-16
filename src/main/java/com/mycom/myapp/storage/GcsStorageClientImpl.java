package com.mycom.myapp.storage;

import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("prod")
public class GcsStorageClientImpl implements StorageClient {

    private final Storage storage;
    private final String bucketName;

    public GcsStorageClientImpl(
            @Value("${gcp.storage.bucket}") String bucketName
    ) {
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
    }

    @Override
    public UploadResult upload(byte[] data, String imageKey) throws StorageException {
        try {
            BlobId blobId = BlobId.of(bucketName, imageKey);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/*")
                    .build();

            Blob blob = storage.create(blobInfo, data);

            log.info("GCS upload success. bucket={}, key={}, size={}",
                    bucketName, imageKey, blob.getSize());

            return new UploadResult(imageKey, blob.getSize());
        } catch (Exception e) {
            log.error("GCS upload failed. bucket={}, key={}", bucketName, imageKey, e);
            throw new StorageException("GCS upload failed", e);
        }
    }

    @Override
    public void delete(String imageKey) throws StorageException {
        try {
            BlobId blobId = BlobId.of(bucketName, imageKey);
            boolean deleted = storage.delete(blobId);

            if (!deleted) {
                log.warn("GCS delete failed or object not found. key={}", imageKey);
            }
        } catch (Exception e) {
            throw new StorageException("GCS delete failed", e);
        }
    }

    @Override
    public boolean exists(String imageKey) throws StorageException {
        try {
            Blob blob = storage.get(BlobId.of(bucketName, imageKey));
            return blob != null && blob.exists();
        } catch (Exception e) {
            throw new StorageException("GCS exists check failed", e);
        }
    }
}
