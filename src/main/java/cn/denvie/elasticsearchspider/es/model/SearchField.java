package cn.denvie.elasticsearchspider.es.model;

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
    private QueryType queryType;
    /**
     * bool搜索类型
     */
    private QueryType boolType;
}
