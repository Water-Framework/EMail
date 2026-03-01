# EMail Module — Email Notification & Template Management

## Purpose
Provides email sending capabilities (SMTP via Jakarta Mail) and a database-backed template management system using FreeMarker. Supports both template-based emails (parameters substituted at send time) and direct HTML content emails. Also manages email template CRUD operations as a fully permission-protected entity.

## Sub-modules

| Sub-module | Runtime | Key Classes |
|---|---|---|
| `EMail-api` | All | `EmailNotificationService`, `EmailContentBuilder`, `EMailTemplateApi`, `EMailTemplateSystemApi`, `EMailTemplateRestApi`, `EMailTemplateRepository` |
| `EMail-model` | All | `EMailTemplate` entity |
| `EMail-service` | Water/OSGi | `EmailNotificationServiceImpl` (SMTP + FreeMarker), service impl, repository, REST controller |
| `EMail-service-spring` | Spring Boot | Spring MVC REST controllers, Spring Boot app config |

## EMailTemplate Entity

```java
@Entity
@Table(name = "email_template")
@AccessControl(
    availableActions = {CrudActions.class},
    rolesPermissions = {
        @DefaultRoleAccess(roleName = "emailTemplateManager", actions = {CrudActions.class}),
        @DefaultRoleAccess(roleName = "emailTemplateEditor",  actions = {CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL}),
        @DefaultRoleAccess(roleName = "emailTemplateViewer",  actions = {CrudActions.FIND, CrudActions.FIND_ALL})
    }
)
public class EMailTemplate extends AbstractJpaEntity implements ProtectedEntity {
    @NotNull @Column(unique = true) @NoMalitiusCode
    private String name;             // template identifier (e.g., "user-activation")

    @NoMalitiusCode
    private String description;

    @Lob @NotNull
    private String content;          // FreeMarker template body (HTML or plain text)

    @NoMalitiusCode
    private String subject;          // email subject (may contain FreeMarker expressions)
}
```

## EmailNotificationService — Core Sending API

```java
public interface EmailNotificationService {

    // Send via named template (FreeMarker substitution)
    void sendEmail(String templateName, Map<String, Object> params,
                   String from, List<String> to, List<String> cc, List<String> bcc,
                   List<File> attachments);

    // Send raw HTML content
    void sendEmail(String subject, String htmlContent,
                   String from, List<String> to, List<String> cc, List<String> bcc,
                   List<File> attachments);

    // Convenience: single recipient, no attachments
    void sendEmail(String templateName, Map<String, Object> params, String from, String to);
}
```

## EmailContentBuilder

Helper for building email content from templates without sending:

```java
public interface EmailContentBuilder {
    String buildContent(String templateName, Map<String, Object> params);
    String buildContent(EMailTemplate template, Map<String, Object> params);
}
```

## FreeMarker Integration

Templates are stored in the `EMailTemplate` entity as FreeMarker markup:

```html
<!-- Template name: "user-activation" -->
<html>
<body>
  <h1>Welcome, ${user.name}!</h1>
  <p>Your activation code is: <strong>${activationCode}</strong></p>
  <p>Click <a href="${activationUrl}">here</a> to activate your account.</p>
</body>
</html>
```

Usage:
```java
Map<String, Object> params = new HashMap<>();
params.put("user", waterUser);
params.put("activationCode", user.getActivateCode());
params.put("activationUrl", "https://myapp.com/activate/" + user.getActivateCode());

emailNotificationService.sendEmail("user-activation", params,
    "noreply@myapp.com", user.getEmail());
```

## SMTP Configuration

```properties
# Sender display name
it.water.mail.sender.name=My Application

# SMTP server
it.water.mail.smtp.host=smtp.gmail.com
it.water.mail.smtp.port=587
it.water.mail.smtp.username=myapp@gmail.com
it.water.mail.smtp.password=app-specific-password

# Authentication & TLS
it.water.mail.smtp.auth.enabled=true
it.water.mail.smtp.start-ttls.enabled=true
it.water.mail.smtp.ssl.enabled=false

# Optional: debug
it.water.mail.smtp.debug=false
```

## REST Endpoints (Template Management)

| Method | Path | Permission |
|---|---|---|
| `POST` | `/water/emailtemplates` | emailTemplateManager |
| `PUT` | `/water/emailtemplates` | emailTemplateManager / emailTemplateEditor |
| `GET` | `/water/emailtemplates/{id}` | emailTemplateViewer |
| `GET` | `/water/emailtemplates` | emailTemplateViewer |
| `DELETE` | `/water/emailtemplates/{id}` | emailTemplateManager |

**Note:** The email sending API (`EmailNotificationService`) is a system-level service — it does NOT have REST endpoints. It is called programmatically from other modules (e.g., `User` module calls it for activation emails).

## Default Roles

| Role | Allowed Actions |
|---|---|
| `emailTemplateManager` | Full CRUD on templates |
| `emailTemplateEditor` | UPDATE, FIND, FIND_ALL |
| `emailTemplateViewer` | FIND, FIND_ALL |

## Integration with User Module

The `User` module uses `EmailNotificationService` for:
- Account activation email (template: `user-activation`)
- Password reset email (template: `user-password-reset`)
- Account deletion confirmation (template: `user-deletion-confirm`)

Configure template names via `UserOptions`:
```properties
water.user.activation.email.template=user-activation
water.user.password.reset.email.template=user-password-reset
```

## Dependencies
- `it.water.repository.jpa:JpaRepository-api` — `AbstractJpaEntity`
- `it.water.core:Core-permission` — `@AccessControl`, `CrudActions`
- `it.water.rest:Rest-persistence` — `BaseEntityRestApi`
- `jakarta.mail:jakarta.mail-api` — SMTP sending
- `org.freemarker:freemarker` — template engine

## Testing
- Unit tests: `WaterTestExtension` — mock `EmailNotificationService` (do NOT send real emails in tests)
- Template CRUD: test save/update/find/remove with `WaterTestExtension`
- Template rendering: test `EmailContentBuilder` with a local `Configuration` (no SMTP needed)
- REST tests: **Karate only** — never JUnit direct calls to `EMailTemplateRestController`

## Code Generation Rules
- `EmailNotificationService` is injected as a dependency in other services — never call SMTP directly
- FreeMarker templates support the full FreeMarker language: `<#if>`, `<#list>`, `<#include>`, macros, etc.
- Template `name` is the lookup key — use snake-case convention (e.g., `user-activation`, `invoice-generated`)
- Never hardcode email addresses or template content in service code — use `ApplicationProperties` for configurable values
- `EMailTemplateRestController` tested **exclusively via Karate**
- Attachments: pass `List<File>` for sending; ensure temp files are cleaned up after sending
