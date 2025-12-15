package com.mycom.myapp.storage;

public class UploadResult {
    private final long size;
    private final String key;

    // key와 size를 둘 다 초기화
    public UploadResult(String key, long size) {
        this.key = key;
        this.size = size;
    }

    // key만 받을 수 있는 생성자 추가
    public UploadResult(String key) {
        this.key = key;
        this.size = 0; // 기본값
    }

    public String getKey() {
        return key;
    }

    public long getSize() {
        return size;
    }
}
