# Spring Cloud Stream Problem

I have been trying to implement the following solution:

[![spring-cloud-stream-shared-topic][1]][1]

My application is expecting to consume message `A` from `all-messages`, do some business logic and then producing 
message `B` back into `all-messages`.

The reason why I am using `StreamBridge` instead of a `Function<A,B>` is because I want the producing side to work with an arbitrary
number of produced messages, but for the sake of this example I am trying to simplify the scenario to just one.

Additionally, there is a custom router function in order to avoid an infinite loop, that dispatch the incoming messages
into the appropriate consumer, either `incoming` or `discarded`, would be great to have a way to effectively discard messages.

That being said I cannot quite get the implementation right using Spring Cloud Stream.

I tried to write a test with different combinations of incoming and outgoing bindings to see what is what.

One set of tests runs with a default profile, where I setup the destination override, while the other set runs with no
overrides, again just for having a control group. Only 2 tests from the `no-overrides` profile pass, the rest fails.

I have shared my code into [this](https://github.com/th3n3rd/scs-problem) repository for convenience.

At this point I am starting to think I misunderstood some concepts behind Spring Cloud Stream, but I really hope
you can provide some useful advise.

Thanks in advance.

[1]: ./spring-cloud-stream-share-topic.png
