package com.mycom.myapp.storage;

public interface StorageClient {
    UploadResult upload(byte[] data, String imageKey) throws StorageException;
    void delete(String imageKey) throws StorageException;
    boolean exists(String imageKey) throws StorageException;

//    String getPublicUrl(String key);  // public url사용 하지 않음


    /**
     * GCS 객체에 대한 Signed URL을 생성하여 반환한다.
     * 구현체에서 적절한 만료 시간(TTL)을 고정값으로 설정하여 사용한다.
     */
    String getSignedUrl(String key);
}
