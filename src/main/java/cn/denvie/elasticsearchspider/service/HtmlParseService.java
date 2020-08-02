package cn.denvie.elasticsearchspider.service;

import java.net.MalformedURLException;

public interface HtmlParseService<T> {

    T parse(String url) throws Exception;

}
