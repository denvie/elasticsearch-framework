/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果。
 *
 * @author denvie
 * @since 2020/8/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingResult<T> {
    private long total;
    private List<T> dataList;
    private int pageNo;
    private int pageSize;
}
