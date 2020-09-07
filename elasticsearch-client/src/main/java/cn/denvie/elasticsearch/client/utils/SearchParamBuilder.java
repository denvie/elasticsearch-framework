/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.client.utils;

import cn.denvie.elasticsearch.client.model.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;

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
    private HighlightField highlightField;
    private String highlightPreTags;
    private String highlightPostTags;
    private int pageNo = 1;
    private int pageSize = 10;
    private List<AggregationBuilder> aggregationBuilders;
    private boolean trackTotalHits;
    private boolean withOriginalResponse;

    public SearchParamBuilder searchField(SearchField searchField) {
        this.searchFieldList.add(searchField);
        return this;
    }

    public SearchParamBuilder searchField(String name, Object value, SearchType searchType,
                                          SearchType boolType, boolean isConstantScore) {
        this.searchFieldList.add(new SearchField(name, value, searchType, boolType, isConstantScore));
        return this;
    }

    /**
     * 设置排序字段。
     *
     * @param orderField 排序字段
     * @return SearchParamBuilder
     */
    public SearchParamBuilder orderField(OrderField orderField) {
        this.orderField = orderField;
        return this;
    }

    /**
     * 设置高亮字段。
     *
     * @param highlightField    高亮字段
     * @return SearchParamBuilder
     */
    public SearchParamBuilder highlightField(HighlightField highlightField) {
        this.highlightField = highlightField;
        return this;
    }

    /**
     * 设置页数。
     *
     * @param pageNo 页数
     * @return SearchParamBuilder
     */
    public SearchParamBuilder pageNo(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    /**
     * 设置每页大小。
     *
     * @param pageSize 每页大小
     * @return SearchParamBuilder
     */
    public SearchParamBuilder pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 添加聚合搜索项。
     *
     * @param aggregation 聚合搜索项
     * @return SearchParamBuilder
     */
    public SearchParamBuilder aggregation(AggregationBuilder aggregation) {
        if (this.aggregationBuilders == null) {
            this.aggregationBuilders = new ArrayList<>();
        }
        this.aggregationBuilders.add(aggregation);
        return this;
    }

    /**
     * 设置是否获取实际文档总数。
     *
     * @param trackTotalHits true或者false
     * @return SearchParamBuilder
     */
    public SearchParamBuilder trackTotalHits(boolean trackTotalHits) {
        this.trackTotalHits = trackTotalHits;
        return this;
    }

    /**
     * 设置搜索结果是否包含原生的SearchResponse引用。
     *
     * @param withOriginalResponse true或者false
     * @return SearchParamBuilder
     */
    public SearchParamBuilder withOriginalResponse(boolean withOriginalResponse) {
        this.withOriginalResponse = withOriginalResponse;
        return this;
    }

    /**
     * 构建单字段搜索参数。
     *
     * @return SingleSearchParam
     */
    public SingleSearchParam buildSingleSearchParam() {
        if (searchFieldList.isEmpty()) {
            return null;
        }
        SingleSearchParam param = new SingleSearchParam();
        param.setSearchField(searchFieldList.get(0));
        initSearchParam(param);
        return param;
    }

    /**
     * 构建多字段搜索参数。
     *
     * @return MultiSearchParam
     */
    public MultiSearchParam buildMultiSearchParam() {
        MultiSearchParam param = new MultiSearchParam();
        param.setSearchFieldList(searchFieldList);
        initSearchParam(param);
        return param;
    }

    private void initSearchParam(AbstractSearchParam param) {
        param.setOrderField(orderField);
        param.setHighlightField(highlightField);
        param.setPageNo(pageNo);
        param.setPageSize(pageSize);
        param.setAggregationBuilders(aggregationBuilders);
        param.setTrackTotalHits(trackTotalHits);
        param.setWithOriginalResponse(withOriginalResponse);
    }
}
