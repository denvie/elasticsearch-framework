package cn.denvie.elasticsearchspider.es.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索参数。
 *
 * @author denvie
 * @date 2020/8/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractSearchParam {
    protected OrderField orderField;
    protected SearchField highlightField;
    protected String highlightPreTags;
    protected String highlightPostTags;
    protected int pageNo = 1;
    protected int pageSize = 10;
}
