package tech.jhipster.lite.generator.server.springboot.customjhlite.domain;

import static tech.jhipster.lite.generator.server.springboot.cucumbercommon.domain.CucumbersModules.cucumberModuleBuilder;
import static tech.jhipster.lite.module.domain.JHipsterModule.JHipsterModuleBuilder;
import static tech.jhipster.lite.module.domain.JHipsterModule.artifactId;
import static tech.jhipster.lite.module.domain.JHipsterModule.comment;
import static tech.jhipster.lite.module.domain.JHipsterModule.documentationTitle;
import static tech.jhipster.lite.module.domain.JHipsterModule.from;
import static tech.jhipster.lite.module.domain.JHipsterModule.groupId;
import static tech.jhipster.lite.module.domain.JHipsterModule.javaDependency;
import static tech.jhipster.lite.module.domain.JHipsterModule.lineBeforeText;
import static tech.jhipster.lite.module.domain.JHipsterModule.path;
import static tech.jhipster.lite.module.domain.JHipsterModule.propertyKey;
import static tech.jhipster.lite.module.domain.JHipsterModule.propertyValue;
import static tech.jhipster.lite.module.domain.JHipsterModule.text;
import static tech.jhipster.lite.module.domain.JHipsterModule.to;
import static tech.jhipster.lite.module.domain.JHipsterModule.toSrcMainJava;
import static tech.jhipster.lite.module.domain.JHipsterModule.toSrcMainResources;
import static tech.jhipster.lite.module.domain.JHipsterModule.toSrcTestJava;
import static tech.jhipster.lite.module.domain.JHipsterModule.versionSlug;

import java.util.function.Consumer;
import tech.jhipster.lite.module.domain.JHipsterModule;
import tech.jhipster.lite.module.domain.JHipsterProjectFilePath;
import tech.jhipster.lite.module.domain.file.JHipsterDestination;
import tech.jhipster.lite.module.domain.file.JHipsterSource;
import tech.jhipster.lite.module.domain.javadependency.JavaDependency;
import tech.jhipster.lite.module.domain.javadependency.JavaDependency.JavaDependencyOptionalValueBuilder;
import tech.jhipster.lite.module.domain.javadependency.JavaDependencyScope;
import tech.jhipster.lite.module.domain.javadependency.JavaDependencyType;
import tech.jhipster.lite.module.domain.javaproperties.PropertyKey;
import tech.jhipster.lite.module.domain.properties.JHipsterModuleProperties;
import tech.jhipster.lite.shared.error.domain.Assert;

public class CustomJHLiteModuleFactory {

  private static final String DOMAIN = "domain";
  private static final String SHARED = "shared";
  private static final String INFRASTRUCTURE = "infrastructure";
  private static final String SECONDARY = "secondary";
  private static final String DEPENDENCIES = "dependencies";

  private static final JHipsterSource SOURCE = from("server/springboot/custom-jhlite");
  private static final JHipsterSource MAIN_SOURCE = SOURCE.append("main");
  private static final JHipsterSource SLUG_SOURCE = MAIN_SOURCE.append(SHARED).append("slug");

  private static final JHipsterSource DEPENDENCIES_MAIN_SOURCE = MAIN_SOURCE.append(SHARED).append(DEPENDENCIES);
  private static final JHipsterSource TEST_SOURCE = SOURCE.append("test");
  private static final JHipsterSource DEPENDENCIES_TEST_SOURCE = TEST_SOURCE.append(SHARED).append(DEPENDENCIES);
  private static final JHipsterSource CUCUMBER_SOURCE = from("server/springboot/cucumber");

  private static final String SRC_MAIN_JAVA = "src/main/java";

  private static final PropertyKey SERVER_PORT_KEY = propertyKey("server.port");
  private static final PropertyKey JACKSON_INCLUSION_KEY = propertyKey("spring.jackson.default-property-inclusion");

