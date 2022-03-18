package com.aws.cqrs.infrastructure.persistence;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Simple event that is stored
 */
@DynamoDbBean
public class EventModel {
    private String id;
    private Long version;
    private String kind;
    private String json;

    public EventModel() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public String getId() {
        return this.id;
    }

    public void setId(String id) { this.id = id; }

    @DynamoDbSortKey
    @DynamoDbAttribute("Version")
    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @DynamoDbAttribute("Kind")
    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @DynamoDbAttribute("Event")
    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}