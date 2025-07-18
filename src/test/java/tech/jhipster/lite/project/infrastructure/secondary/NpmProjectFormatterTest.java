package tech.jhipster.lite.project.infrastructure.secondary;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.jhipster.lite.TestFileUtils;
import tech.jhipster.lite.UnitTest;
import tech.jhipster.lite.project.domain.ProjectPath;

@UnitTest
@Disabled("Only works on computer with npm installed and usable")
class NpmProjectFormatterTest {

  private static final NpmProjectFormatter formatter = new NpmProjectFormatter("npm");

  @Test
  void shouldFormatProject() throws IOException {
    String directory = TestFileUtils.tmpDirForTest();
    Path jsonFile = simpleNpmProject(directory);

    formatter.format(new ProjectPath(directory));

    assertThat(Files.readString(jsonFile)).isEqualTo(
        """
        { "key": "value" }
        """
      );
  }

  private static Path simpleNpmProject(String directory) throws IOException {
    Path path = Path.of(directory);
    Files.createDirectories(path);

    Files.copy(Path.of("src/test/resources/projects/files/package.json"), path.resolve("package.json"));

    Path jsonFile = path.resolve("file.json");
    Files.writeString(
      jsonFile,
      """
      {"key":"value"}
      """
    );

    return jsonFile;
  }
}
