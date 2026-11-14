package it.unisa.greenbottle.controller.filters;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe di configurazione del filtro per l'amministratore.
 */
@Configuration
@RequiredArgsConstructor
public class AdminConfigFilter {

  private final AdminFilter adminFilter;

  /**
   * Metodo per la creazione del filtro per l'amministratore.
   *
   * @return il filtro per l'amministratore.
   */
  @Bean
  public FilterRegistrationBean<AdminFilter> filterBeanAdmin() {
    final FilterRegistrationBean<AdminFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(adminFilter);
    registrationBean.addUrlPatterns("/admin", "/admin/*");

    return registrationBean;
  }
}
