/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高亮字段。
 *
 * @author denvie
 * @since 2020/8/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighlightField {
    /**
     * 高亮字段
     */
    private String fieldName;
    /**
     * 标签前缀标签
     */
    private String preTags;
    /**
     * 高亮后缀标签
     */
    private String postTags;
}
