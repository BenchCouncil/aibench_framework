package org.benchcouncil.aibench;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class SearchPlaner {

    private final RestHighLevelClient excellentItemSearchClient;
    private final RestHighLevelClient goodItemSearchClient;
    private final RestHighLevelClient badItemSearchClient;
    private final RestHighLevelClient rankingSystemClient;
    private final RestHighLevelClient summarySystemClient;
    private final int FETCH_SIZE = 10;
    private final int PAGE_SIZE = 10;
    private final String FIELD = "title";

    int totalSearch = 0;
    int goodSearch = 0;
    int badSearch = 0;

    private String excellentItemsIndex;
    private String goodItemsIndex;
    private String badItemsIndex;
    private String rankingIndex;
    private String summaryIndex;
    private String qpURL;

    public SearchPlaner(@Value("${qp.url}") String qpURL,
                        @Value("${excellent_item_index}") String excellentItemsIndex,
                        @Value("${good_item_index}") String goodItemsIndex,
                        @Value("${bad_item_index}") String badItemsIndex,
                        @Value("${ranking_index}") String rankingIndex,
                        @Value("${summary_index}") String summaryIndex) {
        ApplicationContext context = new ClassPathXmlApplicationContext("factorybean-spring-ctx.xml");

        this.excellentItemSearchClient = ((SearchClient) context.getBean("excellentItemSearchClient")).getClient();
        this.goodItemSearchClient = ((SearchClient) context.getBean("goodItemSearchClient")).getClient();
        this.badItemSearchClient = ((SearchClient) context.getBean("badItemSearchClient")).getClient();
        this.rankingSystemClient = ((SearchClient) context.getBean("rankingSystemClient")).getClient();
        this.summarySystemClient = ((SearchClient) context.getBean("summarySystemClient")).getClient();
        this.qpURL = qpURL;
        this.excellentItemsIndex = excellentItemsIndex;
        this.goodItemsIndex = goodItemsIndex;
        this.badItemsIndex = badItemsIndex;
        this.rankingIndex = rankingIndex;
        this.summaryIndex = summaryIndex;
    }


    @GetMapping("/")
    String welcome() {
        return "Welcome!";
    }

    @PostMapping("/test")
    String getMyId(@RequestBody QueryInfo queryInfo) {
        log.debug(String.valueOf(queryInfo));
        return String.valueOf(new Random().nextLong());
    }

    @PostMapping("/search")
    List<GetResponse> search(@RequestBody QueryInfo queryInfo) throws IOException {
        long[] timeStamps = new long[5];
        timeStamps[0] = System.nanoTime();
        List<Double> weights = analyseQuery(queryInfo.getQid(), queryInfo.getUid(), queryInfo.getQuery());

        timeStamps[1] = System.nanoTime();
        List<String> docIds = matchPhase(queryInfo.getQuery());

        timeStamps[2] = System.nanoTime();
        docIds = rankingPhase(docIds, rankingIndex, queryInfo.getQuery(), weights);

        timeStamps[3] = System.nanoTime();
        List<GetResponse> summaries = fetchPhase(summaryIndex, docIds);

        timeStamps[4] = System.nanoTime();
        log.info(queryInfo.getQid() + ":" + Arrays.toString(timeStamps));
        log.info("totalSearch: " + totalSearch + ", goodSearch: " + goodSearch + ", badSearch: " + badSearch);

        return summaries;
    }

    private List<Double> analyseQuery(int qid, String uid, String query) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<QueryInfo> request = new HttpEntity<>(new QueryInfo(qid, uid, query));
        Weights weights = restTemplate.postForObject(qpURL, request, Weights.class);

        return Objects.requireNonNull(weights).getWeights();
    }

    private List<String> matchPhase(String query) throws IOException {
        totalSearch++;
        SearchRequest request = buildRequest(excellentItemsIndex, FIELD, query, FETCH_SIZE);
        List<String> docIds = new ArrayList<>();
        SearchResponse response = excellentItemSearchClient.search(request, RequestOptions.DEFAULT);
        addToDocs(docIds, response);

        if (response.getHits().totalHits < FETCH_SIZE) {
            goodSearch++;
            request = buildRequest(goodItemsIndex, FIELD, query, FETCH_SIZE);
            response = goodItemSearchClient.search(request, RequestOptions.DEFAULT);
            docIds.clear();
            addToDocs(docIds, response);

            long fetchedSize = response.getHits().totalHits;
            if (fetchedSize < FETCH_SIZE) {
                badSearch++;
                request = buildRequest(badItemsIndex, FIELD, query, (int) (FETCH_SIZE - fetchedSize));
                response = badItemSearchClient.search(request, RequestOptions.DEFAULT);
                addToDocs(docIds, response);
            }
        }

        log.debug("matchPhase: " + docIds.toString());

        return docIds;
    }

    private SearchRequest buildRequest(String index, String field, String query, int fetchSize) {
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.fetchSource(false);
        builder.query(new MatchQueryBuilder(field, query));
        builder.from(0);
        builder.size(FETCH_SIZE);
        request.source(builder);
        return request;
    }

    private void addToDocs(List<String> docIds, SearchResponse response) {
        for (SearchHit hit : response.getHits().getHits()) {
            docIds.add(hit.getId());
        }
    }

    private List<String> rankingPhase(List<String> docIds, String index, String query, List<Double> weights) throws IOException {

        String queryTemplate = "{\"_source\":false,\"query\":{\"bool\":{\"filter\":{\"terms\":{\"_id\":%s}}}},\"rescore\":{\"window_size\":%d,\"query\":{\"rescore_query\":{\"function_score\":{\"query\":{\"match\":{\"title\":\"%s\"}},\"functions\":[{\"field_value_factor\":{\"field\":\"price\",\"factor\":%s}},{\"field_value_factor\":{\"field\":\"ratesum\",\"factor\":%s}}],\"boost\":%s,\"score_mode\":\"sum\",\"boost_mode\":\"sum\"}}}}}";
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String idsString = ow.writeValueAsString(docIds);

        String queryJsonString = String.format(
                queryTemplate,
                idsString, FETCH_SIZE, query,
                String.valueOf(weights.get(1)),
                String.valueOf(weights.get(2)),
                String.valueOf(weights.get(0)));
        log.debug(queryJsonString);

        Request request = new Request("GET", "/" + index + "/_search");
        request.setJsonEntity(queryJsonString);
        Response response = rankingSystemClient.getLowLevelClient().performRequest(request);

        String responseJson = EntityUtils.toString(response.getEntity());

        Pattern pattern = Pattern.compile("_id\":\"(.*?)\",");
        Matcher matcher = pattern.matcher(responseJson);

        List<String> rescoreDocIds = new ArrayList<>();
        while (matcher.find()) {
            rescoreDocIds.add(matcher.group(1));
        }

        log.debug("rankingPhase: " + rescoreDocIds.toString());

        return rescoreDocIds.subList(0, Math.min(PAGE_SIZE, rescoreDocIds.size()));
    }

    private List<GetResponse> fetchPhase(String index, List<String> docIds) throws IOException {
        List<GetResponse> responses = new ArrayList<>();
        if (docIds.isEmpty()) {
            return responses;
        }

        MultiGetRequest request = new MultiGetRequest();
        docIds.forEach(id -> request.add(index, "_doc", id));
        MultiGetResponse response = summarySystemClient.mget(request, RequestOptions.DEFAULT);

        for (MultiGetItemResponse multiGetItemResponse : response) {
//            log.info(String.valueOf(multiGetItemResponse.getResponse()));
            responses.add(multiGetItemResponse.getResponse());
        }

        return responses;
    }
}
