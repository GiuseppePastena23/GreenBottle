package it.unisa.greenbottle.controller.abbonamento;

import it.unisa.greenbottle.controller.abbonamento.form.AbbonamentoForm;
import it.unisa.greenbottle.controller.abbonamento.util.AbbonamentoWrapper;
import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import it.unisa.greenbottle.storage.abbonamento.dao.AbbonamentoDao;
import it.unisa.greenbottle.storage.abbonamento.dao.DisposizioneDao;
import it.unisa.greenbottle.storage.abbonamento.entity.Abbonamento;
import it.unisa.greenbottle.storage.abbonamento.entity.Disposizione;
import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller per la gestione degli abbonamenti.
 */
@Controller
@RequestMapping("/abbonamento")
@RequiredArgsConstructor
public class AbbonamentoController {

  private static final String ABBONAMENTO_VIEW = "AbbonamentoView/Abbonamenti";
  private static final String CHECKOUT_ABBONAMENTO_VIEW = "AbbonamentoView/CheckoutAbbonamento";
  private static final String ABBONAMENTO_ATTRIBUTE = "abbonamento";
  private static final String ERROR_VIEW = "error";

  private final AbbonamentoDao abbonamentoDao;
  private final SessionCliente sessionCliente;
  private final DisposizioneDao disposizioneDao;
  private final ClienteDao clienteDao;

  /**
   * Mostro la lista degli abbonamenti disponibili e i dettagli dell'abbonamento selezionato.
   *
   * @param tipo  Bronze, Silver, Gold
   * @param id    id dell'abbonamento selezionato per la sottoscrizione
   * @param model model
   * @return la vista degli abbonamenti
   */
  @GetMapping
  public String get(
      @RequestParam(name = ABBONAMENTO_ATTRIBUTE, required = false) final String tipo,
      @RequestParam(name = "sottoscrivi", required = false) final Long id, final Model model) {
    if (id != null) {
      final Optional<Abbonamento> abbonamentoOpt = abbonamentoDao.findAbbonamentoById(id);
      if (abbonamentoOpt.isEmpty()) {
        model.addAttribute("message", "Abbonamento non trovato.");
        model.addAttribute("status", HttpServletResponse.SC_NOT_FOUND);
        return ERROR_VIEW;
      }
      model.addAttribute(ABBONAMENTO_ATTRIBUTE, abbonamentoOpt.get());
      return CHECKOUT_ABBONAMENTO_VIEW;
    }

    if (tipo != null && !tipo.isBlank()) {
      if (!tipo.equalsIgnoreCase("BRONZE")
          && !tipo.equalsIgnoreCase("SILVER")
          && !tipo.equalsIgnoreCase("GOLD")) {
        throw new IllegalArgumentException("Tipo di abbonamento non valido: " + tipo);
      }

      final Abbonamento.TipoAbbonamento tipoAbbonamento =
          Abbonamento.TipoAbbonamento.valueOf(tipo.toUpperCase());
      final List<Abbonamento> abbonamenti = abbonamentoDao.findAbbonamentoByTipo(tipoAbbonamento);
      final Map<Abbonamento, List<Disposizione>> disposizioni =
          disposizioneDao.findDisposizioneByAbbonamentoIn(abbonamenti).stream()
              .collect(Collectors.groupingBy(Disposizione::getAbbonamento));
      final List<AbbonamentoWrapper> abbonamentiFinale = new ArrayList<>();
      for (final Abbonamento abbonamento : abbonamenti) {
        abbonamentiFinale.add(new AbbonamentoWrapper(abbonamento,
            disposizioni.getOrDefault(abbonamento, List.of())));
      }

      model.addAttribute("abbonamenti", abbonamentiFinale);
    } else {
      model.addAttribute("abbonamenti", List.of());
    }

    return ABBONAMENTO_VIEW;
  }

  /**
   * Sottoscrizione dell'abbonamento.
   *
   * @param abbonamentoForm     form dell'abbonamento
   * @param bindingResult       binding result
   * @param model               model
   * @param httpServletResponse response
   * @return la vista dell'abbonamento
   * @throws IOException IOException
   */
  @PostMapping
  public String post(@ModelAttribute @Valid final AbbonamentoForm abbonamentoForm,
                     final BindingResult bindingResult, final Model model,
                     final HttpServletResponse httpServletResponse)
      throws IOException {
    if (bindingResult.hasErrors()) {
      // Se c'è un errore specifico per un campo, gestisci il messaggio
      final FieldError fieldError = bindingResult.getFieldErrors().getFirst();
      model.addAttribute("message", fieldError.getDefaultMessage());
      model.addAttribute("status", HttpServletResponse.SC_BAD_REQUEST);
      httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
          fieldError.getDefaultMessage());
      return ERROR_VIEW; // Visualizza la vista con il messaggio di errore
    }

    final Optional<Cliente> clienteOpt = sessionCliente.getCliente();
    if (clienteOpt.isEmpty()) {
      model.addAttribute("errore", "Non loggato.");
      return "redirect:/login";
    }

    final Long idAbbonamento = abbonamentoForm.getId();
    final Optional<Abbonamento> abbonamentoOptional =
        abbonamentoDao.findAbbonamentoById(idAbbonamento);
    if (abbonamentoOptional.isEmpty()) {
      httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Abbonamento non trovato.");
      return ERROR_VIEW;
    }

    final Abbonamento abbonamento = abbonamentoOptional.get();
    final Cliente cliente = clienteOpt.get();

    cliente.setAbbonamento(abbonamento);
    cliente.setSottoscrizione(new Timestamp(System.currentTimeMillis()));
    clienteDao.save(cliente);

    model.addAttribute(ABBONAMENTO_ATTRIBUTE, abbonamento.getId());

    if (model.containsAttribute(ABBONAMENTO_ATTRIBUTE)) {
      return "redirect:/areaPersonale";
    } else {
      httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND,
          "Errore nel sottoscrivere l'abbonamento.");
      return ERROR_VIEW;
    }
  }
}
