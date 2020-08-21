## 介绍
使用JSoup爬取数据，并整合Elasticsearch进行数据存储与检索。


#### 依赖组件
* [Spring Boot](https://spring.io/projects/spring-boot/) 2.3.3
* [ElasticSearch](https://www.elastic.co/cn/elasticsearch/) 7.6.2
* [Guava](https://github.com/google/guava/) 28.2-jre
* [Commons Lang](http://commons.apache.org/proper/commons-lang/) 3.11

#### 接口
* 爬取京东商品：
    http://localhost:8080/crawlJdGoods?keyword=java&pageNo=1
* 搜索京东商品：
    http://localhost:8080/searchJdGoods?keyword=java&pageNo=1&pageSize=5
* 多字段搜索：
    http://localhost:8080/searchJdGoods?field=title,shop&keyword=xxx&pageNo=1&pageSize=5
* 多条件搜索：
    http://localhost:8080/boolSearchJdGoods?title=xxx&shop=yyy&startTime=2020-08-07 12:12:12&endTime=2020-08-08 12:12:12&pageNo=1&pageSize=5