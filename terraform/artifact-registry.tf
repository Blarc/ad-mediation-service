resource "google_artifact_registry_repository" "github_container_registry" {
  location      = "europe"
  repository_id = "github-container-registry"
  description   = "Remote repository for GitHub Container Registry (ghcr.io)"
  format        = "DOCKER"
  mode          = "REMOTE_REPOSITORY"

  remote_repository_config {
    docker_repository {
      custom_repository {
        uri = "https://ghcr.io"
      }
    }
  }
}