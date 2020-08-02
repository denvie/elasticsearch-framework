package cn.denvie.elasticsearchspider.service;

import cn.denvie.elasticsearchspider.domain.ElasticSearchBean;

import java.util.List;

public interface ElasticSearchService {

    boolean save(String index, ElasticSearchBean... sources) throws Exception;

    <T> List<T> search(String index, String field, String keyword,
                       int pageNo, int pageSize, Class<T> beanClass) throws Exception;

    <T> List<T> search(String index, String field, String keyword, String preTags, String postTags,
                       int pageNo, int pageSize, Class<T> beanClass) throws Exception;

}
