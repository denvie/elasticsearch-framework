/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索参数。
 *
 * @author denvie
 * @since 2020/8/4
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
