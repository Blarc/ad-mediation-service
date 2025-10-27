# Service account for Ad Mediation Service
resource "google_service_account" "ad_mediation_sa" {
  account_id   = "ad-mediation-service"
  display_name = "Ad Mediation Service Account"
}

# Grant Ad Mediation service account permission to pull from Artifact Registry
resource "google_artifact_registry_repository_iam_member" "ad_mediation_artifact_access" {
  location   = "europe"
  repository = "github-container-registry"
  role       = "roles/artifactregistry.reader"
  member     = "serviceAccount:${google_service_account.ad_mediation_sa.email}"
}

# Deploy Ad Mediation Service on Cloud Run
resource "google_cloud_run_v2_service" "ad_mediation" {
  name     = "ad-mediation-service"
  location = var.region

  deletion_protection = false

  template {
    service_account = google_service_account.ad_mediation_sa.email

    containers {
      image = "europe-docker.pkg.dev/ad-mediation-service-476306/github-container-registry/blarc/ad-mediation-service:b0c095ea92d17acbf49c884e7733c98a67885274"

      ports {
        container_port = 8080
      }

      # Reference the secret instead of plain text
      env {
        name = "QUARKUS_REDIS_HOSTS"
        value_source {
          secret_key_ref {
            secret  = "REDIS_URL"
            version = "latest"
          }
        }
      }

      env {
        name  = "QUARKUS_REDIS_TLS_ENABLED"
        value = "true"
      }

      env {
        name  = "QUARKUS_REDIS_TLS_TRUST_ALL"
        value = "true"
      }

      resources {
        limits = {
          cpu    = "500m"
          memory = "512Mi"
        }
        cpu_idle = true
      }

      startup_probe {
        initial_delay_seconds = 15
        period_seconds        = 5
        timeout_seconds       = 3
        failure_threshold     = 3

        http_get {
          path = "/q/health/ready"
          port = 8080
        }
      }

      liveness_probe {
        initial_delay_seconds = 30
        period_seconds        = 10
        timeout_seconds       = 3
        failure_threshold     = 3

        http_get {
          path = "/q/health/ready"
          port = 8080
        }
      }
    }

    scaling {
      min_instance_count = 0
      max_instance_count = 10
    }
  }

  # Allow all ingress traffic
  ingress = "INGRESS_TRAFFIC_ALL"

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }
}

# Allow unauthenticated access to Ad Mediation Service (adjust as needed)
resource "google_cloud_run_v2_service_iam_member" "ad_mediation_public" {
  name     = google_cloud_run_v2_service.ad_mediation.name
  location = google_cloud_run_v2_service.ad_mediation.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# Grant the service account access to read the secret
resource "google_secret_manager_secret_iam_member" "ad_mediation_redis_access" {
  secret_id = "REDIS_URL"
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.ad_mediation_sa.email}"
}