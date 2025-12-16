package com.mycom.myapp.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class StorageClientImpl implements StorageClient {

    @Override
    public UploadResult upload(byte[] data, String imageKey) throws StorageException {
        // 실제 업로드 로직 또는 테스트용 더미 구현
        // 업로드된 데이터 크기를 size로 전달
        long size = data != null ? data.length : 0;
        return new UploadResult(imageKey, size);
    }

    @Override
    public void delete(String imageKey) throws StorageException {
        // 삭제 로직
    }

    @Override
    public boolean exists(String imageKey) throws StorageException {
        // 존재 여부 체크
        return false;
    }

    @Override
    public String getPublicUrl(String key) {
        return "http://localhost/mock/" + key;
    }

}
