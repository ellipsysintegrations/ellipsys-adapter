package hu.ellipsysintegrations.adapter.allure.server.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RunResult {
    SUCCESS(0),
    FAILURE(1),
    ERROR(2);

    private final int exitCode;
}
