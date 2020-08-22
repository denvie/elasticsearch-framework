# 介绍
封装Elasticsearch简易操作框架**elasticsearch-core**，使用JSoup爬取数据，并整合**elasticsearch-core**进行数据存储与检索。



### 依赖组件
* [Spring Boot](https://spring.io/projects/spring-boot/) 2.3.3
* [ElasticSearch](https://www.elastic.co/cn/elasticsearch/) 7.6.2
* [Guava](https://github.com/google/guava/) 28.2-jre



### SpringBoot整合elasticsearch-core

- 在**pom.xml**中添加starter依赖

```xml
<dependency>
    <groupId>cn.denvie</groupId>
    <artifactId>elasticsearch-spring-boot-starter</artifactId>
    <version>${newest.version}</version>
</dependency>
```

- 在**application.yml**中添加Elasticsearch配置

```yaml
elasticsearch:
  hosts: localhost:9200
  scheme: http
  timeoutSeconds: 30
```

- 注入**ElasticsearchService**

```java
import cn.denvie.elasticsearch.model.*;
import cn.denvie.elasticsearch.service.ElasticsearchService;
import cn.denvie.elasticsearch.utils.SearchParamBuilder;

public class TestService {
    @Autowired
    private ElasticsearchService elasticSearchService;
    
    public void testService() {
        String index = "user-index";
        // 创建索引
        elasticSearchService.createIndex(index);
        // 保存文档
        User user = new User("Denvie", 18, "户外运动，打篮球");
        elasticSearchService.saveDocument(index, User);
        // 单项搜索，比如 match, multi_match、term、range 等
        SingleSearchParam singleSearchParam = new SearchParamBuilder()
                .searchField(new SearchField("name,hobbies", "Denvie户外", QueryType.MULTI_MATCH, null))
                .orderField(new OrderField("age", true))
                .highlightPreTags("<span style='color: read;'>")
                .highlightPostTags("</span")
                .pageNo(1)
                .pageSize(10)
                .buildSingleSearchParam();
        elasticSearchService.searchDocuments(index, singleSearchParam, User.class);
        // bool搜索
        SearchParamBuilder multiSearchParamBuilder = new SearchParamBuilder()
                .searchField("name", "Denvie", QueryType.MATCH, QueryType.BOOL_MUST)
                .searchField("hobbies", "户外", QueryType.MATCH, QueryType.BOOL_MUST);
        RangeValue rangeValue = new RangeValue();
        rangeValue.gte(15);
        rangeValue.lte(25);
        multiSearchParamBuilder.searchField("age", rangeValue, QueryType.RANGE, QueryType.BOOL_MUST);
        multiSearchParamBuilder.pageNo(1).pageSize(10);
        MultiSearchParam multiSearchParam = multiSearchParamBuilder.buildMultiSearchParam();
        elasticSearchService.boolSearchDocuments(index, multiSearchParam, User.class);
        // ...
    }
}
```



#### JSoup爬虫接口

* 爬取京东商品：
    http://localhost:8080/crawlJdGoods?keyword=java&pageNo=1
* 搜索京东商品：
    http://localhost:8080/searchJdGoods?field=title&keyword=java&pageNo=1&pageSize=5
* 多字段搜索：
    http://localhost:8080/searchJdGoods?field=title,shop&keyword=java&pageNo=1&pageSize=5
* 多条件搜索：
    http://localhost:8080/boolSearchJdGoods?title=java&shop=yyy&startTime=2020-08-07 12:12:12&endTime=2020-08-08 12:12:12&pageNo=1&pageSize=5