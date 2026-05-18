# Microservicio de Control de Subastas (`subasta-proyecto`)

## Integrantes
* **Gonzalo Hormazábal**
* **Geraldinne González**


## Descripción
Módulo del motor de subastas. Controla los tiempos de inicio y cierre de las pujas, los precios base de reserva y la determinación automática de los ganadores de cada lote.

* **Puerto:** `8083`
* **Base de Datos:** `subastas_db` (MySQL)


## Funcionalidades Clave
* Apertura, monitoreo y cierre cronometrado de salas de subasta.
* Control de montos mínimos de reserva para adjudicación.
* Exposición de endpoints REST para la validación de estados de salas activos.


## Configuración (`application.properties`)
* server.port=8083
* spring.datasource.url=jdbc:mysql://localhost:3306/subastas_db
* spring.datasource.username=root
* spring.datasource.password=
* spring.jpa.hibernate.ddl-auto=update
* logging.level.cl.sda1085.subastas=DEBUG


## Pasos para Ejecutar

### 1. Preparación de la Base de Datos
Antes de ejecutar el servicio, crear la conexión a la base de datos de MySQL (XAMPP) corriendo en el puerto 3306 y con el nombre 'subastas_db'.

### 2. Verificación de Credenciales
Revisar que el archivo application.properties tenga por defecto, usuario root y contraseña vacía.

### 3. Lanzamiento del Microservicio
Ejecutar (run) la clase principal con la anotación @SpringBootApplication (SubastasApplication.java).

### 4. Reglas de Seguridad
Al consumir los endpoints en Postman, tener en cuenta el comportamiento de la cadena de filtros de seguridad:

* Ver Salas Activas (GET /api/subastas): Es de acceso público para que los postores vean los cronómetros en tiempo real (No Auth).
