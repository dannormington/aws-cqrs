package com.aws.cqrs.serverless.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.EventCollisionException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.application.AccountService;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.serverless.command.AccountWithdraw;
import com.aws.cqrs.serverless.response.AccountWithdrawResponse;

public class PostAccountWithdraw extends RequestHandlerBase<AccountWithdraw, AccountWithdrawResponse> {

    @Override
    public AccountWithdrawResponse handleRequest(AccountWithdraw input, Context context) {

        AccountService accountService = new AccountService();

        try {
            accountService.withdraw(input.getAccountId(), input.getAmount());
            return new AccountWithdrawResponse(input.getAccountId());
        } catch (EventCollisionException e) {
            String message = buildErrorMessage(409, "Conflict", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        } catch (TransactionFailedException e) {
            String message = buildErrorMessage(500, "InternalServerError", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        } catch (HydrationException e) {
            String message = buildErrorMessage(500, "InternalServerError", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        } catch (AggregateNotFoundException e) {
            String message = buildErrorMessage(404, "NotFound", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        }
    }
}
