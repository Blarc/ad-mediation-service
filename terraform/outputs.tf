# Outputs
output "ad_mediation_service_url" {
  description = "URL of the Ad Mediation Service"
  value       = google_cloud_run_v2_service.ad_mediation.uri
}