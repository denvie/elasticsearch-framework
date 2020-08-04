package cn.denvie.elasticsearchspider.es.model;

import lombok.Data;

/**
 * 查询类型。
 *
 * @author denvie
 * @date 2020/8/4
 */
@Data
public enum QueryType {
    /**
     * 匹配查询
     */
    MATCH(1, "match"),
    /**
     * 精确查询
     */
    TERM(2, "term"),
    /**
     * 范围查询
     */
    RANGE(3, "range");

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
