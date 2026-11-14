package it.unisa.greenbottle.controller.areapersonale;

import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import it.unisa.greenbottle.controller.areapersonale.form.DataForm;
import it.unisa.greenbottle.controller.ordine.util.OrdineWrapper;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.ordine.dao.OrdineDao;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Questa classe gestisce la visualizzazione dello storico degli ordini di un cliente.
 */
@Controller
@RequestMapping("areaPersonale/visualizzaStoricoOrdini")
@RequiredArgsConstructor
public class VisualizzaStoricoOrdiniController {

  private static final String VISUALIZZA_STORICO_ORDINI_VIEW = "AreaPersonaleView/StoricoOrdini";
  private static final String MESSAGE_ATTRIBUTE = "message";
  private static final String STATUS_ATTRIBUTE = "status";
  private static final String ORDINI_ATTRIBUTE = "ordini";
  private static final String DATA_INIZIO_PREDEFINITA = "2023-01-01";
  private static final long MILLISECONDI_AL_GIORNO = 86_400_000L;

  private final OrdineDao ordineDao;
  private final SessionCliente sessionCliente;

  /**
   * Questo metodo gestisce la richiesta GET
   * per la visualizzazione dello storico degli ordini di un cliente.
   *
   * @param dataForm            form per la data di inizio e di fine
   * @param bindingResult       bindingResult
   * @param model               model
   * @param httpServletResponse httpServletResponse
   * @param httpServletRequest  httpServletRequest
   * @return visualizzaStoricoOrdiniView
   * @throws IOException IOException
   */
  @GetMapping
  public String get(@ModelAttribute final DataForm dataForm,
                    final BindingResult bindingResult, final Model model,
                    final HttpServletResponse httpServletResponse,
                    final HttpServletRequest httpServletRequest) throws IOException {
    if (bindingResult.hasErrors()) {
      // Se c'è un errore specifico per un campo, gestisci il messaggio
      final FieldError fieldError = bindingResult.getFieldErrors().getFirst();
      return mostraErrore(model, httpServletRequest, httpServletResponse,
          HttpServletResponse.SC_BAD_REQUEST, fieldError.getDefaultMessage());
    }

    final Optional<Cliente> clienteOpt = sessionCliente.getCliente();
    if (clienteOpt.isEmpty()) {
      return "redirect:/login";
    }
    final Cliente cliente = clienteOpt.get();

    // se non è stata inserita una data di inizio, la data di inizio è 2023-01-01
    final String startDateStr = !dataForm.getStartDate().isEmpty()
        ? dataForm.getStartDate() : DATA_INIZIO_PREDEFINITA;
    // se non è stata inserita una data di fine, la data di fine è la data attuale
    final String endDateStr = !dataForm.getEndDate().isEmpty() ? dataForm.getEndDate()
        : LocalDate.ofEpochDay(System.currentTimeMillis() / MILLISECONDI_AL_GIORNO).toString();

    LocalDate startDate;
    LocalDate endDate;

    try {
      startDate = LocalDate.parse(startDateStr);
    } catch (final DateTimeException e) {
      return mostraErrore(model, httpServletRequest, httpServletResponse,
          HttpServletResponse.SC_BAD_REQUEST, "Data di inizio non valida.");
    }

    try {
      endDate = LocalDate.parse(endDateStr);
    } catch (final DateTimeException e) {
      return mostraErrore(model, httpServletRequest, httpServletResponse,
          HttpServletResponse.SC_BAD_REQUEST, "Data di fine non valida.");
    }

    if (startDate.isAfter(endDate)) {
      return mostraErrore(model, httpServletRequest, httpServletResponse,
          HttpServletResponse.SC_BAD_REQUEST, "Data iniziale successiva alla data finale.");
    }

    final List<OrdineWrapper> ordiniFinale = new ArrayList<>();
    final List<Ordine> ordiniCliente = ordineDao.findOrdineByCliente(cliente);

    for (final Ordine ordine : ordiniCliente) {
      final LocalDate dataOrdine = ordine.getData().toLocalDateTime().toLocalDate();
      if ((dataOrdine.isEqual(startDate) || dataOrdine.isAfter(startDate))
          && (dataOrdine.isEqual(endDate) || dataOrdine.isBefore(endDate))) {
        ordiniFinale.add(new OrdineWrapper(ordine, ordine.getComposizioni().stream().toList()));
      }
    }

    model.addAttribute(ORDINI_ATTRIBUTE, ordiniFinale);
    model.addAttribute("StatoSpedizione", Ordine.StatoSpedizione.class);
    return VISUALIZZA_STORICO_ORDINI_VIEW;
  }

  /**
   * Imposta gli attributi di errore e restituisce la vista dello storico ordini.
   *
   * @param model               model
   * @param httpServletRequest  richiesta HTTP
   * @param httpServletResponse risposta HTTP
   * @param status              codice di stato HTTP
   * @param message             messaggio di errore
   * @return la vista dello storico ordini
   * @throws IOException IOException
   */
  private String mostraErrore(final Model model, final HttpServletRequest httpServletRequest,
                              final HttpServletResponse httpServletResponse, final int status,
                              final String message) throws IOException {
    model.addAttribute(MESSAGE_ATTRIBUTE, message);
    model.addAttribute(STATUS_ATTRIBUTE, status);
    httpServletRequest.setAttribute(STATUS_ATTRIBUTE, status);
    httpServletRequest.setAttribute(MESSAGE_ATTRIBUTE, message);
    httpServletResponse.sendError(status, message);
    return VISUALIZZA_STORICO_ORDINI_VIEW;
  }
}
