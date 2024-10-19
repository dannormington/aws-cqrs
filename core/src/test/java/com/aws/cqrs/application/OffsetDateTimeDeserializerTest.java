package com.aws.cqrs.application;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class OffsetDateTimeDeserializerTest {
  private final OffsetDateTimeDeserializer deserializer = new OffsetDateTimeDeserializer();

  @Test
  void when_deserialize_expect_success() {
    // Arrange
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer());
    Gson gson = gsonBuilder.create();
    OffsetDateTime expectedObject = OffsetDateTime.parse("2024-10-18T19:28:47.396553-05:00");

    // Act
    OffsetDateTime offsetDateTime =
        gson.fromJson("\"2024-10-18T19:28:47.396553-05:00\"", OffsetDateTime.class);

    // Assert
    assertEquals(expectedObject, offsetDateTime);
  }

  @Test
  void when_serialize_expect_success() {
    // Arrange
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer());
    Gson gson = gsonBuilder.create();
    String expectedValue = "\"2024-10-18T19:28:47.396553-05:00\"";

    // Act
    String json = gson.toJson(OffsetDateTime.parse("2024-10-18T19:28:47.396553-05:00"));

    // Assert
    assertEquals(expectedValue, json);
  }
}
