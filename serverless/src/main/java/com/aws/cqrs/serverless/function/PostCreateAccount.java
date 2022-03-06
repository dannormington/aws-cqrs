package com.aws.cqrs.serverless.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.aws.cqrs.infrastructure.exceptions.EventCollisionException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.application.AccountService;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.serverless.command.CreateAccount;
import com.aws.cqrs.serverless.response.CreateAccountResponse;

public class PostCreateAccount extends RequestHandlerBase<CreateAccount, CreateAccountResponse> {

    @Override
    public CreateAccountResponse handleRequest(CreateAccount input, Context context) {

        AccountService accountService = new AccountService();

        try {
            accountService.create(input.getAccountId(), input.getFirstName(), input.getLastName());
            return new CreateAccountResponse(input.getAccountId());
        } catch (EventCollisionException e) {
            String message = buildErrorMessage(409, "Conflict", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        } catch (TransactionFailedException e) {
            String message = buildErrorMessage(500, "InternalServerError", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        } catch (HydrationException e) {
            String message = buildErrorMessage(404, "InternalServerError", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        }
    }
}
