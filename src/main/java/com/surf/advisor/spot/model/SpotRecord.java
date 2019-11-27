package com.surf.advisor.spot.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.surf.advisor.spot.web.api.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType.S;
import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType.SS;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "SPOT")
public class SpotRecord {

    public static final String ID = "id";
    static final String STATUS = "spot_status";
    static final String COUNTRY = "country";
    static final String DIFFICULTY = "difficulty";
    static final String WATER_TYPE = "waterType";
    static final String DEPTH = "depth";

    static final String SPORT_TYPES = "sportTypes";
    static final String FACILITIES = "facilities";

    static final String WAVE_AVG_PERIOD = "waveAvgPeriod";
    static final String WAVE_AVG_SIZE = "waveAvgSize";

    static final String STATE = "spot_state";
    static final String CITY = "city";
    static final String NAME = "name";

    @DynamoDBHashKey
    private String id;
    @DynamoDBRangeKey
    public String rangeKey;

    @DynamoDBTyped(S)
    @DynamoDBAttribute(attributeName = "spot_status")
    private SpotStatus status;
    @DynamoDBTyped(S)
    @DynamoDBAttribute
    private CountryCode country;
    @DynamoDBAttribute(attributeName = "spot_state")
    private String state;
    @DynamoDBAttribute
    private String city;
    @DynamoDBAttribute
    private String name;

    @DynamoDBTyped(SS)
    @DynamoDBAttribute
    private Set<SportType> sportTypes;
    @DynamoDBTyped(S)
    @DynamoDBAttribute
    private WaterType waterType;
    @DynamoDBTyped(SS)
    @DynamoDBAttribute
    private Set<SpotFacility> facilities;
    @DynamoDBTyped(S)
    @DynamoDBAttribute
    private Difficulty difficulty;
    @DynamoDBTyped(S)
    @DynamoDBAttribute
    private Depth depth;
    @DynamoDBAttribute
    private Integer waveAvgPeriod;
    @DynamoDBAttribute
    private Integer waveAvgSize;

    @DynamoDBTyped(SS)
    @DynamoDBAttribute
    private Set<BasicWindDirection> bestWindDirections;
    @DynamoDBTyped(SS)
    @DynamoDBAttribute
    private Set<BasicWindDirection> worstWindDirections;
    @DynamoDBAttribute
    private String dangers;
    @DynamoDBTyped(S)
    @DynamoDBAttribute
    private Popularity popularity;
    @DynamoDBTyped(S)
    @DynamoDBAttribute
    private ChopSize chopSize;
    @DynamoDBAttribute
    private Integer waveQuality;
    @DynamoDBAttribute
    private Integer waveMaxSize;
    @DynamoDBTyped(S)
    @DynamoDBAttribute
    private WaterCurrent waterCurrent;

    @DynamoDBAttribute
    private Map<String, String> descriptions;
    @DynamoDBAttribute
    private List<String> photoUrls;
    @DynamoDBAttribute
    private List<String> liveCamera;

}
