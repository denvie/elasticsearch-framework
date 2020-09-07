# 简介
封装Elasticsearch简易操作框架**elasticsearch-client**，使用JSoup爬取数据，并整合**elasticsearch-client**进行数据存储与检索。



## 依赖组件
* [Spring Boot](https://spring.io/projects/spring-boot/) 2.3.3
* [ElasticSearch](https://www.elastic.co/cn/elasticsearch/) 7.6.2
* [Guava](https://github.com/google/guava/) 28.2-jre



## SpringBoot整合elasticsearch-client

- 在**pom.xml**中添加starter依赖

```xml
<dependency>
    <groupId>cn.denvie.elasticsearch</groupId>
    <artifactId>elasticsearch-spring-boot-starter</artifactId>
    <version>${newest.version}</version>
</dependency>
```

- 在**application.yml**中添加Elasticsearch配置

```yaml
elasticsearch:
  hosts: localhost:9200,localhost:19200
  scheme: http
  connect-timeout: 10
  socket-timeout: 5
  search-timeout: 30
  enable-logger: true
```

- 注入**ElasticsearchService**，使用示例：

```java
import cn.denvie.elasticsearch.client.model.*;
import cn.denvie.elasticsearch.client.service.ElasticsearchService;
import cn.denvie.elasticsearch.client.utils.SearchParamBuilder;

public class TestService {
    @Autowired
    private ElasticsearchService elasticSearchService;
    
    public void testService() {
        String index = "user-index";
        // 创建索引
        elasticSearchService.createIndex(index);
        
        // 保存文档 User(name, age, hobbies, createTime)
        User user = new User("Denvie", 18, "户外运动，打篮球", new Date());
        elasticSearchService.saveDocument(index, User);
        
        // 单项搜索，比如 match, multi_match、term、range 等
        SingleSearchParam singleSearchParam = new SearchParamBuilder()
                .searchField(new SearchField("name", "Denvie", QueryType.MATCH, null))
                //.searchField(new SearchField("name, hobbies", "Denvie户外", QueryType.MULTI_MATCH, null))
                .orderField(new OrderField("age", true))
            	.highlightField(new HighlightField(
                        "name", "<span style='color: read;'>", "</span>"))
                .pageNo(1).pageSize(10).trackTotalHits(true)
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
        multiSearchParamBuilder.pageNo(1).pageSize(10).trackTotalHits(true);
        MultiSearchParam multiSearchParam = multiSearchParamBuilder.buildMultiSearchParam();
        elasticSearchService.boolSearchDocuments(index, multiSearchParam, User.class);
        
        // 日期直方图聚合搜索
        SearchParamBuilder searchParamBuilder = new SearchParamBuilder()
                .searchField(new SearchField(field, keyword, SearchType.MATCH, null, true))
                .aggregation(AggregationUtils.dateHistogram("group_by_date", "createTime"))
                .withOriginalResponse(true).pageNo(-1).pageSize(0);
        elasticSearchService.searchDocuments(index,
                searchParamBuilder.buildSingleSearchParam(), null).toString();
    }
}
```



## jsoup-spider-demo示例接口

* 爬取京东商品：

```
http://localhost:8080/crawlJdGoods?keyword=java&pageNo=1
```

* 单字段搜索：

```
http://localhost:8080/searchJdGoods?field=title&keyword=java&pageNo=1&pageSize=5
```

* 多字段搜索：

```
http://localhost:8080/searchJdGoods?field=title,shop&keyword=java&pageNo=1&pageSize=5
```

* 多条件搜索：

```
http://localhost:8080/boolSearchJdGoods?title=java&shop=人民邮电出版社&startTime=2020-08-07 12:12:12&endTime=2020-08-08 12:12:12&pageNo=1&pageSize=5
```

* 日期直方图聚合搜索：

 ```
http://localhost:8080/aggregationSearchJdGoods?field=title&keyword=小米10至尊版
 ```





## 自定义spring-boot-starter步骤

==一、新建**xxx-spring-boot-autoconfigure**模块==

1. 新建**XxxAutoConfiguration**自动配置类及**XxxProperties**属性配置类，例如：

   ```java
   @Configuration(proxyBeanMethods = false)
   @EnableConfigurationProperties(ElasticsearchProperties.class)
   public class ElasticsearchAutoConfiguration {
   
       @Bean
       @ConditionalOnMissingBean
       public RestHighLevelClient restHighLevelClient(ElasticsearchProperties properties) {
           HttpHost[] httpHosts = new HttpHost[properties.getHosts().size()];
           for (int i = 0; i < properties.getHosts().size(); i++) {
               String[] splits = properties.getHosts().get(i).split(":");
               httpHosts[i] = new HttpHost(splits[0].trim(),
                       Integer.parseInt(splits[1].trim()), properties.getScheme());
           }
           return new RestHighLevelClient(RestClient.builder(httpHosts));
       }
   
       @Bean
       @ConditionalOnMissingBean
       public ElasticsearchService elasticsearchService(RestHighLevelClient restHighLevelClient,
                                                        ElasticsearchProperties properties) {
           ElasticsearchService elasticsearchService = new ElasticsearchServiceImpl(
                   restHighLevelClient, properties.getTimeoutSeconds());
           return elasticsearchService;
       }
   }
   ```

   ```java
   @Data
   @ConfigurationProperties(prefix = "elasticsearch")
   public class ElasticsearchProperties {
       /**
        * Elasticsearch主机，多个以","分隔
        */
       private List<String> hosts = Collections.singletonList("localhost:9200");
       /**
        * 连接协议
        */
       private String scheme = "http";
       /**
        * 搜索超时时间（秒）
        */
       private int searchTimeout = 30;
   }
   ```

   

2. 在**resources**资源文件夹下新建**META-INF/spring.factories**，内容如下：

   ```properties
   # Auto Configure
   org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
   your.package.XxxAutoConfiguration
   ```

   

3. **pom.xml**配置文件中，除了依赖自己的服务jar包之外，还需要添加以下依赖，注意需要配置为使用**maven-jar-plugin**进行打包：

   ```xml
   <dependencies>     
       <dependency>
           <groupId>your.groupId</groupId>
           <artifactId>your.business.artifactId</artifactId>
           <version>1.0.0-SNAPSHOT</version>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-autoconfigure</artifactId>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-autoconfigure-processor</artifactId>
           <optional>true</optional>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-configuration-processor</artifactId>
           <optional>true</optional>
       </dependency>
   </dependencies>
   <build>
       <plugins>
           <!-- 注意：这里要使用Apache Maven打成普通Jar包 -->
           <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-jar-plugin</artifactId>
           </plugin>
       </plugins>
   </build>
   ```

   

==二、新建**xxx-spring-boot-starter**模块==

这个模块只需要一个**pom.xml**把xxx-spring-boot-autoconfigure依赖进来就可以了，这里也需要使用**maven-jar-plugin**进行打包，比如：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>cn.denvie</groupId>
        <artifactId>elasticsearch-spring-boot-autoconfigure</artifactId>
        <version>${parent.version}</version>
    </dependency>
</dependencies>
<build>
    <plugins>
        <!-- 注意：这里要使用Apache Maven打成普通Jar包 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

​      

==三、其他项目中直接依赖**xxx-spring-boot-starter**，配置需要的属性即可==



