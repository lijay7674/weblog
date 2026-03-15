package com.forum.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private Long total;
    private Long pages;
    private Long current;
    private Long size;
    private List<T> records;

    public static <T> PageResult<T> of(Long total, Long pages, Long current, Long size, List<T> records) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setPages(pages);
        pageResult.setCurrent(current);
        pageResult.setSize(size);
        pageResult.setRecords(records);
        return pageResult;
    }
}
