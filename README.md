# EMail

## Overview

The EMail project provides email sending capabilities within the Water framework. It allows managing email templates and sending emails using those templates. The project is a module in a larger system, likely used in enterprise applications where standardized email communication is crucial.

The EMail module simplifies the process of sending emails by providing a template-based approach. Instead of hardcoding email content in the application, users can create and manage email templates with placeholders for dynamic data. This makes it easier to update email content without modifying the application code.

The primary goal of the EMail project is to provide a reusable and configurable email sending service that can be easily integrated into other Water framework-based applications. It abstracts away the complexities of email sending, such as SMTP configuration and MIME message creation, allowing developers to focus on the application logic.

## Technology Stack

*   **Language:** Java
*   **Build Tool:** Gradle
*   **Logging:** SLF4J
*   **Code Generation:** Lombok
*   **Class Indexing:** Atteo Class Index
*   **Persistence:** Jakarta Persistence API, Hibernate
*   **Templating:** FreeMarker
*   **Email:** Jakarta Mail
*   **Cryptography:** Bouncy Castle
*   **Security:** Nimbus JOSE+JWT
*   **Testing:** JUnit Jupiter, Mockito, HSQLDB
*   **Core Framework:** Water Core (`Core-api`, `Core-model`, `Core-interceptors`, `Core-permission`, `Core-bundle`, `Core-registry`, `Core-security`, `Core-service`, `Core-validation`, `Core-testing-utils`)
*   **Code Coverage:** Jacoco
*    **Code Quality:** Sonarqube

## Directory Structure

```
EMail/
├── EMail-api/               # Defines the public API for the EMail module
│   ├── src/main/java/it/water/email/api/
│   │   ├── EMailOptions.java           # Interface for email configuration options
│   │   ├── EMailTemplateRepository.java  # Repository interface for EMailTemplate entities
│   │   ├── EMailTemplateSystemApi.java   # System-level API for EMailTemplate management
│   └── build.gradle           # Gradle build file for EMail-api module
├── EMail-model/             # Defines the data model for the EMail module
│   ├── src/main/java/it/water/email/model/
│   │   ├── EMailConstants.java       # Constants related to email configuration options
│   │   ├── EMailTemplate.java        # Represents an email template entity
│   └── build.gradle           # Gradle build file for EMail-model module
├── EMail-service/           # Implements the core logic of the EMail module
│   ├── src/main/java/it/water/email/service/
│   │   ├── EMailOptionsImpl.java      # Implementation of EMailOptions interface
│   │   ├── EMailSystemServiceImpl.java # Implementation of EMailTemplateSystemApi interface
│   │   ├── EMailRepositoryImpl.java # Implementation of EMailTemplateRepository interface
│   ├── src/main/resources/
│   │   ├── it.water.application.properties  # Default email configuration properties
│   │   ├── META-INF/persistence.xml    # Persistence unit configuration
│   ├── src/test/java/it/water/email/
│   │   ├── EMailApiTest.java         # Integration tests for the EMail module
│   ├── src/test/resources/
│   │   ├── it.water.application.properties # Application properties for tests
│   └── build.gradle           # Gradle build file for EMail-service module
├── build.gradle             # Root Gradle build file
├── gradle.properties        # Gradle properties
├── settings.gradle          # Gradle settings file
└── README.md                # Project documentation
```

## Getting Started

1.  **Prerequisites:**
    *   Java Development Kit (JDK) version 11 or higher
    *   Gradle version 7.0 or higher
    *   An active internet connection to download dependencies

2.  **Cloning the Repository:**

    Clone the repository using the following command:

    ```bash
    git clone https://github.com/Water-Framework/EMail.git
    ```

3.  **General Build Steps:**

    *   Navigate to the root directory of the project.
    *   Compile the code: `./gradlew compileJava`
    *   Run the tests: `./gradlew test`
    *   Build the project: `./gradlew build`
    *   Publish the project to a local Maven repository: `./gradlew publishToMavenLocal`

