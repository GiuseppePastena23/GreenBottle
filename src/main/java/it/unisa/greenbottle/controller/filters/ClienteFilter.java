package it.unisa.greenbottle.controller.filters;

import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Filtro per i clienti.
 */
@Component
@RequiredArgsConstructor
public class ClienteFilter implements Filter {

  private static final String LOGIN_PATH = "/login";

  private final SessionCliente sessionCliente;

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                       final FilterChain filterChain) throws IOException, ServletException {
    if (sessionCliente.getCliente().isPresent()) {
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      ((HttpServletResponse) servletResponse).sendRedirect(LOGIN_PATH);
    }
  }
}
