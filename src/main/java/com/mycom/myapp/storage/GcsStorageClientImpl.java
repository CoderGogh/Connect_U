package com.mycom.myapp.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.concurrent.TimeUnit;

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
    @Override
    public String getPublicUrl(String key) {
        return "https://storage.googleapis.com/" + bucketName + "/" + key;
    }

    /**
     * 게시글/프로필 조회 시마다 새로 생성해서 내려줄 Signed URL.
     * 여기서는 TTL을 24시간으로 고정한다 (Option A).
     */
    @Override
    public String getSignedUrl(String key) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, key)).build();
            URL url = storage.signUrl(
                    blobInfo,
                    24, TimeUnit.HOURS,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature()
            );
            return url.toString();
        } catch (Exception e) {
            log.error("GCS signUrl failed. bucket={}, key={}", bucketName, key, e);
            // 문제가 생기면 기존 public URL 방식으로 degrade
            return getPublicUrl(key);
        }
    }
}
