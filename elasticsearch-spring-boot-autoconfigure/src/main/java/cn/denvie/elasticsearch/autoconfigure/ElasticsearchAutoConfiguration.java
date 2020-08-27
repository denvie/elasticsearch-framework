/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.autoconfigure;

import cn.denvie.elasticsearch.config.ElasticsearchConfig;
import org.springframework.context.annotation.Import;

/**
 * Elasticsearch auto configuration.
 *
 * @author denvie
 * @since 2020/8/22
 */
@Import({ElasticsearchConfig.class})
public class ElasticsearchAutoConfiguration {
}
