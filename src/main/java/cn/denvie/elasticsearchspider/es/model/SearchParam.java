package cn.denvie.elasticsearchspider.es.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索参数。
 *
 * @author denvie
 * @date 2020/8/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchParam {
    private List<SearchField> searchFieldList;
    private OrderField orderField;
    private String highlightPreTags;
    private String highlightPostTags;
    private int pageNo = 1;
    private int pageSize = 10;

    public static class Builder {
        private List<SearchField> searchFieldList = new ArrayList<>();
        private OrderField orderField;
        private String highlightPreTags;
        private String highlightPostTags;
        private int pageNo = 1;
        private int pageSize = 10;

        public Builder searchField(SearchField searchField) {
            this.searchFieldList.add(searchField);
            return this;
        }

        public Builder searchField(String name, Object value, QueryType queryType) {
            this.searchFieldList.add(new SearchField(name, value, queryType));
            return this;
        }

        public Builder orderField(OrderField orderField) {
            this.orderField = orderField;
            return this;
        }

        public Builder highlightPreTags(String highlightPreTags) {
            this.highlightPreTags = highlightPreTags;
            return this;
        }

        public Builder highlightPostTags(String highlightPostTags) {
            this.highlightPostTags = highlightPostTags;
            return this;
        }

        public Builder pageNo(int pageNo) {
            this.pageNo = pageNo;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public SearchParam build() {
            return new SearchParam(searchFieldList, orderField,
                    highlightPreTags, highlightPostTags, pageNo, pageSize);
        }
    }
}
