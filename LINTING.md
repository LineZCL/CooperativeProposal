# Linting Configuration for CooperativeProposals

This project now includes comprehensive code quality linting tools to help maintain consistent code standards.

## Available Linting Tools

### 1. Checkstyle
- **Purpose**: Enforces coding standards and style guidelines
- **Configuration**: `config/checkstyle/checkstyle.xml`
- **Reports**: HTML and XML format in `build/reports/checkstyle/`

### 2. SpotBugs
- **Purpose**: Static analysis tool that finds potential bugs
- **Reports**: HTML and XML format in `build/reports/spotbugs/`

## Available Gradle Tasks

### Run All Linting
```bash
./gradlew lint
```
Runs both Checkstyle and SpotBugs on main and test sources.

### Run Individual Tools
```bash
./gradlew lintCheckstyle    # Checkstyle only
./gradlew lintSpotbugs      # SpotBugs only
./gradlew lintMain          # Both tools on main source only
```

### Run Individual Components
```bash
./gradlew checkstyleMain    # Checkstyle on main sources
./gradlew checkstyleTest    # Checkstyle on test sources
./gradlew spotbugsMain      # SpotBugs on main sources
./gradlew spotbugsTest      # SpotBugs on test sources
```

## Configuration

### Current Settings
- **ignoreFailures**: `true` (warnings won't fail the build)
- **maxWarnings**: 200 for Checkstyle
- **maxErrors**: 50 for Checkstyle
- **SpotBugs effort**: `default`
- **SpotBugs reportLevel**: `high`

### Common Issues Found

#### Checkstyle Issues:
- Unused imports
- Missing final parameters
- Incorrect whitespace formatting
- Magic numbers in code
- Method naming conventions (test methods)
- Design for extension warnings

#### SpotBugs Issues:
- Potential null pointer exceptions
- Security vulnerabilities
- Performance issues
- Bad practices

## Report Locations

After running linting tasks, reports are generated in:
- **Checkstyle**: `build/reports/checkstyle/main.html` and `build/reports/checkstyle/test.html`
- **SpotBugs**: `build/reports/spotbugs/main.html` and `build/reports/spotbugs/test.html`

## Integration with CI/CD

To make linting stricter for CI/CD, modify `build.gradle`:
```gradle
checkstyle {
    ignoreFailures = false  // Fail build on violations
    maxWarnings = 0        // No warnings allowed
    maxErrors = 0          // No errors allowed
}

spotbugs {
    ignoreFailures = false  // Fail build on violations
    reportLevel = 'medium'  // More strict reporting
}
```

## Quick Fixes

### Fix Unused Imports
Run your IDE's "Optimize Imports" feature or remove unused import statements.

### Fix Final Parameters
Add `final` keyword to method parameters that are not modified.

### Fix Whitespace Issues
Use your IDE's auto-formatter or manually adjust spacing around operators and braces.

### Fix Magic Numbers
Extract magic numbers into named constants:
```java
private static final int DEFAULT_TIMEOUT = 60;
private static final long MILLISECONDS_MULTIPLIER = 1000L;
```

## Customization

To customize Checkstyle rules, edit `config/checkstyle/checkstyle.xml`. 
To suppress specific SpotBugs warnings, use annotations like `@SuppressFBWarnings`.
