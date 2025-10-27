# Ad Mediation Service

A REST API built with Quarkus for retrieving ad network priorities.

- The service is available at [GCP](https://ad-mediation-service-340096700111.europe-west1.run.app/q/swagger-ui)
  and [self-hosted Kubernetes](https://ad-mediation-service.external.blarc.my.id/q/swagger-ui).
- OpenAPI schema is available at `/q/openapi`.

## 🏗️ Tech Stack

- **Framework**: Quarkus 3.24.5
- **Language**: Java 21
- **Database**: Redis
- **Security**: Basic authentication
- **API Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5, AssertJ

## Deployment

- Docker files are available [here](src/main/docker).
- The service can be easily deployed to GCP with Terraform. The documentation and configuration files can be
  found [here](terraform).
- The configuration files used for the deployment on Kubernetes can be
  found [here](https://github.com/Blarc/home-k8s/tree/main/kubernetes/apps/ad-mediation-service).

## Development

### 📋 Requirements

- Java 21 or higher
- Docker (for running Redis with Dev Services)

### Development Mode

Run the application in development mode with live coding enabled:

```shell
./mvnw quarkus:dev
```

The application will be available at http://localhost:8080

### Testing

Execute tests while in dev mode by pressing `r`, or run tests separately:

```shell
./mvnw test
```

### 🔍 API Endpoints

Once the application is running, you can explore the API at:

- **API Documentation**: http://localhost:8080/q/swagger-ui

Main endpoints include:

- `/ads` - Ad network priorities

### 🛠️ Configuration

Configuration properties can be found in [application.yaml](src/main/resources/application.yaml).

Customize the application by setting environment variables or updating the configuration file.

### 💡 Development Tips

- Use Dev UI at http://localhost:8080/q/dev/ for development tools
- Hot reload is enabled in dev mode
