/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearchspider.spider.service;

import java.io.IOException;

/**
 * @author denvie
 * @since 2020/8/4
 */
public interface HtmlParseService<T> {
    /**
     * 爬取并解析URL。
     *
     * @param url URL
     * @return 解析结果
     * @throws Exception Exception
     */
    T parse(String url) throws IOException;
}
