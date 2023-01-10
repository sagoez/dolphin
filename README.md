# Dolphin <a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" />

[![Continuous Integration](https://github.com/lapsusHQ/dolphin/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/lapsusHQ/dolphin/actions/workflows/ci.yml)
[![Clean](https://github.com/lapsusHQ/dolphin/actions/workflows/clean.yml/badge.svg)](https://github.com/lapsusHQ/dolphin/actions/workflows/clean.yml)
![GitHub issues](https://img.shields.io/github/issues/lapsusHQ/dolphin)

EventStoreDB is an open-source state-transition database, designed for businesses that are ready to harness the true
power of event-driven architecture. It is a purpose-built database for event-driven applications, with a focus on high
performance, scalability, and reliability.

This project is a little playground for me to learn more about EventStoreDB and how to use it in a Scala application,
using the [EventStoreDB Java for Scala](https://github.com/EventStore/EventStoreDB-Client-Java) client.

## Roadmap

- [x] Add a docker-compose file to run EventStoreDB
- [x] Figure out what to do with some result data types like Position, ExpectedVersion
- [x] [Write a simple application that uses this wrapper](https://github.com/samgj18/event-sourcing-poc/)
- [x] Keep the wrapper up to date with the latest version of the Java client
- [x] Revisit if we should log the errors or not simply let the user handle logging.
- [ ] Write tests for Client, Session, StoreSession and Trace
- [ ] Write documentation on how to use the wrapper
- [ ] Write all the missing data types and methods in the wrapper
- [ ] Revisit design decisions and refactor if needed as keeping the session open for the whole application lifetime is
  not ideal since it seems to starve the cpu.
- [ ] Resolve authentication handling
- [ ] Abstract common settings like connection string, credentials and some methods into a single module