4.  **Required Configuration or Environment Variables:**

    The EMail module relies on several configuration properties defined in `EMail-service/src/main/resources/it.water.application.properties`. These properties can be overridden using environment variables. The following properties are available:

    *   `it.water.mail.sender.name`: The name of the email sender (default: Info).
    *   `it.water.mail.smtp.host`: The SMTP server hostname (default: localhost).
    *   `it.water.mail.smtp.port`: The SMTP server port (default: 587).
    *   `it.water.mail.smtp.username`: The SMTP username (default: user).
    *   `it.water.mail.smtp.password`: The SMTP password (default: pwd).
    *   `it.water.mail.smtp.auth.enabled`: Whether SMTP authentication is enabled (default: false).
    *   `it.water.mail.smtp.start-ttls.enabled`: Whether STARTTLS is enabled (default: false).

    To override these properties, set the corresponding environment variables. For example:

    ```bash
    export WATER_EMAIL_SMTP_HOST=smtp.example.com
    export WATER_EMAIL_SMTP_USERNAME=myuser
    export WATER_EMAIL_SMTP_PASSWORD=mypassword
    ```

5.  **Module Usage:**

    *   **EMail-api:** This module defines the public API for interacting with the EMail module. To use it in an external project, add it as a dependency in your `build.gradle` file:

        ```gradle
        dependencies {
            implementation 'it.water.email:EMail-api:3.0.0' // Replace 3.0.0 with the actual version
        }
        ```

        This module provides interfaces like `EMailTemplateSystemApi` to manage email templates and `EMailOptions` to configure email settings.

    *   **EMail-model:** This module defines the data model for the EMail module. It contains the `EMailTemplate` class, which represents an email template. To use it, add it as a dependency:

        ```gradle
        dependencies {
            implementation 'it.water.email:EMail-model:3.0.0' // Replace 3.0.0 with the actual version
        }
        ```

        The `EMailTemplate` class can be used to create and manage email templates in your application.

    *   **EMail-service:** This module implements the core logic of the EMail module. To use it, add it as a dependency:

        ```gradle
        dependencies {
            implementation 'it.water.email:EMail-service:3.0.0' // Replace 3.0.0 with the actual version
        }
        ```

        This module provides the `EMailSystemServiceImpl` class, which implements the `EMailTemplateSystemApi` interface and provides the core email sending functionality.  It relies on the presence of the Water framework's core modules in the runtime environment.  Configuration is primarily driven through the `it.water.application.properties` file and environment variable overrides, as described above.

        **Example Usage:**

        To send an email using the `EMail-service` module, you would typically inject an instance of the `EMailTemplateSystemApi` into your application component. Assuming your application is running within the Water framework (or a compatible OSGi environment), you can obtain a reference to the `EMailTemplateSystemApi` service through the OSGi service registry or using dependency injection mechanisms provided by the framework.

        ```java
        import it.water.email.api.EMailTemplateSystemApi;
        import java.util.HashMap;
        import java.util.List;

        public class MyEmailSender {

            private EMailTemplateSystemApi emailTemplateSystemApi;

            // Assuming emailTemplateSystemApi is injected by the container
            public MyEmailSender(EMailTemplateSystemApi emailTemplateSystemApi) {
                this.emailTemplateSystemApi = emailTemplateSystemApi;
            }

            public void sendWelcomeEmail(String recipientEmail, String userName) {
                String templateName = "welcome_email";
                HashMap<String, Object> data = new HashMap<>();
                data.put("userName", userName);

                try {
                    emailTemplateSystemApi.sendMail(templateName, data, "Welcome!", List.of(recipientEmail), null, null, "text/html", null);
                } catch (Exception e) {
                    // Handle exception appropriately
                    e.printStackTrace();
                }
            }
        }
        ```

        In this example, the `sendWelcomeEmail` method retrieves the email template named "welcome_email", populates it with the user's name, and sends the email to the specified recipient.  The `emailTemplateSystemApi` instance is assumed to be provided by the Water framework's dependency injection mechanism.  Ensure that the "welcome_email" template exists in the database and that the email configuration properties are correctly set.

## Functional Analysis

### 1. Main Responsibilities of the System

