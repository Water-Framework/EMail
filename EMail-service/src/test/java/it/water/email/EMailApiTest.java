package it.water.email;

import it.water.core.api.bundle.ApplicationProperties;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.model.Role;
import it.water.core.api.notification.email.EmailContentBuilder;
import it.water.core.api.notification.email.EmailNotificationService;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.role.RoleManager;
import it.water.core.api.service.Service;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.model.exceptions.ValidationException;
import it.water.core.model.exceptions.WaterRuntimeException;
import it.water.core.testing.utils.api.TestPermissionManager;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.email.api.EMailOptions;
import it.water.email.api.EMailTemplateRepository;
import it.water.email.api.EMailTemplateSystemApi;
import it.water.email.model.EMailTemplate;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * Generated with Water Generator.
 * Test class for EMail Services.
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EMailApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private ApplicationProperties applicationProperties;

    @Inject
    @Setter
    private EMailTemplateSystemApi emailTemplateSystemApi;

    @Inject
    @Setter
    private EmailContentBuilder emailContentBuilder;

    @Inject
    @Setter
    private EmailNotificationService emailNotificationService;

    @Inject
    @Setter
    private EMailOptions options;

    @Inject
    @Setter
    //default permission manager in test environment;
    private TestPermissionManager permissionManager;

    @Inject
    @Setter
    //test role manager
    private RoleManager roleManager;

    @Inject
    @Setter
    //test role manager
    private UserManager userManager;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private EMailTemplateRepository emailRepository;

    @SuppressWarnings("unused")
    private it.water.core.api.model.User adminUser;
    private it.water.core.api.model.User managerUser;
    private it.water.core.api.model.User viewerUser;
    private it.water.core.api.model.User editorUser;
    private Role manager;
    private Role viewer;
    private Role editor;

    @BeforeAll
    void beforeAll() {
        //getting user
        manager = roleManager.getRole("emailTemplateManager");
        viewer = roleManager.getRole("emailTemplateViewer");
        editor = roleManager.getRole("emailTemplateEditor");
        Assertions.assertNotNull(manager);
        Assertions.assertNotNull(viewer);
        Assertions.assertNotNull(editor);
        //impersonate admin so we can test the happy path
        adminUser = userManager.addUser("admin", "name", "lastname", "admin@a.com", "Password1_", "salt", true);
        managerUser = userManager.addUser("manager", "name", "lastname", "manager@a.com", "Password1_", "salt", false);
        viewerUser = userManager.addUser("viewer", "name", "lastname", "viewer@a.com", "Password1_", "salt", false);
        editorUser = userManager.addUser("editor", "name", "lastname", "editor@a.com", "Password1_", "salt", false);
        //starting with admin permissions
        roleManager.addRole(managerUser.getId(), manager);
        roleManager.addRole(viewerUser.getId(), viewer);
        roleManager.addRole(editorUser.getId(), editor);
        //starting with admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    /**
     * Testing basic injection of basic component for email entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
        this.emailTemplateSystemApi = this.componentRegistry.findComponent(EMailTemplateSystemApi.class, null);
        Assertions.assertNotNull(this.emailTemplateSystemApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(EMailTemplateSystemApi.class, null));
        this.emailRepository = this.componentRegistry.findComponent(EMailTemplateRepository.class, null);
        Assertions.assertNotNull(this.emailRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    void saveOk() {
        EMailTemplate entity = createEMail(0);
        entity = this.emailTemplateSystemApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertTrue(entity.getId() > 0);
        Assertions.assertEquals("name0", entity.getTemplateName());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(3)
    void updateShouldWork() {
        Query q = this.emailRepository.getQueryBuilderInstance().createQueryFilter("templateName=name0");
        EMailTemplate entity = this.emailTemplateSystemApi.find(q);
        Assertions.assertNotNull(entity);
        entity.setContent("new Content");
        entity = this.emailTemplateSystemApi.update(entity);
        Assertions.assertEquals("new Content", entity.getContent());
        Assertions.assertEquals(2, entity.getEntityVersion());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(4)
    void updateShouldFailWithWrongVersion() {
        Query q = this.emailRepository.getQueryBuilderInstance().createQueryFilter("templateName=name0");
        EMailTemplate errorEntity = this.emailTemplateSystemApi.find(q);
        Assertions.assertEquals("name0", errorEntity.getTemplateName());
        Assertions.assertEquals(2, errorEntity.getEntityVersion());
        errorEntity.setEntityVersion(1);
        Assertions.assertThrows(WaterRuntimeException.class, () -> this.emailTemplateSystemApi.update(errorEntity));
    }

    /**
     * Testing finding all entries with no pagination
     */
    @Test
    @Order(5)
    void findAllShouldWork() {
        PaginableResult<EMailTemplate> all = this.emailTemplateSystemApi.findAll(null, -1, -1, null);
        Assertions.assertEquals(1, all.getResults().size());
    }

    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(6)
    void findAllPaginatedShouldWork() {
        for (int i = 2; i < 11; i++) {
            EMailTemplate u = createEMail(i);
            this.emailTemplateSystemApi.save(u);
        }
        PaginableResult<EMailTemplate> paginated = this.emailTemplateSystemApi.findAll(null, 7, 1, null);
        Assertions.assertEquals(7, paginated.getResults().size());
        Assertions.assertEquals(1, paginated.getCurrentPage());
        Assertions.assertEquals(2, paginated.getNextPage());
        paginated = this.emailTemplateSystemApi.findAll(null, 7, 2, null);
        Assertions.assertEquals(3, paginated.getResults().size());
        Assertions.assertEquals(2, paginated.getCurrentPage());
        Assertions.assertEquals(1, paginated.getNextPage());
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(7)
    void removeAllShouldWork() {
        PaginableResult<EMailTemplate> paginated = this.emailTemplateSystemApi.findAll(null, -1, -1, null);
        paginated.getResults().forEach(entity -> {
            this.emailTemplateSystemApi.remove(entity.getId());
        });
        Assertions.assertEquals(0, this.emailTemplateSystemApi.countAll(null));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(8)
    void saveShouldFailOnDuplicatedEntity() {
        EMailTemplate entity = createEMail(1);
        this.emailTemplateSystemApi.save(entity);
        EMailTemplate duplicated = this.createEMail(1);
        //cannot insert new entity wich breaks unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.emailTemplateSystemApi.save(duplicated));
    }

    /**
     * Testing failure on validation failure for example code injection
     */
    @Test
    @Order(9)
    void updateShouldFailOnValidationFailure() {
        EMailTemplate newEntity = new EMailTemplate("<script>function(){alert('ciao')!}</script>", "", "");
        Assertions.assertThrows(ValidationException.class, () -> this.emailTemplateSystemApi.save(newEntity));
    }

    @Test
    @Order(10)
    void saveOrUpdateTemplate() {
        EMailTemplate template = createEMail(101);
        template.setContent("new-content");
        emailContentBuilder.saveOrUpdateTemplate(template.getTemplateName(), template.getContent());
        String body = emailContentBuilder.createBodyFromTemplate("new-content-1", new HashMap<>());
        Assertions.assertNotNull(body);
        emailContentBuilder.saveOrUpdateTemplate(template.getTemplateName(), template.getContent());
    }

    @Test
    @Order(11)
    void testOptions() {
        Assertions.assertNotNull(options.systemSenderName());
        Assertions.assertNotNull(options.smtpHostname());
        Assertions.assertNotNull(options.smtpPort());
        Assertions.assertNotNull(options.smtpUsername());
        Assertions.assertNotNull(options.smtpPassword());
        Assertions.assertFalse(options.isSmtpAuthEnabled());
        Assertions.assertFalse(options.isStartTTLSEnabled());
        Properties properties = new Properties();
        properties.put("it.water.mail.sender.name", "Sender");
        properties.put("it.water.mail.smtp.host", "senderHost.water.it");
        properties.put("it.water.mail.smtp.port", "25");
        properties.put("it.water.mail.smtp.username", "username");
        properties.put("it.water.mail.smtp.password", "password");
        properties.put("it.water.mail.smtp.auth.enabled", "false");
        properties.put("it.water.mail.smtp.start-ttls.enabled", "true");
        applicationProperties.loadProperties(properties);
        Assertions.assertNotNull(options.systemSenderName());
        Assertions.assertNotNull(options.smtpHostname());
        Assertions.assertNotNull(options.smtpPort());
        Assertions.assertNotNull(options.smtpUsername());
        Assertions.assertNotNull(options.smtpPassword());
        Assertions.assertFalse(options.isSmtpAuthEnabled());
        Assertions.assertTrue(options.isStartTTLSEnabled());
    }

    @Test
    @Order(12)
    void testSendEmail() {
        String message = "test@water.it";
        List<String> recipients = new ArrayList<>();
        recipients.add("test@water.it");
        Assertions.assertNotNull(emailNotificationService.getSystemSenderName());
        File attachment = new File("attachment.txt");
        List<File> attachments = new ArrayList<>();
        attachments.add(attachment);
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // Simula il comportamento del metodo statico
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            emailNotificationService.sendMail(message, recipients, recipients, recipients, "subject", "body", attachments);
            emailNotificationService.sendMail("name101", new HashMap<>(), message, recipients, recipients, recipients, "subject", attachments);
            mockedTransport.verify(() -> Transport.send(any(Message.class)), times(2));
        }
    }


    private EMailTemplate createEMail(int seed) {
        EMailTemplate entity = new EMailTemplate("name" + seed, "description" + seed, "content" + seed);
        return entity;
    }
}
