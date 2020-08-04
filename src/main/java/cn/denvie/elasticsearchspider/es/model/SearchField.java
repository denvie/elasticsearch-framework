package cn.denvie.elasticsearchspider.es.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchField {

    private String name;
    private Object value;
    private QueryType queryType;

}
