package cn.denvie.elasticsearchspider.service;

import cn.denvie.elasticsearchspider.domain.ElasticSearchBean;

import java.util.List;
import java.util.Map;

public interface ElasticSearchService {

    boolean createIndex(String index) throws Exception;

    boolean createIndex(String index, Map<String, String> settings,
                        Map<String, Map<String, Object>> mappings) throws Exception;

    boolean isIndexExists(String index) throws Exception;

    boolean saveDocuments(String index, ElasticSearchBean... sources) throws Exception;

    <T> List<T> searchDocuments(String index, String field, String keyword,
                                int pageNo, int pageSize, Class<T> beanClass) throws Exception;

    <T> List<T> searchDocuments(String index, String field, String keyword, String preTags, String postTags,
                                int pageNo, int pageSize, Class<T> beanClass) throws Exception;

}
