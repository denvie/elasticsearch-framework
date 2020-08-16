/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearchspider.es.utils;

import cn.denvie.elasticsearchspider.es.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索参数构造器。
 *
 * @author denvie
 * @since 2020/8/8
 */
public class SearchParamBuilder {
    private List<SearchField> searchFieldList = new ArrayList<>();
    private OrderField orderField;
    private String highlightPreTags;
    private String highlightPostTags;
    private int pageNo = 1;
    private int pageSize = 10;

    public SearchParamBuilder searchField(SearchField searchField) {
        this.searchFieldList.add(searchField);
        return this;
    }

    public SearchParamBuilder searchField(String name, Object value,
                                          QueryType queryType, QueryType boolType) {
        this.searchFieldList.add(new SearchField(name, value, queryType, boolType));
        return this;
    }

    public SearchParamBuilder orderField(OrderField orderField) {
        this.orderField = orderField;
        return this;
    }

    public SearchParamBuilder highlightPreTags(String highlightPreTags) {
        this.highlightPreTags = highlightPreTags;
        return this;
    }

    public SearchParamBuilder highlightPostTags(String highlightPostTags) {
        this.highlightPostTags = highlightPostTags;
        return this;
    }

    public SearchParamBuilder pageNo(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public SearchParamBuilder pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public SingleSearchParam buildSingleSearchParam() {
        if (searchFieldList.isEmpty()) {
            return null;
        }
        SingleSearchParam param = new SingleSearchParam();
        param.setSearchField(searchFieldList.get(0));
        param.setOrderField(orderField);
        param.setHighlightPreTags(highlightPreTags);
        param.setHighlightPostTags(highlightPostTags);
        param.setPageNo(pageNo);
        param.setPageSize(pageSize);
        return param;
    }

    public MultiSearchParam buildMultiSearchParam() {
        MultiSearchParam param = new MultiSearchParam();
        param.setSearchFieldList(searchFieldList);
        param.setOrderField(orderField);
        param.setHighlightPreTags(highlightPreTags);
        param.setHighlightPostTags(highlightPostTags);
        param.setPageNo(pageNo);
        param.setPageSize(pageSize);
        return param;
    }
}
