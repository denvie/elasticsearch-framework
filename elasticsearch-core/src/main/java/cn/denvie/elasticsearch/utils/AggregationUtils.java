/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.utils;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;

import java.time.ZoneId;

/**
 * 聚合搜索工具类。
 *
 * @author denvie
 * @since 2020/8/28
 */
public class AggregationUtils {
    // 默认时区
    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("+08:00");

    /**
     * 获取默认的求合聚合搜索Builder。
     *
     * @param name  聚合结果名称
     * @param field 求和字段
     * @return SumAggregationBuilder
     */
    public static SumAggregationBuilder sumAggregation(String name, String field) {
        return AggregationBuilders.sum(name).field(field).timeZone(DEFAULT_TIME_ZONE);
    }

    /**
     * 获取默认的平均值聚合搜索Builder。
     *
     * @param name  聚合结果名称
     * @param field 平均值字段
     * @return AvgAggregationBuilder
     */
    public static AvgAggregationBuilder avgAggregation(String name, String field) {
        return AggregationBuilders.avg(name).field(field).timeZone(DEFAULT_TIME_ZONE);
    }

    /**
     * 获取默认的分组求合、求平均值聚合搜索Builder。
     *
     * @param name       分组名称
     * @param termsField 分组字段
     * @param sumName    求和结果名称
     * @param sumField   求和字段
     * @param avgName    求平均值结果名称
     * @param avgField   求平均值字段
     * @return SumAggregationBuilder
     */
    public static TermsAggregationBuilder termsSumAvgAggregation(String name, String termsField,
                                                                 String sumName, String sumField,
                                                                 String avgName, String avgField) {
        return AggregationBuilders.terms(name).field(termsField)
                // 首先根据求合值倒序排序，再根据平均值倒序排序
                .order(BucketOrder.compound(
                        BucketOrder.aggregation(sumName, false),
                        BucketOrder.aggregation(avgName, false)))
                .subAggregation(AggregationBuilders.sum(sumName).field(sumField))
                .subAggregation(AggregationBuilders.sum(avgName).field(avgField))
                .timeZone(DEFAULT_TIME_ZONE);
    }

    /**
     * 获取默认的直方图聚合搜索Builder。
     *
     * @param name  聚合结果名称
     * @param field 聚合字段
     * @return HistogramAggregationBuilder
     */
    public static HistogramAggregationBuilder histogramAggregation(String name, String field) {
        return AggregationBuilders.histogram(name).field(field)
                .interval(1)
                .minDocCount(0)
                .timeZone(DEFAULT_TIME_ZONE);
    }

    /**
     * 获取默认的日期直方图聚合搜索Builder。
     *
     * @param name  聚合结果名称
     * @param field 聚合字段
     * @return DateHistogramAggregationBuilder
     */
    public static DateHistogramAggregationBuilder dateHistogram(String name, String field) {
        DateHistogramAggregationBuilder dateHistogram = AggregationBuilders.dateHistogram("group_by_date");
        dateHistogram.field("createTime");
        dateHistogram.calendarInterval(DateHistogramInterval.DAY);
        dateHistogram.format("yyyy-MM-dd");
        dateHistogram.timeZone(DEFAULT_TIME_ZONE);
        dateHistogram.minDocCount(0);
        return dateHistogram;
    }
}
