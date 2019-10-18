package com.surf.advisor.spot.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.surf.advisor.spot.domain.SpotRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
