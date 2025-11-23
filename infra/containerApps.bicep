param location string = resourceGroup().location
param containerAppsEnvironmentId string
param containerRegistryUrl string
param containerRegistryUsername string
param containerRegistryPassword string

var projectName = 'fastfood'

// API Gateway
resource apiGatewayApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-api-gateway'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8085
        allowInsecure: false
        traffic: [
          {
            latestRevision: true
            weight: 100
          }
        ]
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/api-gateway:latest'
          name: 'api-gateway'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
          env: [
            {
              name: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
              value: 'http://eureka-server:8761/eureka/'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 3
      }
    }
  }
}

// Eureka Server
resource eurekaApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-eureka-server'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8761
        allowInsecure: false
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/eureka-server:latest'
          name: 'eureka-server'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 2
      }
    }
  }
}

// Order Service
resource orderServiceApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-order-service'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8082
        allowInsecure: false
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/order-service:latest'
          name: 'order-service'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
          env: [
            {
              name: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
              value: 'http://eureka-server:8761/eureka/'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 3
      }
    }
  }
  dependsOn: [
    eurekaApp
  ]
}

// Product Service
resource productServiceApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-product-service'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8088
        allowInsecure: false
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/product-service:latest'
          name: 'product-service'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
          env: [
            {
              name: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
              value: 'http://eureka-server:8761/eureka/'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 3
      }
    }
  }
  dependsOn: [
    eurekaApp
  ]
}

// User Service
resource userServiceApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-user-service'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8081
        allowInsecure: false
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/user-service:latest'
          name: 'user-service'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
          env: [
            {
              name: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
              value: 'http://eureka-server:8761/eureka/'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 3
      }
    }
  }
  dependsOn: [
    eurekaApp
  ]
}

// Payment Service
resource paymentServiceApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-payment-service'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8083
        allowInsecure: false
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/payment-service:latest'
          name: 'payment-service'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
          env: [
            {
              name: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
              value: 'http://eureka-server:8761/eureka/'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 2
      }
    }
  }
  dependsOn: [
    eurekaApp
  ]
}

// Restaurant Service
resource restaurantServiceApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-restaurant-service'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8084
        allowInsecure: false
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/restaurant-service:latest'
          name: 'restaurant-service'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
          env: [
            {
              name: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
              value: 'http://eureka-server:8761/eureka/'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 2
      }
    }
  }
  dependsOn: [
    eurekaApp
  ]
}

// Drone Service
resource droneServiceApp 'Microsoft.App/containerApps@2023-04-01-preview' = {
  name: '${projectName}-drone-service'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironmentId
    configuration: {
      ingress: {
        external: true
        targetPort: 8086
        allowInsecure: false
      }
      registries: [
        {
          server: containerRegistryUrl
          username: containerRegistryUsername
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistryPassword
        }
      ]
    }
    template: {
      containers: [
        {
          image: '${containerRegistryUrl}/drone-service:latest'
          name: 'drone-service'
          resources: {
            cpu: '0.25'
            memory: '0.5Gi'
          }
          env: [
            {
              name: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
              value: 'http://eureka-server:8761/eureka/'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 2
      }
    }
  }
  dependsOn: [
    eurekaApp
  ]
}

output apiGatewayUrl string = 'https://${apiGatewayApp.properties.configuration.ingress.fqdn}'
output eurekaUrl string = 'https://${eurekaApp.properties.configuration.ingress.fqdn}'
