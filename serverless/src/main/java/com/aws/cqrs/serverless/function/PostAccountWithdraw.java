package com.aws.cqrs.serverless.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.aws.cqrs.core.exceptions.EventCollisionException;
import com.aws.cqrs.core.exceptions.HydrationException;
import com.aws.cqrs.sample.services.AccountService;
import com.aws.cqrs.serverless.command.AccountWithdraw;
import com.aws.cqrs.serverless.response.AccountWithdrawResponse;

public class PostAccountWithdraw extends RequestHandlerBase<AccountWithdraw, AccountWithdrawResponse> {

    @Override
    public AccountWithdrawResponse handleRequest(AccountWithdraw input, Context context) {

        AccountService accountService = new AccountService();

        try {
            accountService.withdrawal(input.getAccountId(), input.getAmount());
            return new AccountWithdrawResponse(input.getAccountId());
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