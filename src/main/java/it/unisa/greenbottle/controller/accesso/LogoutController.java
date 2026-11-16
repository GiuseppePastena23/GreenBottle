package it.unisa.greenbottle.controller.accesso;

import it.unisa.greenbottle.controller.accesso.util.SessionAdmin;
import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Questa classe gestisce il logout dell'utente e dell'amministratore.
 */
@Controller
@RequestMapping("/logout")
@RequiredArgsConstructor
public class LogoutController {

  private final SessionCliente sessionCliente;
  private final SessionAdmin sessionAdmin;

  /**
   * Questo metodo permette di effettuare il logout del cliente e dell'amministratore.
   *
   * @param session sessione del cliente
   * @return redirect alla home
   */
  @GetMapping
  public String get(final HttpSession session) {
    sessionCliente.emptyCliente();
    sessionAdmin.emptyAdmin();

    session.setAttribute("sessionCliente", sessionCliente);
    session.setAttribute("sessionAdmin", sessionAdmin);
    return "redirect:/";
  }
}
