AWS / CQRS with Event Sourcing
=========================

CQRS pattern implemented using AWS technologies. The example code includes both a set of base classes as well as a sample domain model, commands, events, and set of Lambdas.

This implementation is based off of Greg Young's Simple CQRS C# example
located at https://github.com/gregoryyoung/m-r

## AWS Technologies
- AWS Java SDK v2
- DynamoDB
- Lambda

## TODO
- Add handler to subscribe to DDB stream to update read models.
- Complete API
- Unit tests
