package com.miyazaki.cooperativeproposals.rabbitmq.message;

import java.util.UUID;

public record SessionMessage(
        UUID votingSessionId
) {
}
