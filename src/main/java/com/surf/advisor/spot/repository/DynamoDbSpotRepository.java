package com.surf.advisor.spot.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.surf.advisor.spot.mapper.SpotMapper;
import com.surf.advisor.spot.model.SpotQueryProps;
import com.surf.advisor.spot.model.SpotRecord;
import com.surf.advisor.spot.web.api.model.PagedSpotResponse;
import com.surf.advisor.spot.web.api.model.Spot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.surf.advisor.spot.model.SpotRecord.ID;
import static com.surf.advisor.spot.util.RecordUtils.lastEvaluatedKey;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DynamoDbSpotRepository implements ISpotRepository {

    private final DynamoDBMapper dbMapper;
    private final AmazonDynamoDB ddb;
    private final String tableName;

    @Override
    public Optional<SpotRecord> get(String id) {
        var request = new QueryRequest(tableName)
            .withKeyConditionExpression("id = :v_id")
            .withExpressionAttributeValues(Map.of(":v_id", new AttributeValue(id)));

        return ddb.query(request).getItems().stream().findAny()
            .map(item -> dbMapper.marshallIntoObject(SpotRecord.class, item));
    }

    @Override
    public void put(SpotRecord spotRecord) {
        dbMapper.save(spotRecord);
    }

    @Override
    public PagedSpotResponse findSpots(SpotQueryProps props) {

        log.debug("Find spots by params: {}", props);

        var page = isEmpty(props.getKeyConditions()) ? scanSpots(props) : querySpots(props);

        page.setSize(page.getSpots().size());
        page.setLimit(props.getLimit());

        return page;
    }

    @Override
    public List<String> findSpotIds(SpotQueryProps props) {

        props.setProjectionExp(ID);

        return findSpots(props).getSpots().stream()
            .map(Spot::getId).collect(toList());
    }

    private PagedSpotResponse querySpots(SpotQueryProps props) {

        var spots = props.getKeyConditions().parallelStream().flatMap(keyCondition -> {

            var request = new QueryRequest(tableName)
                .withKeyConditionExpression(keyCondition.getExpression())
                .withFilterExpression(props.getFilterExp())
                .withExpressionAttributeValues(keyCondition.getValueMap())
                .withProjectionExpression(props.getProjectionExp())
                .withLimit(props.getLimit());

            log.debug("Retrieving spots by DynamoDB Query: {}", request);

            return ddb.query(request).getItems().stream()
                .map(item -> dbMapper.marshallIntoObject(SpotRecord.class, item))
                .map(SpotMapper.INSTANCE::map);
        })
            .limit(props.getLimit())
            .collect(toList());

        var page = new PagedSpotResponse();

        page.setSpots(spots);
        return page;
    }

    private PagedSpotResponse scanSpots(SpotQueryProps props) {

        var request = new ScanRequest(tableName)
            .withFilterExpression(props.getFilterExp())
            .withExpressionAttributeValues(props.getValueMap())
            .withProjectionExpression(props.getProjectionExp())
            .withExclusiveStartKey(props.getExclusiveStartKey())
            .withLimit(props.getLimit());

        log.debug("Retrieving spots by DynamoDB Scan: {}", request);

        var scan = ddb.scan(request);

        var spots = scan.getItems().stream()
            .map(item -> dbMapper.marshallIntoObject(SpotRecord.class, item))
            .map(SpotMapper.INSTANCE::map)
            .collect(toList());

        var page = new PagedSpotResponse();

        page.setLastKey(lastEvaluatedKey(scan.getLastEvaluatedKey()));
        page.setSpots(spots);
        return page;
    }
}
