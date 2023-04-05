# Roadmap

Bare in mind that this is a work in progress and the API is not stable yet.
The following is a list of things that need to be done before we can consider this wrapper somewhat usable.

- [x] Add a docker-compose file to run EventStoreDB.
- [x] Figure out what to do with some result data types like Position, ExpectedVersion.
- [x] [Write a simple application that uses this wrapper](https://github.com/samgj18/event-sourcing-poc/)
- [x] Keep the wrapper up to date with the latest version of the Java client.
- [x] Revisit if we should log the errors or not simply let the user handle logging.
- [x] Write tests for Client, Session, StoreSession and Trace.
- [x] Write all the missing data types and methods in the wrapper.
- [x] Resolve authentication handling.
- [x] Write documentation on how to use the wrapper.
- [x] Improve the way we handle the subscription listener.
- [x] Provide Stream[F, Event[...]] instead of a Resource[F, ...] for the subscription.
- [ ] Improve ProjectionManager and write tests for it.
- [ ] Improve documentation, i.e. mini-website.
- [ ] Keeping the session open for the whole application lifetime is not ideal since it seems to starve the cpu.
- [ ] Check how to test and improve performance if needed.
- [ ] Revisit design decisions and refactor if needed.
- [ ] Try to make queries more type safe and programmatic.
