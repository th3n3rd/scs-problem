package com.example.scsproblem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class ScsProblemTests {

	private final static ObjectMapper mapper = new ObjectMapper();
	private final List<UUID> seenMessages = new ArrayList<>();

	@Autowired
	private InputDestination input;

	@Autowired
	private OutputDestination output;

	@Test
	void consumeFromAndProduceIntoSharedTopic() {
		givenNoOutgoingMessages();
		whenAnIncomingMessageArrives("all-messages");
		thenEventuallyAnOutgoingMessageIsProduced("all-messages");
	}

	void givenNoOutgoingMessages() {
		output.clear();
	}

	void whenAnIncomingMessageArrives(String destination) {
		var message = MessageBuilder
			.withPayload(new A())
			.setHeader("type", "A")
			.build();
		input.send(message, destination);
		seenMessages.add(message.getHeaders().getId());
	}

	private void thenEventuallyAnOutgoingMessageIsProduced(String destination) {
		var message = nextUnseenMessage(destination);

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

	private Message<byte[]> nextUnseenMessage(String destination) {
		var remainingAttempts = 5;
		var timeout = 1000;

		do {
			var message = output.receive(timeout, destination);
			if (Objects.nonNull(message) && !seenMessages.contains(message.getHeaders().getId())) {
				return message;
			}
			remainingAttempts--;
		} while (remainingAttempts > 0);

		return null;
	}
}
