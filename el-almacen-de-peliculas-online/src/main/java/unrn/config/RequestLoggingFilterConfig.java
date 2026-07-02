package unrn.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import unrn.api.RawBodyLoggingFilter;

/**
 * Configuracion servlet que registra el filtro de logging de bodies HTTP.
 *
 * Su funcion es conectar RawBodyLoggingFilter al pipeline de Spring Boot con maxima
 * prioridad. La clase no procesa requests por si misma: solo decide que filtro se
 * instala y con que orden dentro de la aplicacion web.
 */
@Configuration
public class RequestLoggingFilterConfig {

    /**
     * Registra en Spring el filtro que captura el cuerpo crudo de los POST de alta de peliculas.
     * Esta configuracion no implementa reglas de negocio: solo conecta RawBodyLoggingFilter al
     * pipeline HTTP de la aplicacion y lo ubica con maxima prioridad para que pueda envolver el
     * request antes de que lo consuman los controllers, validadores o filtros posteriores.
     *
     * Su utilidad principal es diagnosticar problemas de integracion en /api/admin/peliculas,
     * viendo exactamente que body, content type y longitud llegaron al backend. Si se elimina
     * esta clase, la API sigue funcionando, pero se pierde ese log de diagnostico.
     */
    @Bean
    public FilterRegistrationBean<RawBodyLoggingFilter> rawBodyLoggingFilter() {
        FilterRegistrationBean<RawBodyLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RawBodyLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
