/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

import lombok.Data;
import org.elasticsearch.search.sort.SortOrder;

/**
 * 排序字段。
 *
 * @author denvie
 * @since 2020/8/4
 */
@Data
public class OrderField {
    /**
     * 排序字段名称
     */
    private String name;
    /**
     * 是否降序
     */
    private boolean isDesc;

    public OrderField() {
    }

    public OrderField(String name, boolean isDesc) {
        this.name = name;
        this.isDesc = isDesc;
    }

    /**
     * 获取排序方式。
     *
     * @return 返回 DESC 或者 ASC
     */
    public SortOrder getSortOrder() {
        return isDesc ? SortOrder.DESC : SortOrder.ASC;
    }
}
