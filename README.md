
<hr></hr>
el-almacen-de-peliculas-online
Descripción General
Este proyecto corresponde al vertical de Películas dentro de una arquitectura distribuida compuesta por cuatro verticales independientes:
Películas (este proyecto)
Ventas (proyecto separado)
Rating (proyecto separado)
Descuentos (proyecto separado)
La gestión de usuarios se realiza mediante Keycloak. El frontend está desarrollado en React y se conecta a los verticales a través de una API Gateway.
<hr></hr>
Requerimientos Funcionales
Home: Lista el catálogo de películas ordenadas de la más nueva a la más vieja. Cada ítem muestra título, fecha de salida, precio, directores, actores e imagen pequeña.
Detalle de Película: Al hacer click, se accede al detalle con condición (nuevo/usado), título, directores, precio, formato, género, sinopsis, actores, imagen ampliada, fecha de salida, rating (5 estrellas) y detalle de cada voto con comentario.
Usuarios:
Clientes: Compran películas, pueden agregar al carrito, ver y modificar el carrito, realizar compras y acceder a su historial.
Administradores: Mantienen el catálogo y gestionan descuentos.
Carrito de Compras: Permite agregar, quitar productos y ver el total.
Registro de Compras: Cada compra registra fecha, cliente y productos. El cliente puede consultar su historial.
Descuentos: El administrador puede cargar descuentos por rango de fecha y monto.
Notificaciones: Tras cada compra, el cliente recibe un email con el detalle (futuro soporte para Whatsapp).
Votación: El cliente puede votar una película (1 a 5 estrellas) y dejar un comentario. No puede votar dos veces la misma película.
Administración: El administrador puede agregar/modificar películas y descuentos.
<hr></hr>
Arquitectura
Backend: Java 23, Maven, modelo de dominio en unrn.model siguiendo buenas prácticas de diseño y validaciones en constructores.
Frontend: React (proyecto separado).
API Gateway: Orquesta la comunicación entre verticales.
Usuarios: Keycloak para autenticación y autorización.
<hr></hr>
Universidad Nacional de Río Negro
Taller de Tecnologías y Producción de Software
Lic. En Sistemas
Trabajo Práctico General
<hr></hr>
Notas de Desarrollo
El modelo de dominio implementa todas las reglas de negocio.
No se generan getters/setters; los objetos se inicializan por constructor y validan su estado.
Se proveen métodos para obtener listas solo lectura.
Las validaciones se realizan en métodos privados en el constructor.
Las excepciones usan RuntimeException y mensajes definidos como constantes de paquete.
Los tests unitarios se implementan con JUnit 5.13 siguiendo la estructura y convenciones indicadas en las instrucciones del proyecto.
<hr></hr>
Cambios y Nuevos Requerimientos
Durante el desarrollo pueden surgir nuevos requerimientos o modificaciones sobre los existentes. El proyecto está preparado para adaptarse a estos cambios siguiendo las mejores prácticas de arquitectura y diseño de software.
<hr></hr>
