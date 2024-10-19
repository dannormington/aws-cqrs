package com.aws.cqrs.infrastructure.exceptions;

import java.util.UUID;

/** Base exception class for aggregate based exceptions */
public abstract class AggregateException extends RuntimeException {

  private static final String ERROR_FORMAT = "Aggregate Id : %s - Message: %s";

  private final UUID aggregateId;

  /**
   * Constructor
   *
   * @param aggregateId The aggregate id.
   * @param message The error message.
   */
  protected AggregateException(UUID aggregateId, String message) {
    super(message);
    this.aggregateId = aggregateId;
  }

  /**
   * Constructor
   *
   * @param source The source of the exception.
   * @param aggregateId The aggregate id.
   * @param message The error message.
   */
  protected AggregateException(Throwable source, UUID aggregateId, String message) {
    super(message, source);
    this.aggregateId = aggregateId;
  }

  /**
   * Get the aggregate id.
   *
   * @return The aggregate id.
   */
  public UUID getAggregateId() {
    return aggregateId;
  }

  /** Get the error message */
  public String getMessage() {

    String aggregateIdString = aggregateId == null ? "NOT SPECIFIED" : aggregateId.toString();

    return String.format(ERROR_FORMAT, aggregateIdString, super.getMessage());
  }
}
