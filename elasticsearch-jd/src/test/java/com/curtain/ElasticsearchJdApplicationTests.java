package com.curtain;

import ch.qos.logback.core.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.curtain.pojo.Content;
import com.curtain.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ElasticsearchJdApplicationTests {

    @Resource
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    @Test
    //索引的创建
    void createIndex(){
        //创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("curtain_index");
        //客户端执行请求
        try {
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    //获取索引
    void isExist(){
        GetIndexRequest request = new GetIndexRequest("curtain_index");
        try {
            boolean flag = client.indices().exists(request, RequestOptions.DEFAULT);
            System.out.println(flag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    //删除索引
    void deleteIndex(){
        DeleteIndexRequest request = new DeleteIndexRequest("curtain_index");
        try {
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            System.out.println(response.isAcknowledged());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    //添加文档
    void addDoc() throws IOException {
        //创建对象
        User user = new User("curtain", 3);
        //创建请求
        IndexRequest request = new IndexRequest("curtain_index");
        //规则 PUT /curtain_index/_doc/i
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
//        request.timeout("1s");效果和上面的一样
        //将我们的数据放入请求 json
        request.source(JSON.toJSONString(user), XContentType.JSON);
        //客户端发送请求
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());
    }

    @Test
    //获取文档，判断是否存在
    void testIsExist() throws IOException {
        GetRequest getRequest = new GetRequest("curtain_index", "1");
        //不获取返回的_source的上下文 效率更高
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Test
    //获取文档信息
    void getDoc() throws IOException {
        GetRequest getRequest = new GetRequest("curtain_index", "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString());
        System.out.println(getResponse);
    }

    @Test
    //更新文档信息
    void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("curtain_index", "1");
        updateRequest.timeout("1s");
        User user = new User("庆腾说java", 18);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    @Test
    //删除文档记录
    void deleteDoc() throws IOException {
        DeleteRequest request = new DeleteRequest("curtain_index", "1");
        request.timeout("1s");
        DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());

    }

    @Test
    //批量插入数据
    void bulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        List<User> list = new ArrayList<>();
        list.add(new User("curtain1", 3));
        list.add(new User("curtain2", 3));
        list.add(new User("curtain3", 3));
        list.add(new User("curtain4", 3));
        list.add(new User("curtain5", 3));
        list.add(new User("curtain6", 3));

        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(new IndexRequest("curtain_index")
                    .id(""+i+1)
                    .source(JSON.toJSONString(list.get(i)), XContentType.JSON));
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures());
    }

    @Test
    //查询
    //SearchRequest 搜索请求
    //SearchSourceBuilder 条件构造
    //HighlightBuilder 构建高亮
    //MatchAllQueryBuilder
    void search() throws IOException {
        SearchRequest searchRequest = new SearchRequest("curtain_index");
        //构建搜索的条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //查询条件，可以使用QueryBuilders工具类实现
        //QueryBuilders.termQuery精确匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "cur");
        //MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("========================================");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }

    }
}