The primary responsibility of the EMail system is to provide a robust and flexible email sending service within the Water framework. It manages email templates, allowing for dynamic content generation, and handles the complexities of sending emails, including SMTP configuration and message creation. It provides a standardized way to send emails from different parts of an application. The system provides the foundational services for managing and dispatching template-based emails.

### 2. Problems the System Solves

The EMail system addresses several key challenges:

*   **Standardization of Email Communication:** It ensures a consistent look and feel for emails sent from different parts of the application by using predefined templates.
*   **Dynamic Content Generation:** It allows for dynamic content to be inserted into emails, such as user names, order details, or other personalized information.
*   **Abstraction of Email Sending Details:** It hides the complexities of SMTP configuration and MIME message creation from the application developer.
*   **Easy Maintenance:** Email templates can be updated without modifying the application code, making it easier to maintain and update email content.
*   **Centralized Configuration:** Email settings are managed in a central location, making it easier to configure and manage email sending across the entire application.

### 3. Interaction of Modules and Components

The EMail system consists of three main modules: `EMail-api`, `EMail-model`, and `EMail-service`.

*   The `EMail-api` module defines the public API for interacting with the EMail module. It provides interfaces for managing email templates and configuring email settings.
*   The `EMail-model` module defines the data model for the EMail module. It contains the `EMailTemplate` class, which represents an email template.
*   The `EMail-service` module implements the core logic of the EMail module. It uses the `EMailOptions` interface to retrieve email configuration parameters, the `EMailTemplateRepository` interface to access and manage `EMailTemplate` entities in the database, FreeMarker to process email templates, and Jakarta Mail to send emails.

The `EMailSystemServiceImpl` class orchestrates the interactions between these components. It retrieves email configuration from `EMailOptionsImpl`, accesses and manages `EMailTemplate` entities using `EMailRepositoryImpl`, processes email templates using FreeMarker, and sends emails using Jakarta Mail.

### 4. User-Facing vs. System-Facing Functionalities

The EMail system provides both user-facing and system-facing functionalities.

*   **User-Facing Functionalities:** These functionalities are typically exposed through a UI or API that allows users to create, manage, and send email templates. This includes functionalities for creating new templates, editing existing templates, and testing email sending.  End users might interact with a UI that leverages the EMail module's API to manage and send emails.
*   **System-Facing Functionalities:** These functionalities are used internally by other system components to send emails. This includes the `EMailTemplateSystemApi` interface, which provides a system-level API for managing email templates, and the `EMailSystemServiceImpl` class, which implements the core email sending logic.  Other modules within the Water framework would use these APIs to send emails as part of their workflows.

The `BaseEntitySystemApi` interface, extended by `EMailTemplateSystemApi`, ensures consistent CRUD operations and behaviors for entities within the Water framework. This common interface systematically applies annotations, decorators, or behaviors across all implementing classes, ensuring consistent and shared functionality.

## Architectural Patterns and Design Principles Applied

*   **Modular Design:** The project is divided into well-defined modules (`EMail-api`, `EMail-model`, `EMail-service`), promoting separation of concerns and reusability.
*   **Dependency Injection:** The `EMailSystemServiceImpl` class uses dependency injection to obtain instances of `EMailRepository` and `EMailOptions`, making the code more testable and flexible.
*   **Repository Pattern:** The `EMailTemplateRepository` interface and its implementation (`EMailRepositoryImpl`) encapsulate the data access logic, providing an abstraction layer between the service layer and the database.
*   **Template Pattern:** The use of FreeMarker for generating email bodies follows the Template Pattern, allowing for dynamic content generation based on templates and data.
*   **Configuration via Properties:** Email configuration is managed through application properties (`it.water.application.properties`), allowing for easy customization and environment-specific settings.
*   **JTA DataSource:** The usage of JTA (Java Transaction API) suggests that the application server manages the transactions.
*   **Layered Architecture:** The project follows a layered architecture, with distinct layers for API definition, data model, service implementation, and data access. This promotes maintainability and scalability.
*   **Interceptor Pattern:** While not explicitly visible, the presence of `Core-interceptors` as a dependency suggests that interceptors are used within the Water framework, possibly for logging, security, or other cross-cutting concerns.
*   **Role-Based Access Control (RBAC):** The `EMailTemplate` class defines default roles (`DEFAULT_MANAGER_ROLE`, `DEFAULT_VIEWER_ROLE`, `DEFAULT_EDITOR_ROLE`), indicating that RBAC is used to control access to email templates.

