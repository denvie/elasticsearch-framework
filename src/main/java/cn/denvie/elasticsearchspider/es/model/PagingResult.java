package cn.denvie.elasticsearchspider.es.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果。
 *
 * @author denvie
 * @date 2020/8/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingResult<T> {

    private long total;
    private List<T> dataList;
    private int pageNo;
    private int pageSize;

}
