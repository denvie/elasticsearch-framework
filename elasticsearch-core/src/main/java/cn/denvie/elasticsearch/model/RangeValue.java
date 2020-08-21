/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.util.HashMap;

/**
 * 封装范围查询的值。
 *
 * @author denvie
 * @since 2020/8/4
 */
public class RangeValue extends HashMap<String, Object> {

    public Object gt() {
        return this.get("gt");
    }

    public void gt(Object gt) {
        this.put("gt", gt);
    }

    public Object gte() {
        return this.get("gte");
    }

    public void gte(Object gte) {
        this.put("gte", gte);
    }

    public Object lt() {
        return this.get("lt");
    }

    public void lt(Object lt) {
        this.put("lt", lt);
    }

    public Object lte() {
        return this.get("lte");
    }

    public void lte(Object lte) {
        this.put("lte", lte);
    }

    public String format() {
        return (String) this.get("format");
    }

    public void format(String format) {
        this.put("format", format);
    }

    public float boost() {
        return NumberUtils.toFloat((String) this.get("boost"), 1.0f);
    }

    public void boost(float boost) {
        this.put("boost", boost);
    }

    public RangeQueryBuilder inflate(RangeQueryBuilder builder) {
        if (builder == null) return null;
        if (gt() != null) builder.gt(gt());
        if (gte() != null) builder.gte(gte());
        if (lt() != null) builder.lt(lt());
        if (lte() != null) builder.lte(lte());
        if (format() != null) builder.format(format());
        builder.boost(boost());
        return builder;
    }
}
