package it.unisa.greenbottle.controller.accesso;

import it.unisa.greenbottle.controller.accesso.form.LoginForm;
import it.unisa.greenbottle.controller.accesso.util.EncryptorUtil;
import it.unisa.greenbottle.controller.accesso.util.SessionAdmin;
import it.unisa.greenbottle.storage.accesso.dao.AdminDao;
import it.unisa.greenbottle.storage.accesso.entity.Admin;
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
 * Classe LoginAdminController, utilizzata per la gestione del login dell'admin.
 */
@Controller
@RequestMapping("/loginAdmin")
@RequiredArgsConstructor
public class LoginAdminController {

  private static final String LOGIN_VIEW = "AccessoView/LoginAdmin";

  private final AdminDao adminDao;
  private final SessionAdmin sessionAdmin;
  private final EncryptorUtil encryptorUtil;

  /**
   * Metodo per ottenere la pagina di login dell'admin.
   *
   * @param loginForm form di login
   * @param model     model
   * @return pagina di login dell'admin
   */
  @GetMapping
  public String get(@ModelAttribute final LoginForm loginForm, final Model model) {
    model.addAttribute("nameLogin", "/loginAdmin");
    return LOGIN_VIEW;
  }

  /**
   * Metodo per effettuare il login dell'admin.
   * Se l'email non esiste o la password è errata, ritorna la pagina di login.
   *
   * @param loginForm     form di login
   * @param bindingResult binding result
   * @param model         model
   * @param session       sessioneHttp
   * @return redirect alla dashboard dell'admin
   */
  @PostMapping
  public String post(@ModelAttribute @Valid final LoginForm loginForm,
                     final BindingResult bindingResult, final Model model,
                     final HttpSession session) {
    model.addAttribute("nameLogin", "/loginAdmin");
    if (bindingResult.hasErrors()) {
      return LOGIN_VIEW;
    }

    final String email = loginForm.getEmail();
    final String password = loginForm.getPassword();

    final Optional<Admin> a = adminDao.findAdminByEmail(email);
    final boolean existsEmail = a.isPresent();
    model.addAttribute("existsEmail", existsEmail);
    if (!existsEmail) {
      return LOGIN_VIEW;
    }

    final String storedPassword = a.get().getPassword();
    if (!password.equals(encryptorUtil.decrypt(storedPassword))) {
      model.addAttribute("correctPassword", false);
      return LOGIN_VIEW;
    }
    model.addAttribute("correctPassword", true);

    sessionAdmin.setAdmin(a.get());
    session.setAttribute("admin", sessionAdmin);
    return "redirect:" + "/admin";
  }
}
