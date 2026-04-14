# justfile - convenience recipes for common project tasks

# Default target
default: help

# Show help (just prints available recipes by default)
help:
	@echo "Available recipes:"
	@echo "  just format         - Apply Spotless formatting (spotless:apply)"
	@echo "  just format-check   - Check Spotless formatting (spotless:check)"
	@echo "  just build          - Run full build (verify) including static analysis and tests"
	@echo "  just package        - Build artifact (skip tests)"
	@echo "  just test           - Run tests"
	@echo "  just ci             - Run formatting check and full verify (good for CI locally)"
	@echo "  just tag-release    - Create and push a tag for current project.version (v<version>)"
	@echo "  just format-commit  - Apply formatting and commit changes"
	@echo "  just run-jar        - Run built jar (glob match). Build first if absent"
	@echo "  just clean          - Clean target"
	@echo ""
	@echo "Examples:"
	@echo "  just format"
	@echo "  just ci"

# Apply code formatting (Spotless apply)
format:
	@echo "Running Spotless apply..."
	mvn spotless:apply

# Check code formatting (CI-style)
format-check:
	@echo "Running Spotless check..."
	mvn -B -U spotless:check

# Run full build (static analysis, tests, package)
build:
	@echo "Running mvn verify (static analysis, tests, packaging)"
	mvn -B -U verify

# Build package skipping tests (faster)
package:
	@echo "Packaging (skip tests)"
	mvn -B -U -DskipTests=true package

# Run unit tests only
test:
	@echo "Running tests"
	mvn -B test

# Combined local CI check (format-check + verify)
ci:
	@echo "Running local CI checks: Spotless + verify"
	mvn -B -U spotless:check
	mvn -B -U verify

# Create and push a release tag v<project.version> (requires push permissions)
tag-release:
	@echo "Resolving project version..."
	VERSION=$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version) || exit 1
	TAG=v$$VERSION
	@echo "Resolved version: $$VERSION -> tag $$TAG"
	if git rev-parse "$$TAG" >/dev/null 2>&1; then \
		echo "Tag $$TAG already exists; skipping push"; \
	else \
		git tag -a "$$TAG" -m "Release $$TAG" && git push origin "$$TAG"; \
	fi

# Apply formatting and commit the result
format-commit:
	@echo "Applying formatting and committing changes (if any)"
	mvn spotless:apply
	git add -A
	git commit -m "Apply Spotless formatting (just format)" || echo "No formatting changes to commit"

# Run the built JAR (attempt glob match). Builds are not triggered automatically.
run-jar:
	JAR=$(ls target/*.jar 2>/dev/null | head -n 1 || true)
	if [ -z "$JAR" ]; then \
		echo "No JAR found in target/. Build first with 'just package' or 'just build'"; exit 1; \
	fi
	@echo "Running $$JAR"
	java -jar "$$JAR"

# Clean
clean:
	mvn -q clean

