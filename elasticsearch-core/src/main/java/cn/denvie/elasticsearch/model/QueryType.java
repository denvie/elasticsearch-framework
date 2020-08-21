/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

/**
 * 搜索类型。
 *
 * @author denvie
 * @since 2020/8/4
 */
public enum QueryType {
    /**
     * 匹配搜索
     */
    MATCH(1, "match"),
    /**
     * 多字段匹配搜索
     */
    MULTI_MATCH(2, "multi_match"),
    /**
     * 精确搜索
     */
    TERM(3, "term"),
    /**
     * 范围搜索
     */
    RANGE(4, "range"),
    /**
     * bool搜索, must
     */
    BOOL_MUST(10, "must"),
    /**
     * bool搜索, must_not
     */
    BOOL_MUST_NOT(11, "must_not"),
    /**
     * bool搜索, must_not
     */
    BOOL_MUST_SHOULD(12, "should"),
    /**
     * bool搜索, filter
     */
    BOOL_MUST_FILTER(13, "filter");

    private int code;
    private String queryName;

    QueryType(int code, String queryName) {
        this.code = code;
        this.queryName = queryName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }
}
