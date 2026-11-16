package it.unisa.greenbottle.controller.accesso;

import it.unisa.greenbottle.controller.accesso.form.RegistrazioneForm;
import it.unisa.greenbottle.controller.accesso.util.EncryptorUtil;
import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Questa classe gestisce la registrazione di un nuovo cliente.
 */
@Controller
@RequestMapping("/registrazione")
@RequiredArgsConstructor
public class RegistrazioneController {

  private static final String REGISTRAZIONE_VIEW = "AccessoView/Registrazione";
  private static final String LOGIN_PATH = "/login";
  private static final String ERROR_VIEW = "error";
  private static final String MESSAGE_ATTRIBUTE = "message";
  private static final String STATUS_ATTRIBUTE = "status";
  private static final String ERRORE_ATTRIBUTE = "errore";

  private final ClienteDao clienteDao;
  private final EncryptorUtil encryptorUtil;

  /**
   * Metodo per ottenere la pagina di registrazione.
   *
   * @param registrazioneForm form di registrazione
   * @return pagina di registrazione
   */
  @GetMapping
  public String get(@ModelAttribute final RegistrazioneForm registrazioneForm) {
    return REGISTRAZIONE_VIEW;
  }

  /**
   * Metodo per effettuare la registrazione di un nuovo cliente.
   * In caso di errore ritorna la pagina di registrazione.
   *
   * @param registrazioneForm   form di registrazione
   * @param bindingResult       binding result
   * @param model               model
   * @param httpServletResponse response
   * @return redirect alla pagina di login
   * @throws IOException se ci sono errori
   */
  @PostMapping
  public String post(@ModelAttribute @Valid final RegistrazioneForm registrazioneForm,
                     final BindingResult bindingResult, final Model model,
                     final HttpServletResponse httpServletResponse) throws IOException {

    if (bindingResult.hasErrors()) {
      // Se c'è un errore specifico per un campo, gestisci il messaggio
      final FieldError fieldError = bindingResult.getFieldErrors().getFirst();
      model.addAttribute(MESSAGE_ATTRIBUTE, fieldError.getDefaultMessage());
      model.addAttribute(STATUS_ATTRIBUTE, HttpServletResponse.SC_BAD_REQUEST);
      httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
          fieldError.getDefaultMessage());
      return ERROR_VIEW; // Visualizza la vista con il messaggio di errore
    }

    final String encryptedPassword = encryptorUtil.encrypt(registrazioneForm.getPassword());
    final String email = registrazioneForm.getEmail();
    final String nome = registrazioneForm.getNome();
    final String cognome = registrazioneForm.getCognome();

    if (clienteDao.findClienteByEmail(email).isPresent()) {
      model.addAttribute(ERRORE_ATTRIBUTE, "Email gia' registrata.");
      return REGISTRAZIONE_VIEW;
    }

    final Cliente c = new Cliente(email, encryptedPassword, nome, cognome, 0, 0, null, null);
    clienteDao.save(c);

    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    return "redirect:" + LOGIN_PATH;
  }
}
