# Contributing to Liberty Assistant Hub

Thank you for your interest in contributing! This project welcomes contributions of all kinds.

## Getting Started

1. Fork the repository and clone your fork.
2. Follow the setup instructions in the [README](README.md).
3. Create a feature branch: `git checkout -b feat/your-feature-name`

## Development Guidelines

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use Kotlin DSL for all Gradle configuration.
- Keep composables small and focused — extract reusable UI into `component/`.
- ViewModels should expose `StateFlow` and never hold Android framework references.

### Architecture Rules
- **Domain layer** must not import from `data` or `presentation` packages.
- **Data layer** maps entities ↔ domain models via private extension functions.
- All business logic lives in use cases, not ViewModels.
- Dependency injection is handled exclusively via Hilt modules in `di/`.

### AI / Model Integration
- All inference must remain fully offline — no network calls in `InferenceEngine`.
- `PromptBuilder` is the single source of truth for system prompts per mode.
- New `AssistantMode` entries require a corresponding prompt builder function.

## Submitting Changes

1. Ensure your changes compile: `./gradlew assembleDebug`
2. Run lint: `./gradlew lint`
3. Run unit tests: `./gradlew test`
4. Open a pull request against the `main` branch with a clear description.

## Reporting Issues

Open a GitHub Issue with:
- Android version and device model
- Steps to reproduce
- Expected vs. actual behaviour
- Logcat output if applicable

## License

By contributing, you agree your contributions will be licensed under the [MIT License](LICENSE).
