# Event Store DB

EventStoreDB is an open-source state-transition database, designed for businesses that are ready to harness the true power of event-driven architecture. It is a purpose-built database for event-driven applications, with a focus on high performance, scalability, and reliability.

This project is a little playground for me to learn more about EventStoreDB and how to use it in a Scala application, using the [EventStoreDB Java for Scala](https://github.com/EventStore/EventStoreDB-Client-Java) client.

## Roadmap

- [x] Add a docker-compose file to run EventStoreDB
- [ ] Write tests for the application
- [ ] Write a simple application that uses this wrapper
- [ ] Write documentation on how to use the wrapper
- [ ] Keep the wrapper up to date with the latest version of the Java client
- [ ] Write all the missing data types and methods in the wrapper (e.g. projections, subscriptions, etc.) and figure out what to do with some result data types like Position, ExpectedVersion, etc.
- [ ] Revisit design decisions and refactor if needed as keeping the session open for the whole application lifetime is not ideal since it starves the cpu.