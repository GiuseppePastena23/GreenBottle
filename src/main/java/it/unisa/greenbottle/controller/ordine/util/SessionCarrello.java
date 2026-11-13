package it.unisa.greenbottle.controller.ordine.util;

import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Classe che rappresenta il carrello dell'utente in sessione.
 */
@Component
@SessionScope
@Getter
@RequiredArgsConstructor
public class SessionCarrello {

  private final Map<Long, Integer> carrello = new HashMap<>();

  private final ProdottoDao prodottoDao;

  /**
   * Restituisce il carrello filtrato.
   *
   * @return il carrello filtrato
   */
  public Map<Prodotto, Integer> getRealCarrello() {
    if (carrello.isEmpty()) {
      return new HashMap<>();
    }

    final Map<Prodotto, Integer> filteredCarrello = new HashMap<>();
    for (final Prodotto prodotto : prodottoDao.findAllById(carrello.keySet())) {
      filteredCarrello.put(prodotto, carrello.get(prodotto.getId()));
    }

    return filteredCarrello;
  }

  /**
   * Aggiunge un prodotto al carrello.
   *
   * @param prodotto id del prodotto
   * @param quantita quantità del prodotto
   * @return true se l'operazione è andata a buon fine, false altrimenti
   */
  public boolean addToCarrello(final Long prodotto, final Integer quantita) {
    if (prodotto == null || quantita == null || quantita <= 0) {
      return false;
    }
    carrello.put(prodotto, carrello.getOrDefault(prodotto, 0) + quantita);
    return true;
  }

  /**
   * Rimuove un prodotto dal carrello.
   *
   * @param prodotto id del prodotto
   * @return true se l'operazione è andata a buon fine, false altrimenti
   */
  public boolean removeFromCarrello(final Long prodotto) {
    if (prodotto == null || !carrello.containsKey(prodotto)) {
      return false;
    }
    carrello.remove(prodotto);
    return true;
  }

  /**
   * Svuota il carrello.
   *
   * @return true se l'operazione è andata a buon fine, false altrimenti
   */
  public boolean clearCarrello() {
    if (!carrello.isEmpty()) {
      carrello.clear();
      return true;
    }
    return false;
  }
}
