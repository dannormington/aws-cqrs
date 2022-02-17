package com.aws.cqrs.serverless.function;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public abstract class RequestHandlerBase<I,O> implements RequestHandler<I,O> {
    protected String buildErrorMessage(int statusCode, String errorType, String message, String requestId) {

        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("errorType", errorType);
        errorPayload.put("httpStatus", statusCode);
        errorPayload.put("requestId", requestId);
        errorPayload.put("message", message);

        return errorPayload.toString();
    }
}
