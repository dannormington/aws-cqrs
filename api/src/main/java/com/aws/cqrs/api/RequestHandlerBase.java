package com.aws.cqrs.api;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.aws.cqrs.api.response.ErrorResponse;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public abstract class RequestHandlerBase implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    protected final Gson gson;

    protected RequestHandlerBase(Gson gson) {
        this.gson = gson;
    }

    protected APIGatewayProxyResponseEvent buildSuccessfulResponse() {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        return response;
    }

    protected APIGatewayProxyResponseEvent buildInvalidBodyResponse() {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        response.setBody(gson.toJson(new ErrorResponse("Invalid body")));
        response.setStatusCode(400);
        return response;
    }

    protected APIGatewayProxyResponseEvent buildServerError(String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        response.setBody(gson.toJson(new ErrorResponse(message)));
        response.setStatusCode(500);
        return response;
    }
}
