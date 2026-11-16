package it.unisa.greenbottle.controller.catalogo;

import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller per la gestione delle richieste relative ai prodotti.
 */
@Controller
@RequestMapping("/prodotto")
@RequiredArgsConstructor
public class ProdottoController {

  private final ProdottoDao prodottoDao;

  /**
   * Metodo per ottenere un prodotto dal catalogo.
   *
   * @param id id del prodotto da cercare
   * @return ResponseEntity contenente il prodotto cercato
   */
  @GetMapping
  public ResponseEntity<Object> get(@RequestParam final Long id) {
    final Optional<Prodotto> prodotto = prodottoDao.findProdottoById(id);
    if (prodotto.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("Errore: Prodotto non trovato");
    }
    return ResponseEntity.ok(prodotto.get());
  }
}
