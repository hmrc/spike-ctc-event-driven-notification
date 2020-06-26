
# spike-ctc-event-driven-notification

Using Mongo for an simple event sourcing system.

## Development setup

- create the capped collections and set the max document count:
  ```
  db.createCollection("events", { capped: true, size: 64, max: 10 });
  db.createCollection("eventsWorkLog", { capped: true, size: 64, max: 10 });
  ``` 

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
 
