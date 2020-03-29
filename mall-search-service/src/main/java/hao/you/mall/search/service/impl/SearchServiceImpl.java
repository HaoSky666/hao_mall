package hao.you.mall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import hao.you.mall.bean.PmsSearchParam;
import hao.you.mall.bean.PmsSearchSkuInfo;
import hao.you.mall.common.Constants;
import hao.you.mall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private JestClient jestClient;

    //根据搜索参数返回搜索对象
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        String dsl = getDSL(pmsSearchParam);

        System.out.println(dsl);

        if (StringUtils.isBlank(dsl)) return null;

        Search search = new Search.Builder(dsl).addIndex(Constants.index).addType(Constants.type).build();

        SearchResult result = null;
        try {
            result = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result == null) return null;

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = result.getHits(PmsSearchSkuInfo.class);
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            //高亮
            Map<String, List<String>> highlight = hit.highlight;
            if (highlight != null) {
                String skuName = highlight.get(Constants.skuName).get(0);
                source.setSkuName(skuName);
            }
            pmsSearchSkuInfoList.add(source);
        }
        return pmsSearchSkuInfoList;
    }

    //获得查询es的DSL语句
    private String getDSL(PmsSearchParam pmsSearchParam) {
        //Long转化为String , null 会变为“null”
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueId = pmsSearchParam.getValueId();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //filter
        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termsQueryBuilder = new TermQueryBuilder(Constants.catalog3Id, catalog3Id);
            boolQueryBuilder.filter(termsQueryBuilder);
        }
        if (ArrayUtils.isNotEmpty(valueId)) {
            for (String s : valueId) {
                TermQueryBuilder termsQueryBuilder = new TermQueryBuilder(Constants.skuAttrValueList_valueId, s);
                boolQueryBuilder.filter(termsQueryBuilder);
            }
        }

        //must
        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(Constants.skuName, keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);

        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.field(Constants.skuName);
        highlightBuilder.postTags("</span>");

        searchSourceBuilder.highlight(highlightBuilder);
        // sort
        searchSourceBuilder.sort(Constants.id, SortOrder.DESC);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(40);

        return searchSourceBuilder.toString();
    }
}
