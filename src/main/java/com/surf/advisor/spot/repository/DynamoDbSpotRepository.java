package com.surf.advisor.spot.repository;

import static com.surf.advisor.spot.util.RecordUtils.lastEvaluatedKey;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.surf.advisor.spot.model.SpotQueryProps;
import com.surf.advisor.spot.model.SpotRecord;
import com.surf.advisor.spot.web.api.model.PagedSpotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DynamoDbSpotRepository implements ISpotRepository {

    private final AmazonDynamoDB ddb;
    private final String tableName;

    @Override
    public void put(SpotRecord spotRecord) {
        var ddbRequest = new PutItemRequest(tableName, spotRecord.getValues());

        ddb.putItem(ddbRequest);
    }

    @Override
    public PagedSpotResponse findSpots(SpotQueryProps props) {

        log.debug("Find spots by params: {}", props);

        var page = isEmpty(props.getKeyConditionExp()) ? scanSpots(props) : querySpots(props);

        page.setSize(page.getSpots().size());
        page.setLimit(props.getLimit());

        return page;
    }

    private PagedSpotResponse querySpots(SpotQueryProps props) {

        var spots = props.getKeyConditionExp().parallelStream().flatMap(keyCondition -> {

            var request = new QueryRequest(tableName)
                .withKeyConditionExpression(keyCondition)
                .withFilterExpression(props.getFilterExp())
                .withExpressionAttributeValues(props.getValueMap())
                .withLimit(props.getLimit());

            log.debug("Retrieving spots by DynamoDB Query: {}", request);

            return ddb.query(request).getItems().stream()
                .map(SpotRecord::new).map(SpotRecord::toResponse);
        })
            .limit(props.getLimit())
            .collect(toList());

        var page = new PagedSpotResponse();

        page.setSpots(spots);
        return page;
    }

    private PagedSpotResponse scanSpots(SpotQueryProps queryProps) {

        var request = new ScanRequest(tableName)
            .withFilterExpression(queryProps.getFilterExp())
            .withExpressionAttributeValues(queryProps.getValueMap())
            .withExclusiveStartKey(queryProps.getExclusiveStartKey())
            .withLimit(queryProps.getLimit());

        log.debug("Retrieving spots by DynamoDB Scan: {}", request);

        var scan = ddb.scan(request);

        var spots = scan.getItems().stream()
            .map(SpotRecord::new).map(SpotRecord::toResponse).collect(toList());

        var page = new PagedSpotResponse();

        page.setLastKey(lastEvaluatedKey(scan.getLastEvaluatedKey()));
        page.setSpots(spots);
        return page;
    }
}
