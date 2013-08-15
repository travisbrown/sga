Yet another job queue
=====================

This is a simple job queue that behaves more or less the same
as Jim Smith's Node.js implementation in the directory next door.

This one is written in [Scala](http://www.scala-lang.org/)
and uses [Akka](http://akka.io/) to manage concurrency.

Running the demo
----------------

To run the echo server demo, first start [Redis](http://redis.io/),
and then start the server:

``` bash
./sbt "project yajq-core" "run-main edu.umd.mith.yajq.DemoServer"
```

Next run the demo client application, which by default will create
a thousand clients and start a hundred jobs from each:

``` bash
./sbt "project yajq-core" "run-main edu.umd.mith.yajq.DemoClient"
```

After a minute or so you'll see a message like this:

```
Processed 100000 jobs from 1000 clients in 41145 ms!
```

If you run the demo client against the Node.js server implementation,
you'll see something like this:

```
Processed 100000 jobs from 1000 clients in 146605 ms!
```

In a handful of unscientific benchmarks like this, Akka is consistently
two-and-a-half to three times faster than Node.js on my quad-core processor.

Warning!
--------

The code is uncommented, untested, and there's no error handling to speak of.
Consider it a quick proof-of-concept only.

