package it.unisa.greenbottle.controller.filters;

import it.unisa.greenbottle.controller.accesso.util.SessionAdmin;
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
 * Filtro per la gestione dell'accesso alle pagine riservate agli admin.
 */
@Component
@RequiredArgsConstructor
public class AdminFilter implements Filter {

  private static final String LOGIN_ADMIN_PATH = "/loginAdmin";

  private final SessionAdmin sessionAdmin;

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                       final FilterChain filterChain) throws IOException, ServletException {
    if (sessionAdmin.getAdmin().isPresent()) {
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      ((HttpServletResponse) servletResponse).sendRedirect(LOGIN_ADMIN_PATH);
    }
  }
}
