package com.kuang.kuangshenesapi;

import com.alibaba.fastjson.JSON;
import com.kuang.kuangshenesapi.pojo.User;
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
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.UpperCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class KuangshenEsApiApplicationTests {

    // 面向对象来操作
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    
    // 测试索引的创建
    @Test
    public void testCreateIndex() throws IOException {
        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("jd_goods");
        // 执行创建请求
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);

        System.out.println(response);
    }
    
    // 测试获取索引,判断其是否存在
    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("kuang_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    
    // 测试删除索引
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("jd_goods");
        // 删除
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }
    
    // 测试添加文档
    @Test
    public void testAddDocument() throws IOException {
        // 创建对象
        User user = new User("狂神说", 3);
        // 创建请求
        IndexRequest request = new IndexRequest("kuang_index");
        
        // 设置规则put /kuang_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");
        
        // 将我们的数据放入请求
        request.source(JSON.toJSONString(user), XContentType.JSON);
        // 客户端发送请求,获取相应的结果
        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.status());
    }
    
    // 测试获取文档，判断是否存在 get /kuang_shen/_doc/1
    @Test
    public void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("kuang_index", "1");
        // 不获取返回的_source的上下文了
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    
    // 获取文档信息
    @Test
    public void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("kuang_index", "1");
        GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());// 打印文档的内容
        System.out.println(response);// 返回的全部内容和命令格式一样的
    }

    // 更新文档信息
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("kuang_index", "1");
        updateRequest.timeout("1s");
        User user = new User("狂神说java", 18);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(response.status());// 打印文档的内容
        System.out.println(response);// 返回的全部内容和命令格式一样的
    }

    // 删除文档信息
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("kuang_index", "1");
        deleteRequest.timeout("1s");
        DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(response.status());// 打印文档的内容
        System.out.println(response);// 返回的全部内容和命令格式一样的
    }
    
    // 批量插入，真实项目
    @Test
    public void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        ArrayList<User> userList = new ArrayList<>();
        
        userList.add(new User("kuangshen4", 3));
        userList.add(new User("kuangshen5", 3));
        userList.add(new User("kuangshen6", 3));
        userList.add(new User("haha4", 3));
        userList.add(new User("haha5", 3));
        userList.add(new User("haha6", 3));

        // 批处理请求
        for (int i = 0; i < userList.size(); i++) {
            // 批量更新和删除在这里操作就可以
            bulkRequest.add(
                    new IndexRequest("kuang_index")
                    .id("" + (i + 1))//可以不设置Id，会给你一个随机Id
                    .source(JSON.toJSONString(userList.get(i)), XContentType.JSON));
        }

        BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(response.status());
        System.out.println(response.hasFailures());// 是否失败，返回false代表成功
    }
    
    // 查询
    @Test
    public void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("kuang_index");
        // 构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        // 高亮
//        sourceBuilder.highlighter();
        
        // 查询条件，使用QueryBuilders 工具实现
        // 精确查询
//        QueryBuilders.termQuery("name", "haha4");
//        QueryBuilders.matchAllQuery() 匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "haha4");
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.from(0);
        sourceBuilder.size(3);
        
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("=======================");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap()); 
        }
    }
}
