
.DEFAULT_GOAL := all

.PHONY: all check format format-check lint typecheck \
        test unit-test integration-test full-test clean

# Default: run fast checks
all: check

# -------------------------------
# Code Quality
# -------------------------------

format:
	bun run format

format-check:
	bun run format:check

lint:
	bun run lint

typecheck:
	bun run typecheck

check: format-check lint typecheck

# -------------------------------
# Testing
# -------------------------------

# Run JavaScript/TypeScript unit tests
unit-test:
	bun run test:ci

# Run Android native integration tests
# Requires a generated Android example project
# and an attached Android device/emulator.
integration-test:
	@if [ ! -f example/android/gradlew ]; then \
		echo "Android example project not generated."; \
		echo "Set up the Expo Android example before running integration tests."; \
		exit 1; \
	fi
	cd example/android && bash ./gradlew :offline-auth-react-native:connectedDebugAndroidTest --console=plain

# Default test command
test: unit-test

# Run all checks and all test suites
full-test: check unit-test integration-test

# -------------------------------
# Maintenance
# -------------------------------

clean:
	rm -rf build dist
