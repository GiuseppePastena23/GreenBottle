package it.unisa.greenbottle.controller.ordine;

import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import it.unisa.greenbottle.controller.ordine.form.OrdineForm;
import it.unisa.greenbottle.controller.ordine.util.SessionCarrello;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.areapersonale.dao.IndirizzoDao;
import it.unisa.greenbottle.storage.areapersonale.entity.Indirizzo;
import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import it.unisa.greenbottle.storage.ordine.dao.OrdineDao;
import it.unisa.greenbottle.storage.ordine.entity.Composizione;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import it.unisa.greenbottle.storage.ordine.entity.OrdineDirector;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller per la creazione di un ordine.
 */
@Controller
@RequestMapping("/ordina")
@RequiredArgsConstructor
public class CreazioneOrdineController {

  private static final String ORDINE_VIEW = "OrdineView/Checkout";
  private static final String FALLBACK_VIEW = "redirect:/";
  private static final String SUCCESS_VIEW = "redirect:/areaPersonale/visualizzaStatoOrdine";
  private static final String ERROR_VIEW = "error";
  private static final String ERROR_ATTRIBUTE = "errore";
  private static final String MESSAGE_ATTRIBUTE = "message";
  private static final String STATUS_ATTRIBUTE = "status";
  private static final String DEFAULT_ERROR_MESSAGE = "Default error message";

  private final OrdineDao ordineDao;
  private final ProdottoDao prodottoDao;
  private final IndirizzoDao indirizzoDao;
  private final SessionCliente sessionCliente;
  private final SessionCarrello sessionCarrello;
  private final MessageSource messageSource;

  /**
   * Metodo per la visualizzazione della pagina di checkout.
   *
   * @param model               modello per la vista
   * @param httpServletResponse response http
   * @return la vista di checkout
   * @throws IOException eccezione di I/O
   */
  @GetMapping
  public String get(final Model model, final HttpServletResponse httpServletResponse)
      throws IOException {
    final Optional<Cliente> clienteOptional = sessionCliente.getCliente();
    final Map<Prodotto, Integer> carrello = sessionCarrello.getRealCarrello();
    if (clienteOptional.isEmpty()) {
      return "redirect:/login";
    }
    if (carrello == null || carrello.isEmpty()) {
      model.addAttribute(ERROR_ATTRIBUTE, "Il carrello è vuoto.");
      httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il carrello è vuoto");
      return FALLBACK_VIEW;
    }

    final Cliente cliente = clienteOptional.get();
    final List<Indirizzo> indirizzi =
        indirizzoDao.findAllByCliente(cliente).orElseGet(ArrayList::new);
    model.addAttribute("indirizzi", indirizzi);
    model.addAttribute("carrello", sessionCarrello.getRealCarrello());
    return ORDINE_VIEW;
  }

  /**
   * Metodo per la creazione di un ordine.
   *
   * @param ordineForm          form per la creazione dell'ordine
   * @param model               modello per la vista
   * @param bindingResult       risultato del binding
   * @param httpServletResponse response http
   * @return la vista di errore o di successo
   * @throws IOException eccezione di I/O
   */
  @PostMapping
  @Transactional
  public String post(@ModelAttribute @Valid final OrdineForm ordineForm,
                     final BindingResult bindingResult, final Model model,
                     final HttpServletResponse httpServletResponse) throws IOException {
    final Optional<Cliente> clienteOptional = sessionCliente.getCliente();

    if (clienteOptional.isEmpty()) {
      return "redirect:/login";
    }

    final Cliente cliente = clienteOptional.get();

    if (bindingResult.hasErrors()) {
      return handleBindingErrors(bindingResult, model, httpServletResponse);
    }

    final Map<Long, Integer> prodottiUnparsed = sessionCarrello.getCarrello();

    if (prodottiUnparsed == null || prodottiUnparsed.isEmpty()) {
      model.addAttribute(ERROR_ATTRIBUTE, "Il carrello è vuoto");
      return FALLBACK_VIEW;
    }

    final Map<Prodotto, Integer> prodotti =
        buildProdotti(prodottiUnparsed, model, httpServletResponse);
    if (prodotti == null) {
      return FALLBACK_VIEW;
    }

    final float prezzoTotale = (float) prodotti.entrySet().stream()
        .mapToDouble((final Map.Entry<Prodotto, Integer> entry) ->
            entry.getKey().getPrezzo() * entry.getValue())
        .sum();
    final String numeroCarta = ordineForm.getNumeroCarta();
    final String dataScadenza = ordineForm.getDataScadenza();
    final String nomeTitolare = ordineForm.getNomeTitolare();
    final String riassuntoCarta = nomeTitolare + "/" + dataScadenza + "/" + numeroCarta.substring(
        numeroCarta.length() - 4);
    final Boolean isSupporto = ordineForm.getIsSupporto();
    final String descrizioneSupporto = ordineForm.getDescrizioneSupporto();
    final Boolean isRitiro = ordineForm.getIsRitiro();

    if (!isSupportoValido(isSupporto, descrizioneSupporto, model, httpServletResponse)) {
      return ERROR_VIEW;
    }

    final Long idIndirizzo = ordineForm.getIndirizzo();
    final Optional<Indirizzo> indirizzoOpt = indirizzoDao.findIndirizzoById(idIndirizzo);

    if (indirizzoOpt.isEmpty()) {
      model.addAttribute(STATUS_ATTRIBUTE, HttpServletResponse.SC_NOT_FOUND);
      model.addAttribute(MESSAGE_ATTRIBUTE, "Indirizzo non trovato.");
      httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Indirizzo non trovato.");
      return ERROR_VIEW;
    }
    final Indirizzo indirizzo = indirizzoOpt.get();

    final Set<Composizione> composizioni = new HashSet<>();
    for (final Map.Entry<Prodotto, Integer> entry : prodotti.entrySet()) {
      final Prodotto prod = entry.getKey();
      final int quantita = entry.getValue();
      composizioni.add(new Composizione(prod, quantita));
    }

    Ordine ordine;
    if (Boolean.TRUE.equals(isSupporto)) {
      ordine = OrdineDirector.createOrdineConSupporto(prezzoTotale, isRitiro, riassuntoCarta,
          descrizioneSupporto, indirizzo, cliente, composizioni);
    } else {
      ordine = OrdineDirector.createOrdine(prezzoTotale, isRitiro, riassuntoCarta, indirizzo,
          cliente, composizioni);
    }

    ordineDao.save(ordine);
    final Long idOrdine = ordine.getId();
    sessionCarrello.clearCarrello();
    return SUCCESS_VIEW + "?id=" + idOrdine;
  }

