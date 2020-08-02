package cn.denvie.elasticsearchspider.service;

public interface HtmlParseService<T> {

    T parse(String url) throws Exception;

}
