package com.mycom.myapp.common;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
public class PagingResultDto<T> {
    private final List<T> content;
    private final Long totalCount;

    public PagingResultDto(@NonNull List<T> content, @NonNull Long totalCount) {
        this.content = content;
        this.totalCount = totalCount;
    }
}
