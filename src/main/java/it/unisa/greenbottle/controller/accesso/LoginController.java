package it.unisa.greenbottle.controller.accesso;

import it.unisa.greenbottle.controller.accesso.form.LoginForm;
import it.unisa.greenbottle.controller.accesso.util.EncryptorUtil;
import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Classe LoginController, utilizzata per la gestione del login del cliente.
 */
@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

  private static final String LOGIN_VIEW = "AccessoView/Login";

  private final ClienteDao clienteDao;
  private final SessionCliente sessionCliente;
  private final EncryptorUtil encryptorUtil;

  /**
   * Metodo per ottenere la pagina di login del cliente.
   *
   * @param loginForm form di login
   * @param model     model
   * @return pagina di login del cliente
   */
  @GetMapping
  public String get(@ModelAttribute final LoginForm loginForm, final Model model) {
    model.addAttribute("nameLogin", "/login");
    return LOGIN_VIEW;
  }

  /**
   * Metodo per effettuare il login del cliente.
   * Se l'email non esiste o la password è errata, ritorna la pagina di login.
   *
   * @param loginForm     form di login
   * @param bindingResult binding result
   * @param model         model
   * @param session       sessioneHttp
   * @return redirect all'area personale del cliente
   */
  @PostMapping
  public String post(@ModelAttribute @Valid final LoginForm loginForm,
                     final BindingResult bindingResult, final Model model,
                     final HttpSession session) {
    model.addAttribute("nameLogin", "/login");
    if (bindingResult.hasErrors()) {
      return LOGIN_VIEW;
    }

    final String email = loginForm.getEmail();
    final String password = loginForm.getPassword();

    final Optional<Cliente> c = clienteDao.findClienteByEmail(email);
    final boolean existsEmail = c.isPresent();
    model.addAttribute("existsEmail", existsEmail);
    if (!existsEmail) {
      return LOGIN_VIEW;
    }

    final String storedPassword = c.get().getPassword();
    if (!password.equals(encryptorUtil.decrypt(storedPassword))) {
      model.addAttribute("correctPassword", false);
      return LOGIN_VIEW;
    }
    model.addAttribute("correctPassword", true);

    sessionCliente.setCliente(c.get());
    session.setAttribute("sessionCliente", sessionCliente);
    return "redirect:" + "/areaPersonale";
  }
}
