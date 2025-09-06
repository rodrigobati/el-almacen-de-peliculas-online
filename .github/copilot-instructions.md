# Descripcion general

- Este es un proyecto escrito en Java 23 utilizando el paradigma orientado a objetos.
- El modelo de dominio es donde se implementan todas las reglas de negocio.

## Estructura de Carpetas

- Es un proyecto maven clásico.
- src/main/java para los fuentes
- en unrn.model van las clases del modelo de dominio
- src/test/java para los tests

# Modelo de Dominio

- Nunca generes getters ni setters. No quiero objetos anémicos
- Si necesitas un getter de una lista encapsulada, devolvela solo lectura.
- Los objetos se inicializan siempre por constructor, para generar siempre objetos completos, listos para usar.
- Pone todas las validaciones en el constructor, siempre que sea posible, para instanciar objetos válidos.
- Cuando lances exceptions, siempre usa RuntimeException. Y el mensaje de error ponelo en una constante estática con
  visibilidad de paquete para poder usarla en los tests después.
- Cada validación de un constructor hacela en un método de instancia privado que se llama assert{LO_QUE_ESTAS_VALIDANDO}
- Usa el principio tell don't ask siempre que sea posible. Por ejemplo: esto es INCORRECTO:

```java
private void assertContactoUnico(Tweet tweet) {
    for (Tweet t : tweets) {
        if (t.nombre().equals(tweet.nombre())) {
            throw new RuntimeException(ERROR_CONTACTO_DUPLICADO);
        }
    }
}
```

- Agrega test unitarios para cada modificacion o creacion de objetos que realices.

## Testing Automatizado con JUnit 5.13

### 1. Nombre claro y descriptivo

- Nombra los métodos de test siguiendo este patrón:  
  **`cuestionATestear_resultadoEsperado`**
- Agrega la anotacion `@DisplayName` explicando en lenguaje natural el objetivo del test con este formato:

```java
  @DisplayName("CuestionATestear resultadoEsperado")
```

### 2. No uses Mock, Stubs o Fakes

- Los tests unitarios corren todos en memoria ejutando el código real.
- No uses mocks, stubs o fakes, ya que no son necesarios y complican la lectura del test.
- Solo usaremos mocks en consumo de servicios externos (pagos online, emails, etc).

### 3. Estructura del test

- Cada test debe tener la siguiente estructura:
  ```java
  @Test
  @DisplayName("Nombre del test")
  void testNombreDelMetodo() {
      // Setup: Preparar el escenario
      // Ejercitación: Ejecutar la acción a probar
      // Verificación: Verificar el resultado esperado
  }
  ```
- Incorpora comentarios de la estructura del test para facilitar la comprensión del código.

### 4. Un solo caso de prueba por test

- Cada test debe evaluar un único caso de prueba.
- Si necesitas evaluar múltiples casos, crea un test separado para cada uno.

### 5. Usa Asserts claros y descriptivos

- Utiliza aserciones claras y descriptivas agregando mensajes que expliquen el propósito de la verificación.
  - Por ejemplo:
    ```java
    assertEquals(expectedValue, actualValue, "El valor esperado no coincide con el valor actual");
    ```

### 6. Probá Casos límites

- Valores nulos (null)
- Listas vacías o inputs vacíos
- Números negativos o fuera de rango
- Estados inválidos o excepciones esperadas

### 7. Verifica excepciones correctamente

- Utiliza `assertThrows` para verificar que se lanza la excepción esperada.
- En general mi código lanzará RuntimeException, pero verificaló primero.
- Ejemplo:
  ```java
    var ex = assertThrows(RuntimeException.class, () -> {
            // Código que debería lanzar la excepción
        });
        assertEquals("Mensaje de error esperado", ex.getMessage());
  ```
- Sobre el "Mensaje de error esperado", antes de poner una constante dura en el test verifica que la constante definida
  en el codigo real y si es asi úsala.

### 8. Testing de Integración

- Usamos test-data.sql como set up inicial de la BD.
- Siempre usar como beforeEach el truncate ya que resetea la base de datos despues de cada test que corre:

```java
void beforeEach() {
    emf.getSchemaManager().truncate();
}
```

- No incluyas casos de tests que pueden ser testeados con tests unitarios.
