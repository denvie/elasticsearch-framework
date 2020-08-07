package cn.denvie.elasticsearchspider.es.service;

import cn.denvie.elasticsearchspider.es.model.EsIndexBean;
import cn.denvie.elasticsearchspider.es.model.PagingResult;
import cn.denvie.elasticsearchspider.es.model.SearchParam;

import java.io.IOException;
import java.util.Map;

/**
 * Elasticsearch查询服务。
 *
 * @author denvie
 * @date 2020/8/4
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
     * 使用指定的settings和mapping创建索引。
     *
     * @param index
     * @param settings 配置
     * @param mappings 映射
     * @return true: 创建成功；false: 创建失败
     * @throws IOException IOException
     */
    boolean createIndex(String index, Map<String, String> settings,
                        Map<String, Map<String, Object>> mappings) throws IOException;

    /**
     * 获取索引是否已存在。
     *
     * @param index 索引名称
     * @return true: 已存在；false: 不存在
     * @throws IOException IOException
     */
    boolean isIndexExists(String index) throws IOException;

    /**
     * 保存文档。
     *
     * @param indexes 索引名称
     * @param sources 文档对象
     * @return true: 保存成功；false: 保存失败
     * @throws IOException IOException
     */
    boolean save(String indexes, EsIndexBean... sources) throws IOException;

    /**
     * 搜索文档，适用于 match、term、range 单项查询。
     *
     * @param indexes     索引名称
     * @param searchParam 搜索参数
     * @param beanClass   文档类型Class
     * @param <T>         文档类型
     * @return 搜索结果列表
     * @throws IOException IOException
     */
    <T> PagingResult<T> search(String indexes, SearchParam searchParam, Class<T> beanClass) throws IOException;
}
