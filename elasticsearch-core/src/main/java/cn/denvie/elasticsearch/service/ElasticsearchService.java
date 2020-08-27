/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.service;

import cn.denvie.elasticsearch.model.*;

import java.io.IOException;
import java.util.Map;

/**
 * Elasticsearch查询服务。
 *
 * @author denvie
 * @since 2020/8/4
 */
public interface ElasticsearchService {
    /**
     * 创建索引。
     *
     * @param index 索引名称
     * @return true: 创建成功；false: 创建失败
     * @throws IOException IOException
     */
    boolean createIndex(String index) throws IOException;

    /**
     * 使用指定的索引设置和字段映射创建索引。
     *
     * @param index    索引名称
     * @param settings 索引设置
     * @param mappings 字段映射
     * @return true: 创建成功；false: 创建失败
     * @throws IOException IOException
     */
    boolean createIndex(String index, Map<String, String> settings,
                        Map<String, Map<String, Object>> mappings) throws IOException;

    /**
     * 索引是否已存在。
     *
     * @param index 索引名称
     * @return true: 已存在；false: 不存在
     * @throws IOException IOException
     */
    boolean isIndexExists(String index) throws IOException;

    /**
     * 删除索引。
     *
     * @param index 索引名称
     * @return true: 删除成功；false: 删除失败
     * @throws IOException IOException
     */
    boolean deleteIndex(String index) throws IOException;

    /**
     * 保存文档。
     *
     * @param index   索引名称
     * @param sources 文档实例
     * @return true: 保存成功；false: 保存失败
     * @throws IOException IOException
     */
    boolean saveDocument(String index, EsIndexBean... sources) throws IOException;

    /**
     * 获取文档。
     *
     * @param index    索引名称
     * @param id       文档id
     * @param docClass 文档类型Class
     * @param <T>      文档类型
     * @return 文档实例
     * @throws IOException IOException
     */
    <T> T getDocument(String index, String id, Class<T> docClass) throws IOException;

    /**
     * 更新文档。
     *
     * @param index  索引名称
     * @param id     文档id
     * @param source 文档实例
     * @return true: 更新成功；false: 更新失败
     * @throws IOException IOException
     */
    boolean updateDocument(String index, String id, EsIndexBean source) throws IOException;

    /**
     * 删除文档。
     *
     * @param index 索引名称
     * @param id    文档id
     * @return true: 删除成功；false: 删除失败
     * @throws IOException IOException
     */
    boolean deleteDocument(String index, String id) throws IOException;

    /**
     * 搜索所有文档。
     *
     * @param indexes     索引名称
     * @param searchParam 搜索参数
     * @param docClass    文档类型Class
     * @param <T>         结果实体类型
     * @return 搜索结果列表
     * @throws IOException IOException
     */
    <T> SearchResult<T> searchAllDocuments(String indexes, AbstractSearchParam searchParam, Class<T> docClass)
            throws IOException;

    /**
     * 单项搜索，比如 match, multi_match、term、range。
     *
     * @param indexes     索引名称
     * @param searchParam 搜索参数
     * @param docClass    文档类型Class
     * @param <T>         结果实体类型
     * @return 搜索结果列表
     * @throws IOException IOException
     */
    <T> SearchResult<T> searchDocuments(String indexes, SingleSearchParam searchParam, Class<T> docClass)
            throws IOException;

    /**
     * bool搜索。
     *
     * @param indexes     索引名称
     * @param searchParam 搜索参数
     * @param docClass    文档类型Class
     * @param <T>         结果实体类型
     * @return 搜索结果列表
     * @throws IOException IOException
     */
    <T> SearchResult<T> boolSearchDocuments(String indexes, MultiSearchParam searchParam, Class<T> docClass)
            throws IOException;
}
