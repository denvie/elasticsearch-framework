/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单字段搜索参数。
 *
 * @author denvie
 * @since 2020/8/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleSearchParam extends AbstractSearchParam {
    private SearchField searchField;
}
