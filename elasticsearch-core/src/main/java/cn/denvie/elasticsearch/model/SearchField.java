/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchField {
    /**
     * 搜索字段名称，多个字段用','分隔
     */
    private String name;
    /**
     * 搜索值
     */
    private Object value;
    /**
     * 搜索类型
     */
    private SearchType searchType;
    /**
     * 外层搜索类型，比如：bool搜索类型
     */
    private SearchType outerSearchType;
    /**
     * 是否为constant_score搜索，不进行评分，可提高查询效率
     */
    private boolean isConstantScore;
}
