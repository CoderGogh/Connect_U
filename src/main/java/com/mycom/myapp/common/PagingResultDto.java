package com.mycom.myapp.common;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PagingResultDto<T> {
    private List<T> content;
    private Integer totalCount;

    public PagingResultDto(@NonNull List<T> content, @NonNull Integer totalCount) {
        this.content = content;
        this.totalCount = totalCount;
    }
}
