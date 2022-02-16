AWS / CQRS with Event Sourcing
=========================

CQRS pattern implemented using AWS technologies. The example code includes both a base set of classes as well as a sample domain model, commands, events, and set of Lambdas.

This implementation is based off of Greg Young's Simple CQRS C# example
located at https://github.com/gregoryyoung/m-r

## AWS Technologies
- AWS Java SDK v2
- DynamoDB
- SQS
- Lambda

## TODO
- Add handlers to update read tables based on SQS messages.
