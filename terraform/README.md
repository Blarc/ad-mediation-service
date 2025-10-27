# Terraform

This directory contains Terraform configuration files for deploying the application to GCP Cloud Run service. The
terraform configuration creates the following resources:
- Artifact Registry repository for proxying GitHub Container Registry images.
- Cloud Run service for the application.

## Prerequisites

- Terraform CLI installed.
- Google Cloud CLI installed.
- GCP project with billing enabled.
- Enable Secret Manager API and create Redis URL secret named `REDIS_URL`.

## Deployment

1. To authenticate with gcloud CLI run the following command:
   ```bash
   gcloud auth login
   ```
2. Make sure you're using the correct project by listing the projects:
   ```bash
   gcloud projects list
   ```
   and setting the one you'd like to use:
   ```bash
   gcloud config set project $PROJECT_ID
   ```
3. Initialize terraform:
   ```bash
   terraform init
   ```
4. Plan the creation of all resources:
   ```bash
   terraform plan
   ```
5. Create resources with terraform:
   ```bash
   terraform apply
   ```

## Destroy

1. Run the following command:
   ```bash
   terraform destroy --auto-approve
   ```