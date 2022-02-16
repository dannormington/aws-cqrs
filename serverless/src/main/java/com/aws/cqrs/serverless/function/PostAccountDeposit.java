package com.aws.cqrs.serverless.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.aws.cqrs.core.exceptions.EventCollisionException;
import com.aws.cqrs.core.exceptions.HydrationException;
import com.aws.cqrs.sample.services.AccountService;
import com.aws.cqrs.serverless.command.AccountDeposit;
import com.aws.cqrs.serverless.response.AccountDepositResponse;

public class PostAccountDeposit extends RequestHandlerBase<AccountDeposit, AccountDepositResponse> {

    @Override
    public AccountDepositResponse handleRequest(AccountDeposit input, Context context) {

        AccountService accountService = new AccountService();

        try {
            accountService.deposit(input.getAccountId(), input.getAmount());
            return new AccountDepositResponse(input.getAccountId());
        } catch (EventCollisionException e) {
            String message = buildErrorMessage(409, "Conflict", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        } catch (HydrationException e) {
            String message = buildErrorMessage(404, "NotFound", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        } catch (Exception e) {
            String message = buildErrorMessage(400, "BadRequest", e.getMessage(), context.getAwsRequestId());
            throw new RuntimeException(message);
        }
    }
}
