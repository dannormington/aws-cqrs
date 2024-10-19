package com.aws.cqrs.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.aws.cqrs.api.command.CreateAccountCommand;
import com.aws.cqrs.application.AccountService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.concurrent.CompletionException;
import javax.inject.Inject;

public class CreateAccountHandler extends RequestHandlerBase {

  private final AccountService accountService;

  @Inject
  public CreateAccountHandler(AccountService accountService, Gson gson) {
    super(gson);
    this.accountService = accountService;
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
    String body = apiGatewayProxyRequestEvent.getBody();

    if (body == null || body.isBlank()) {
      return buildInvalidBodyResponse();
    }

    try {
      CreateAccountCommand command = gson.fromJson(body, CreateAccountCommand.class);

      try {
        accountService
            .create(command.getAccountId(), command.getFirstName(), command.getLastName())
            .join();
        return buildSuccessfulResponse();
      } catch (CompletionException x) {
        Throwable cause = x.getCause();
        return buildServerError(cause.getMessage());
      }

    } catch (JsonSyntaxException x) {
      return buildInvalidBodyResponse();
    }
  }
}