  /**
   * Gestisce gli errori di binding del form dell'ordine.
   *
   * @param bindingResult       risultato del binding
   * @param model               modello per la vista
   * @param httpServletResponse response http
   * @return la vista di errore
   * @throws IOException eccezione di I/O
   */
  private String handleBindingErrors(final BindingResult bindingResult, final Model model,
                                     final HttpServletResponse httpServletResponse)
      throws IOException {
    final FieldError fieldError = bindingResult.getFieldErrors().getFirst();
    final String errorCode = fieldError.getCode();
    String customMessage;
    if (errorCode.startsWith("typeMismatch")) {
      customMessage = resolveTypeMismatchMessage(errorCode, fieldError.getField());
    } else {
      customMessage = fieldError.getDefaultMessage();
    }

    model.addAttribute(MESSAGE_ATTRIBUTE, customMessage);
    model.addAttribute(STATUS_ATTRIBUTE, HttpServletResponse.SC_BAD_REQUEST);
    httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, customMessage);
    return ERROR_VIEW; // Visualizza la vista con il messaggio di errore
  }

  /**
   * Risolve il messaggio associato a un errore di tipo di un campo del form.
   *
   * @param errorCode codice dell'errore
   * @param field     campo del form
   * @return il messaggio da mostrare
   */
  private String resolveTypeMismatchMessage(final String errorCode, final String field) {
    if ("isSupporto".equals(field)) {
      return messageSource.getMessage(errorCode + ".ordineForm.isSupporto", null,
          DEFAULT_ERROR_MESSAGE, Locale.getDefault());
    }
    if ("isRitiro".equals(field)) {
      return messageSource.getMessage(errorCode + ".ordineForm.isRitiro", null,
          DEFAULT_ERROR_MESSAGE, Locale.getDefault());
    }
    return messageSource.getMessage("typeMismatch", null, DEFAULT_ERROR_MESSAGE,
        Locale.getDefault());
  }

  /**
   * Recupera dal database i prodotti presenti nel carrello.
   *
   * @param prodottiUnparsed    prodotti presenti nel carrello (id e quantità)
   * @param model               modello per la vista
   * @param httpServletResponse response http
   * @return i prodotti recuperati, oppure null se un prodotto non è disponibile
   * @throws IOException eccezione di I/O
   */
  private Map<Prodotto, Integer> buildProdotti(final Map<Long, Integer> prodottiUnparsed,
                                               final Model model,
                                               final HttpServletResponse httpServletResponse)
      throws IOException {
    final Map<Long, Prodotto> prodottiPerId = prodottoDao.findAllById(prodottiUnparsed.keySet())
        .stream()
        .collect(Collectors.toMap(Prodotto::getId, Function.identity()));
    final Map<Prodotto, Integer> prodotti = new HashMap<>();
    for (final Map.Entry<Long, Integer> entry : prodottiUnparsed.entrySet()) {
      final Prodotto p = prodottiPerId.get(entry.getKey());
      if (p == null) {
        continue;
      }
      final int quantita = entry.getValue();
      if (quantita > p.getQuantita()) {
        model.addAttribute(ERROR_ATTRIBUTE,
            "Prodotto: " + p.getNome() + " non disponibile in quantità richiesta.");
        httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Prodotto" + p.getNome() + " non disponibile in quantità richiesta");
        return null;
      }
      prodotti.put(p, quantita);
    }
    return prodotti;
  }

  /**
   * Verifica la coerenza tra la richiesta di supporto e la sua descrizione.
   *
   * @param isSupporto          indica se è stato richiesto il supporto aggiuntivo
   * @param descrizioneSupporto descrizione del supporto richiesto
   * @param model               modello per la vista
   * @param httpServletResponse response http
   * @return true se la richiesta di supporto è valida, false altrimenti
   * @throws IOException eccezione di I/O
   */
  private boolean isSupportoValido(final Boolean isSupporto, final String descrizioneSupporto,
                                   final Model model, final HttpServletResponse httpServletResponse)
      throws IOException {
    if (Boolean.TRUE.equals(isSupporto) && descrizioneSupporto.isBlank()) {
      model.addAttribute(ERROR_ATTRIBUTE, "Descrizione supporto non inserita.");
      httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Descrizione supporto non inserita.");
      return false;
    }
    if (!Boolean.TRUE.equals(isSupporto) && !descrizioneSupporto.isBlank()) {
      model.addAttribute(ERROR_ATTRIBUTE,
          "Descrizione presente, ma non si desidera richiedere supporto aggiuntivo.");
      httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Descrizione presente, ma non si desidera richiedere supporto aggiuntivo.");
      return false;
    }
    return true;
  }
}
