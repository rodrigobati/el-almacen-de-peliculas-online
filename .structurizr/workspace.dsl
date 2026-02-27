workspace "Microservicio Descuentos" "Modelo C4 del vertical de descuentos con Keycloak, API Gateway, RabbitMQ y MySQL" {

    !identifiers hierarchical

    model {
        # Actores
        cliente = person "Cliente" "Consulta descuentos vigentes" {
            tags "Cliente"
        }
        admin = person "Administrador" "Gestiona cupones y descuentos" {
            tags "Admin"
        }

        # Sistemas externos
        frontend = softwareSystem "Frontend React Vite" "SPA que consume descuentos" {
            tags "External System" "Frontend"
        }
        apiGateway = softwareSystem "API Gateway" "Punto de entrada; valida JWT y enruta" {
            tags "External System" "Gateway"
        }
        keycloak = softwareSystem "Keycloak" "IAM OAuth2/OIDC con roles ADMIN/CLIENTE" {
            tags "External System" "Security"
        }
        ventasSystem = softwareSystem "Microservicio Ventas" "Solicita validacion de cupones via RPC" {
            tags "External System" "Microservice"
        }
        rabbitMQ = softwareSystem "RabbitMQ" "Broker de mensajeria para RPC" {
            tags "External System" "Message Broker"
        }

        # Sistema principal
        descuentosSystem = softwareSystem "Microservicio Descuentos" "Gestion de cupones, validacion y consultas" {
            tags "Internal System"

            apiApp = container "Descuentos API" "Expone API REST y consume RPC" "Java 17, Spring Boot" {
                tags "Container" "Spring Boot"

                descuentoController = component "DescuentoController" "REST Controller" "Spring MVC" {
                    tags "Controller"
                }
                cuponService = component "CuponService" "Logica de negocio" "Spring Service" {
                    tags "Service"
                }
                cuponRepository = component "CuponRepository" "Acceso a datos" "Spring Data JPA" {
                    tags "Repository"
                }
                cuponEntity = component "CuponEntity" "Entidad JPA" "JPA" {
                    tags "Entity"
                }
                validarCuponRpcListener = component "ValidarCuponRpcListener" "Listener RPC" "Spring AMQP" {
                    tags "Event Listener"
                }
                cuponDTO = component "CuponDTO" "DTO de cupon" "Java Record" {
                    tags "DTO"
                }
                validarCuponRequest = component "ValidarCuponRequest" "DTO request RPC" "Java Record" {
                    tags "DTO"
                }
                validarCuponResponse = component "ValidarCuponResponse" "DTO response RPC" "Java Record" {
                    tags "DTO"
                }
                securityConfig = component "SecurityConfig" "Seguridad JWT y roles" "Spring Security" {
                    tags "Security Component"
                }

                # Relaciones internas de componentes
                descuentoController -> securityConfig "Protegido por"
                descuentoController -> cuponService "Usa"
                descuentoController -> cuponDTO "Responde con"
                cuponService -> cuponRepository "Consulta"
                cuponRepository -> cuponEntity "Mapea"
                validarCuponRpcListener -> validarCuponRequest "Deserializa"
                validarCuponRpcListener -> cuponService "Valida"
                validarCuponRpcListener -> validarCuponResponse "Construye"
            }

            descuentosDb = container "Base de Datos Descuentos" "Persistencia de cupones" "MySQL" {
                tags "Database"
            }
        }

        # Relaciones de contexto
        cliente -> frontend "Consulta descuentos"
        admin -> frontend "Administra cupones"
        frontend -> keycloak "Autentica"
        keycloak -> frontend "Entrega JWT"
        frontend -> apiGateway "Llama API con JWT"
        apiGateway -> keycloak "Valida JWT"
        apiGateway -> descuentosSystem.apiApp "Enruta"

        ventasSystem -> rabbitMQ "Solicita validacion RPC"
        rabbitMQ -> descuentosSystem.apiApp "Entrega mensaje"
        descuentosSystem.apiApp -> rabbitMQ "Responde RPC"

        descuentosSystem.apiApp -> descuentosSystem.descuentosDb "Lee/Escribe"
    }

    views {
        # Nivel 1: Contexto del sistema
        systemContext descuentosSystem "Contexto" {
            include cliente admin frontend apiGateway keycloak ventasSystem rabbitMQ descuentosSystem
            autolayout lr
        }

        # Nivel 2: Contenedores
        container descuentosSystem "Contenedores" {
            include descuentosSystem.apiApp descuentosSystem.descuentosDb frontend apiGateway keycloak ventasSystem rabbitMQ cliente admin
            autolayout lr
        }

        # Nivel 3: Componentes (dentro del contenedor API)
        component descuentosSystem.apiApp "Componentes" {
            include *
            autolayout lr
        }

        styles {
            element "Element" {
                color #0773af
                stroke #0773af
                strokeWidth 7
                shape roundedbox
            }
            element "Person" {
                shape person
            }
            element "Database" {
                shape cylinder
            }
            element "Message Broker" {
                shape pipe
            }
            element "External System" {
                background #999999
                color #ffffff
            }
            element "Internal System" {
                background #1168bd
                color #ffffff
            }
            element "Spring Boot" {
                background #6db33f
                color #ffffff
            }
            element "Controller" {
                background #3498db
                color #ffffff
            }
            element "Service" {
                background #9b59b6
                color #ffffff
            }
            element "Repository" {
                background #e67e22
                color #ffffff
            }
            element "Entity" {
                background #e74c3c
                color #ffffff
            }
            element "DTO" {
                background #95a5a6
                color #ffffff
            }
            element "Security" {
                background #2ecc71
                color #ffffff
            }
            element "Security Component" {
                background #27ae60
                color #ffffff
            }
        }
    }

    configuration {
        scope softwaresystem
    }

}