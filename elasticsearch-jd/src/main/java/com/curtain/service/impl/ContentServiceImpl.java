package com.curtain.service.impl;

import com.alibaba.fastjson.JSON;
import com.curtain.pojo.Content;
import com.curtain.service.ContentService;
import com.curtain.utils.HTMLParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ：Curtain
 * @date ：Created in 2021/3/12 11:45
 * @description：TODO
 */
@Service
public class ContentServiceImpl implements ContentService {
    
    @Autowired
    private RestHighLevelClient restHighLevelClient;



    @Override
    public boolean parseContent(String keywords) {
        try {
            //将解析数据放入es索引中
            List<Content> contents = new HTMLParseUtil().parseJD(keywords);
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.timeout("2m");
            for (int i = 0; i < contents.size(); i++) {
                bulkRequest.add(
                        new IndexRequest("jd_goods").source(JSON.toJSONString(contents.get(i)), XContentType.JSON)
                );
            }
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return !bulk.hasFailures();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> searchPage(String keywords, int pageNo, int pageSize) throws IOException {
        if (pageNo <= 1)
            pageNo = 1;
        if (pageSize <= 0)
            pageSize = 10;
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        //分页
        sourceBuilder.from((pageNo -1) * pageSize);
        sourceBuilder.size(pageSize);
        
        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keywords);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        
        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        
        //解析结果
        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit: searchResponse.getHits().getHits()){
            //解析高亮字段
            Map<String, HighlightField> highlightFields =  hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String,Object> map = hit.getSourceAsMap();
            //替换高亮字段
            if (title != null){
                Text[] fragments = title.fragments();
                String name = "";
                for (Text text : fragments){
                    name += text;
                }
                map.put("title", name);
            }
            list.add(map);
        }
        return list;
    }
}
