/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.search.SearchResponse;

import java.util.List;

/**
 * 搜索结果。
 *
 * @author denvie
 * @since 2020/8/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult<T> {
    private long total;
    private List<T> dataList;
    private SearchResponse originalResponse;
    private int pageNo;
    private int pageSize;
}
