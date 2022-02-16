AWS / CQRS with Event Sourcing
=========================

CQRS pattern implemented using AWS technologies. The example code includes both a set of base classes as well as a sample domain model, commands, events, and set of Lambdas.

This implementation is based off of Greg Young's Simple CQRS C# example
located at https://github.com/gregoryyoung/m-r

## AWS Technologies
- AWS Java SDK v2
- DynamoDB
- SQS
- Lambda

## TODO
- Add condition checks to ensure uniqueness when persisting to event store.
- Add exception handling when querying/persisting to DynamoDB.
- Add handlers to update read tables based on SQS messages.
