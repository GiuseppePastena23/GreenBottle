package it.unisa.greenbottle.controller.ordine;

import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import it.unisa.greenbottle.controller.ordine.util.OrdineWrapper;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.ordine.dao.ComposizioneDao;
import it.unisa.greenbottle.storage.ordine.dao.OrdineDao;
import it.unisa.greenbottle.storage.ordine.entity.Composizione;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Questa classe è un controller che si occupa
 * di gestire la visualizzazione dello stato di un ordine.
 */
@Controller
@RequestMapping("/areaPersonale/visualizzaStatoOrdine")
@RequiredArgsConstructor
public class VisualizzaStatoOrdineController {

  private static final String VISUALIZZA_STATO_ORDINE_VIEW = "OrdineView/VisualizzaStatoOrdine";
  private static final String ERROR_VIEW = "error";
  private static final String MESSAGE_ATTRIBUTE = "message";
  private static final String STATUS_ATTRIBUTE = "status";
  private static final String ORDINE_ATTRIBUTE = "ordine";
  private static final String STATO_SPEDIZIONE_ATTRIBUTE = "StatoSpedizione";

  private final OrdineDao ordineDao;
  private final ComposizioneDao composizioneDao;
  private final SessionCliente sessionCliente;

  /**
   * Questo metodo si occupa di gestire la richiesta GET per visualizzare lo stato di un ordine.
   *
   * @param id                  id dell'ordine da visualizzare
   * @param model               modello dati
   * @param httpServletResponse risposta http
   * @return stringa che rappresenta la view da visualizzare
   * @throws IOException eccezione di input/output
   */
  @GetMapping
  public String get(@RequestParam final Long id, final Model model,
                    final HttpServletResponse httpServletResponse) throws IOException {
    final Optional<Cliente> clienteOpt = sessionCliente.getCliente();
    if (clienteOpt.isEmpty()) {
      return "redirect:/login";
    }
    final Cliente cliente = clienteOpt.get();

    if (id < 0) {
      return mostraErrore(model, httpServletResponse, HttpServletResponse.SC_BAD_REQUEST,
          "Id non valido.");
    }

    final Optional<Ordine> optOrdine = ordineDao.findOrdineById(id);
    if (optOrdine.isEmpty()) {
      return mostraErrore(model, httpServletResponse, HttpServletResponse.SC_NOT_FOUND,
          "Ordine non presente.");
    }

    final Ordine ordine = optOrdine.get();
    if (!ordine.getCliente().equals(cliente)) {
      return mostraErrore(model, httpServletResponse, HttpServletResponse.SC_FORBIDDEN,
          "Ordine non accessibile.");
    }

    final List<Composizione> composizione = composizioneDao.findComposizioneByOrdine(ordine);
    final OrdineWrapper ordineWrapper = new OrdineWrapper(ordine, composizione);

    model.addAttribute(ORDINE_ATTRIBUTE, ordineWrapper);
    model.addAttribute(STATO_SPEDIZIONE_ATTRIBUTE, Ordine.StatoSpedizione.class);

    return VISUALIZZA_STATO_ORDINE_VIEW;
  }

  /**
   * Questo metodo si occupa di gestire la richiesta POST per eliminare un ordine.
   *
   * @param id id dell'ordine da eliminare
   * @return stringa che rappresenta la view da visualizzare
   */
  @PostMapping
  public String post(@RequestParam final Long id) {
    final Optional<Cliente> clienteOpt = sessionCliente.getCliente();
    if (clienteOpt.isEmpty()) {
      return "redirect:/login";
    }
    final Cliente cliente = clienteOpt.get();

    final Optional<Ordine> optOrdine = ordineDao.findOrdineById(id);
    if (optOrdine.isEmpty()) {
      return ERROR_VIEW;
    }
    final Ordine ordine = optOrdine.get();
    if (!ordine.getCliente().equals(cliente)) {
      return ERROR_VIEW;
    }

    composizioneDao.deleteAll(composizioneDao.findComposizioneByOrdine(ordine));
    ordineDao.delete(ordine);

    return "redirect:/areaPersonale/visualizzaStoricoOrdini";
  }

  /**
   * Imposta gli attributi di errore e restituisce la vista di errore.
   *
   * @param model               modello dati
   * @param httpServletResponse risposta http
   * @param status              codice di stato HTTP
   * @param message             messaggio di errore
   * @return la vista di errore
   * @throws IOException eccezione di input/output
   */
  private String mostraErrore(final Model model, final HttpServletResponse httpServletResponse,
                              final int status, final String message) throws IOException {
    model.addAttribute(STATUS_ATTRIBUTE, status);
    model.addAttribute(MESSAGE_ATTRIBUTE, message);
    httpServletResponse.sendError(status, message);
    return ERROR_VIEW;
  }
}