  private static final PropertyKey HIDDEN_SLUGS_PROPERTY_KEY = propertyKey("jhlite.hidden-resources.slugs");
  private static final PropertyKey BEAN_DEFINITION_OVERRIDING_PROPERTY_KEY = propertyKey("spring.main.allow-bean-definition-overriding");
  private static final String PACKAGE_INFO_JAVA = "package-info.java";

  public JHipsterModule buildModule(JHipsterModuleProperties properties) {
    Assert.notNull("properties", properties);

    // @formatter:off
    return cucumberModuleBuilder(properties)
      .context()
        .put("baseName", properties.projectBaseName().capitalized())
        .and()
      .documentation(documentationTitle("Module creation"), SOURCE.template("module-creation.md"))
      .javaDependencies()
        .addDependency(jhipsterLiteDependency())
        .addDependency(jhipsterLiteTestDependency())
        .addTestDependency(groupId("com.approvaltests"), artifactId("approvaltests"), versionSlug("approvaltests"))
      .and()
      .mandatoryReplacements()
        .in(mainClassFile(properties))
          .add(text("@SpringBootApplication"), springBootApplicationWithJHLite(properties))
          .add(lineBeforeText("import org.springframework.boot.SpringApplication;"), "import tech.jhipster.lite.JHLiteApp;")
        .and()
      .and()
      .springMainProperties()
        .set(SERVER_PORT_KEY, propertyValue(properties.serverPort().get()))
        .set(JACKSON_INCLUSION_KEY, propertyValue("non_null"))
        .comment(HIDDEN_SLUGS_PROPERTY_KEY, comment("Disable the modules and its dependencies by slugs"))
        .set(HIDDEN_SLUGS_PROPERTY_KEY, propertyValue("custom-jhlite"))
        .and()
      .springTestProperties()
        .set(SERVER_PORT_KEY, propertyValue(0))
        .set(BEAN_DEFINITION_OVERRIDING_PROPERTY_KEY, propertyValue(true))
        .and()
      .files()
        .batch(SOURCE.append("tests-ci"),to("tests-ci"))
          .addExecutableTemplate("generate.sh")
          .addTemplate("modulePayload.json")
          .addExecutableTemplate("start.sh")
          .addExecutable("stop.sh")
          .and()
        .and()
      .apply(cucumberBuilder(properties))
      .apply(dependenciesReadersBuilder(properties))
      .apply(slugBuilder(properties))
      .build();
    // @formatter:on
  }

  private Consumer<JHipsterModuleBuilder> cucumberBuilder(JHipsterModuleProperties properties) {
    String packagePath = properties.packagePath();
    JHipsterDestination cucumberDestination = toSrcTestJava().append(packagePath).append("cucumber");

    // @formatter:off
    return builder -> builder
      .documentation(documentationTitle("Cucumber"), CUCUMBER_SOURCE.template("cucumber.md"))
      .files()
        .batch(TEST_SOURCE, cucumberDestination)
          .addTemplate("CucumberTest.java")
          .addTemplate("CucumberConfiguration.java")
        .and()
        .add(CUCUMBER_SOURCE.append("rest").template("CucumberRestTemplate.java"), cucumberDestination.append("rest").append("CucumberRestTemplate.java"))
        .add(CUCUMBER_SOURCE.file("gitkeep"), to("src/test/features/.gitkeep"));
    // @formatter:on
  }

