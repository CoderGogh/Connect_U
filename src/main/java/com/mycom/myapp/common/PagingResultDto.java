package com.mycom.myapp.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PagingResultDto<T> {
    private List<T> content;
    private Integer totalCount;
}
