package it.unisa.greenbottle.controller.ordine;

import it.unisa.greenbottle.controller.ordine.form.ProdottoForm;
import it.unisa.greenbottle.controller.ordine.util.SessionCarrello;
import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Questa classe gestisce la richiesta di aggiungere un prodotto al carrello.
 */
@Controller
@RequiredArgsConstructor
public class AggiungiAlCarrelloController {

  private static final String ERROR_VIEW = "error";
  private static final String CATALOGO_REDIRECT = "redirect:/catalogo";

  private final ProdottoDao prodottoDao;
  private final SessionCarrello sessionCarrello;

  /**
   * Questo metodo gestisce la richiesta POST di aggiungere un prodotto al carrello.
   *
   * @param prodottoForm il form del prodotto da aggiungere al carrello
   * @return una stringa che rappresenta la pagina a cui reindirizzare
   */
  @PostMapping("/aggiungi-al-carrello")
  public String post(@ModelAttribute @Valid final ProdottoForm prodottoForm) {
    final Optional<Prodotto> prodottoOpt =
        prodottoDao.findProdottoById(prodottoForm.getIdProdotto());
    if (prodottoOpt.isEmpty()) {
      return ERROR_VIEW;
    }

    final Prodotto prod = prodottoOpt.get();
    if (prodottoForm.getQuantita() > prod.getQuantita()) {
      return ERROR_VIEW;
    }

    sessionCarrello.addToCarrello(prod.getId(), prodottoForm.getQuantita());
    return CATALOGO_REDIRECT;
  }
}