  private Consumer<JHipsterModuleBuilder> dependenciesReadersBuilder(JHipsterModuleProperties properties) {
    String packagePath = properties.packagePath();
    String baseName = properties.projectBaseName().capitalized();
    JHipsterDestination npmMainDestination = toSrcMainJava().append(packagePath).append(SHARED).append(DEPENDENCIES);
    JHipsterDestination npmTestDestination = toSrcTestJava().append(packagePath).append(SHARED).append(DEPENDENCIES);

    // @formatter:off
    return builder -> builder
        .context()
          .put("baseNameUpperCased", properties.projectBaseName().upperCased())
          .put("baseNameKebabCased", properties.projectBaseName().kebabCase())
          .and()
        .files()
          .add(DEPENDENCIES_MAIN_SOURCE.template(PACKAGE_INFO_JAVA), npmMainDestination.append(PACKAGE_INFO_JAVA))
          .add(
            DEPENDENCIES_MAIN_SOURCE.append(DOMAIN).template("NodePackagesVersionSource.java"),
            npmMainDestination.append(DOMAIN).append(baseName + "NodePackagesVersionSource.java")
          )
          .add(
            DEPENDENCIES_MAIN_SOURCE.append(INFRASTRUCTURE).append(SECONDARY).template("NodePackagesVersionsReader.java"),
            npmMainDestination.append(INFRASTRUCTURE).append(SECONDARY).append(baseName + "NodePackagesVersionsReader.java")
          )
          .add(
            DEPENDENCIES_MAIN_SOURCE.append(INFRASTRUCTURE).append(SECONDARY).template("MavenDependenciesReader.java"),
            npmMainDestination.append(INFRASTRUCTURE).append(SECONDARY).append(baseName + "MavenDependenciesReader.java")
          )
          .add(
            DEPENDENCIES_TEST_SOURCE.append(INFRASTRUCTURE).append(SECONDARY).template("NodePackagesVersionsReaderTest.java"),
            npmTestDestination.append(INFRASTRUCTURE).append(SECONDARY).append(baseName + "NodePackagesVersionsReaderTest.java")
          )
          .add(
            DEPENDENCIES_TEST_SOURCE.append(INFRASTRUCTURE).append(SECONDARY).template("MavenDependenciesReaderTest.java"),
            npmTestDestination.append(INFRASTRUCTURE).append(SECONDARY).append(baseName + "MavenDependenciesReaderTest.java")
          )
          .add(
            SOURCE.file("package.json"),
            toSrcMainResources()
              .append("generator")
              .append(properties.projectBaseName().kebabCase() + "-dependencies")
              .append(properties.projectBaseName().kebabCase())
              .append("package.json")
          )
          .add(
            SOURCE.file("pom.xml.mustache"),
            toSrcMainResources().append("generator").append(properties.projectBaseName().kebabCase() + "-dependencies").append("pom.xml")
          );
    // @formatter:on
  }

  private Consumer<JHipsterModuleBuilder> slugBuilder(JHipsterModuleProperties properties) {
    String packagePath = properties.packagePath();
    String baseName = properties.projectBaseName().capitalized();
    JHipsterDestination slugDestination = toSrcMainJava().append(packagePath).append(SHARED).append("slug");

    // @formatter:off
    return builder -> builder
        .files()
          .add(SLUG_SOURCE.template(PACKAGE_INFO_JAVA), slugDestination.append(PACKAGE_INFO_JAVA))
          .add(SLUG_SOURCE.append(DOMAIN).template("FeatureSlug.java"), slugDestination.append(DOMAIN).append(baseName + "FeatureSlug.java"))
          .add(SLUG_SOURCE.append(DOMAIN).template("ModuleSlug.java"), slugDestination.append(DOMAIN).append(baseName + "ModuleSlug.java"));
    // @formatter:on
  }

  private JavaDependency jhipsterLiteDependency() {
    return jhLiteDependencyBuilder().build();
  }

  private JavaDependency jhipsterLiteTestDependency() {
    return jhLiteDependencyBuilder().classifier("tests").scope(JavaDependencyScope.TEST).type(JavaDependencyType.TEST_JAR).build();
  }

  private JavaDependencyOptionalValueBuilder jhLiteDependencyBuilder() {
    return javaDependency().groupId("tech.jhipster.lite").artifactId("jhlite").versionSlug("jhlite");
  }

  private String springBootApplicationWithJHLite(JHipsterModuleProperties properties) {
    return "@SpringBootApplication(scanBasePackageClasses = { JHLiteApp.class, " + mainClassName(properties) + ".class })";
  }

  private JHipsterProjectFilePath mainClassFile(JHipsterModuleProperties properties) {
    return path(SRC_MAIN_JAVA).append(properties.packagePath()).append(mainClassName(properties) + ".java");
  }

  private String mainClassName(JHipsterModuleProperties properties) {
    return properties.projectBaseName().capitalized() + "App";
  }
}
