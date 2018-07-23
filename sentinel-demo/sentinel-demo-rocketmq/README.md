# Sentinel RocketMQ Demo

This demonstrates some specific scenarios for Apache RocketMQ client.

## Uniform Rate Limiting

In Apache RocketMQ, when message consumers are consuming messages, there may a sudden inflow of messages, whether using pull or push mode. If all the messages were handled at this time, it would be likely to cause the system to be overloaded and then affect stability. However, in fact, there may be no messages coming within a few seconds. If redundant messages are directly discarded, the system's ability to process the message is not fully utilized. We hope that the sudden inflow of messages can be spread over a period of time, so that the system load can be kept on the stable level while processing as many messages as possible, thus achieving the effect of “shaving the peaks and filling the valley”.

![shaving the peaks and filling the valley](https://github.com/alibaba/Sentinel/wiki/image/mq-traffic-peak-clipping-en.png)

Sentinel provides a feature for this kind of scenario: [Rate Limiter](https://github.com/alibaba/Sentinel/wiki/Flow-Shaping:-Pace-Limiter), which can spread a large number of sudden request inflow in a uniform rate manner, let the request pass at a fixed interval. It is often used to process burst requests instead of rejecting them. This avoids traffic spurs causing system overloaded. Moreover, the pending requests will be queued and processed one by one. When the request is estimated to exceed the maximum queuing timeout, it will be rejected immediately.

For example, we configure the rule with uniform rate limiting mode and QPS count is 5, which indicates messages are consumed at fixed interval (200 ms) and pending messages will queue. We also set the maximum queuing timeout is 5s, then all requests estimated to exceed the timeout will be rejected immediately.

![Uniform rate](https://github.com/alibaba/Sentinel/wiki/image/uniform-speed-queue.png)