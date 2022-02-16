package com.aws.cqrs.serverless.function;

import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.aws.cqrs.core.exceptions.EventCollisionException;
import com.aws.cqrs.core.exceptions.HydrationException;
import com.aws.cqrs.sample.services.AccountService;
import com.aws.cqrs.serverless.command.CreateAccount;
import com.aws.cqrs.serverless.response.CreateAccountResponse;

public class PostCreateAccount extends RequestHandlerBase<CreateAccount, CreateAccountResponse> {

	@Override
	public CreateAccountResponse handleRequest(CreateAccount input, Context context) {

		AccountService accountService = new AccountService();

		try {
			UUID accountId = accountService.create(input.getFirstName(), input.getLastName());
			return new CreateAccountResponse(accountId);
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
