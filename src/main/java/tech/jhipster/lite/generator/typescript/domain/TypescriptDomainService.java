package tech.jhipster.lite.generator.typescript.domain;

import tech.jhipster.lite.generator.packagemanager.npm.domain.NpmService;
import tech.jhipster.lite.generator.project.domain.Project;
import tech.jhipster.lite.generator.project.domain.ProjectRepository;

public class TypescriptDomainService implements TypescriptService {

  public static final String SOURCE = "typescript";

  private final ProjectRepository projectRepository;
  private final NpmService npmService;

  public TypescriptDomainService(ProjectRepository projectRepository, NpmService npmService) {
    this.projectRepository = projectRepository;
    this.npmService = npmService;
  }

  @Override
  public void init(Project project) {
    Typescript.devDependencies().forEach((dependency, version) -> npmService.addDevDependency(project, dependency, version));
    Typescript.scripts().forEach((name, cmd) -> npmService.addScript(project, name, cmd));
    Typescript.files().forEach(file -> projectRepository.add(project, SOURCE, file));
  }
}
