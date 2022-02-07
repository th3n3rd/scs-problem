package com.example.scsproblem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
abstract class ScsProblemTests {

	private final static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private InputDestination input;

	@Autowired
	private OutputDestination output;

	@ParameterizedTest
	@MethodSource("bindings")
	void consumeFromAndProduceIntoSharedTopic(String incomingBinding, String outgoingBinding) {
		givenNoOutgoingMessages();
		whenAnIncomingMessageArrives(incomingBinding);
		thenEventuallyAnOutgoingMessageIsProduced(outgoingBinding);
	}

	public static Stream<Arguments> bindings() {
		return Stream.of(
			Arguments.of(null, null),
			Arguments.of(null, "outgoing-out-0"),
			Arguments.of(null, "all-messages"),
			Arguments.of("incoming-in-0", null),
			Arguments.of("incoming-in-0", "outgoing-out-0"),
			Arguments.of("incoming-in-0", "all-messages"),
			Arguments.of("all-messages", null),
			Arguments.of("all-messages", "outgoing-out-0"),
			Arguments.of("all-messages", "all-messages")
		);
	}

	void givenNoOutgoingMessages() {
		output.clear();
	}

	void whenAnIncomingMessageArrives(String binding) {
		var message = MessageBuilder
			.withPayload(new A())
			.setHeader("type", "A")
			.build();
		if (Objects.isNull(binding)) {
			input.send(message);
		} else {
			input.send(message, binding);
		}
	}

	private void thenEventuallyAnOutgoingMessageIsProduced(String binding) {
		var timeout = 1000;
		var message = Objects.isNull(binding)
			? output.receive(timeout)
			: output.receive(timeout, binding);

		assertThat(message)
			.withFailMessage("Should receive a message, but none arrived")
			.isNotNull();

		assertThat(message.getHeaders().containsKey("type"))
			.withFailMessage("Message should have a 'type' header")
			.isTrue();

		assertThat(message.getHeaders().get("type"))
			.withFailMessage("Message type should be B")
			.isEqualTo("B");

		assertDoesNotThrow(() -> {
			mapper.readValue(message.getPayload(), B.class);
		}, "Message payload should successfully be deserialized, but failed");
	}
}
