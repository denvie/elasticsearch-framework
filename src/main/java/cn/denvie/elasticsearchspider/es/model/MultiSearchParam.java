package cn.denvie.elasticsearchspider.es.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 多字段搜索参数。
 *
 * @author denvie
 * @date 2020/8/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiSearchParam extends AbstractSearchParam {
    private List<SearchField> searchFieldList;
}
