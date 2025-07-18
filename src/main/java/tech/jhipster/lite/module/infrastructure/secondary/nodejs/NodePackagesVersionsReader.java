package tech.jhipster.lite.module.infrastructure.secondary.nodejs;

import tech.jhipster.lite.module.domain.nodejs.NodePackagesVersions;

/**
 * <p>
 * Read version for a Node.js dependency
 * </p>
 *
 * <p>
 * Spring beans instances of this interface will be used to resolve Node.js versions
 * </p>
 */
public interface NodePackagesVersionsReader {
  /**
   * Get the Node.js packages versions from the given source
   *
   * @return The managed Node.js package versions
   */
  NodePackagesVersions get();
}
