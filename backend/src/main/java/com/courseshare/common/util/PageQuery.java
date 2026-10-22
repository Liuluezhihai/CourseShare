package com.courseshare.common.util;

import lombok.Data;

@Data
public class PageQuery {

    private long page = 1;
    private long size = 10;

    public long normalizedPage() {
        return page < 1 ? 1 : page;
    }

    public long normalizedSize() {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}

