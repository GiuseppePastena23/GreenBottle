package it.unisa.greenbottle.controller.filters;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe di configurazione del filtro per il cliente.
 */
@Configuration
@RequiredArgsConstructor
public class ClienteConfigFilter {

  private final ClienteFilter clienteFilter;

  /**
   * Metodo per la registrazione del filtro per il cliente.
   *
   * @return FilterRegistrationBean ClienteFilter il filtro per il cliente.
   */
  @Bean
  public FilterRegistrationBean<ClienteFilter> filterBeanCliente() {
    final FilterRegistrationBean<ClienteFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(clienteFilter);
    registrationBean.addUrlPatterns("/areaPersonale", "/areaPersonale/*", "/carrello");

    return registrationBean;
  }
}