## Code Quality Analysis

The SonarQube analysis indicates a high level of code quality:

-   **Bugs:** 0 - No bugs detected.
-   **Vulnerabilities:** 0 - No vulnerabilities detected.
-   **Code Smells:** 0 - No code smells detected.
-   **Code Coverage:** 80.6% - Good code coverage.
-   **Duplication:** 0.0% - No duplicated code.

These metrics indicate that the project is well-maintained, reliable, and secure. The high code coverage suggests that the code is thoroughly tested.

## Weaknesses and Areas for Improvement

The following items are based on the software analyst's findings and the SonarQube analysis, reframed as concrete TODO items for future releases or roadmap planning:

*   [ ] **Manual Code Reviews:** Even with a clean bill of health from SonarQube, perform manual code reviews to identify potential improvements in code clarity, maintainability, and performance that may not be caught by automated analysis.
*   [ ] **SonarQube Alerts:** Set up alerts and automated checks in SonarQube to maintain code quality as the project evolves and new features are added.
*   [ ] **Mutation Testing:** Consider using mutation testing tools to assess the effectiveness of the existing test suite and identify areas where tests may be weak or insufficient.
*   [ ] **Investigate Branch Coverage:** Investigate branch coverage to have a more precise view of the test coverage. Add specific tests to cover uncovered branches.
*   [ ] **Environment Variable Handling and Validation:** Ensure environment variables used in `it.water.application.properties` are properly handled and documented to avoid configuration issues in different environments. Consider validating these variables at application startup. Add documentation on how to properly configure these environment variables.
*   [ ] **Email Sending Error Handling and Logging:** Since the project uses Jakarta Mail for sending emails, ensure proper error handling and logging are implemented around email sending operations to quickly identify and address potential issues. Include retry mechanisms for transient errors.
*   [ ] **Dependency Updates and Monitoring:** Regularly update dependencies to the latest stable versions to benefit from security patches and bug fixes. Monitor dependency vulnerability reports to proactively address potential security risks.
*   [ ] **Documentation of Code Generation:** Provide more documentation on the code generation process indicated by the `@Generated by Water Generator` annotation. Explain how the code is generated, what templates are used, and how developers can customize the generated code.
*   [ ] **Clarify Water Framework Integration:** Provide more details on how the EMail module integrates with the broader Water framework. Explain how the Water framework's dependency injection, security, and other features are used within the EMail module.
*   [ ] **Enhance RBAC Documentation:** Expand the documentation on Role-Based Access Control (RBAC) within the EMail module. Explain how the `DEFAULT_MANAGER_ROLE`, `DEFAULT_VIEWER_ROLE`, and `DEFAULT_EDITOR_ROLE` are used to control access to email templates. Provide examples of how to customize these roles.

## Further Areas of Investigation

*   **Performance Bottlenecks:** Investigate potential performance bottlenecks in the email sending process, especially when sending large volumes of emails. Consider using asynchronous email sending or caching email templates to improve performance.
*   **Scalability Considerations:** Evaluate the scalability of the EMail module, especially in high-volume environments. Consider using a distributed message queue to handle email sending requests.
*   **Integrations with External Systems:** Explore potential integrations with external systems, such as CRM or marketing automation platforms.
*   **Advanced Features:** Research and implement advanced features, such as email tracking, A/B testing, and personalized email content.
*   **Codebase Refactoring:** Analyze `EMailSystemServiceImpl` for potential refactoring opportunities to improve code clarity and maintainability, especially in methods like `sendMail` and `createMimeMessage`.
*   **Security Enhancements:** Explore advanced security features such as DKIM and SPF to improve email deliverability and prevent spoofing.

## Attribution

Generated with the support of ArchAI, an automated documentation system.
