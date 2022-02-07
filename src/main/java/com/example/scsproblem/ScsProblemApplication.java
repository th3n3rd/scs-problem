package com.example.scsproblem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

@SpringBootApplication
public class ScsProblemApplication {

	private final StreamBridge streamBridge;

	public ScsProblemApplication(StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
	}

	@Bean
	MessageRoutingCallback router() {
		return new MessageRoutingCallback() {
			@Override
			public FunctionRoutingResult routingResult(Message<?> message) {
				var type = (String) message.getHeaders().get("type");
				if ("A".equals(type)) {
					return new FunctionRoutingResult("incoming");
				}
				return new FunctionRoutingResult("discarded");
			}
		};
	}

	@Bean
	Consumer<Object> discarded() {
		return message -> {};
	}

	@Bean
	Consumer<A> incoming() {
		return message -> outgoing();
	}

	void outgoing() {
		streamBridge.send(
			"outgoing-out-0",
			MessageBuilder
				.withPayload(new B())
				.setHeader("type", "B")
				.build()
		);
	}

	public static void main(String[] args) {
		SpringApplication.run(ScsProblemApplication.class, args);
	}
}
