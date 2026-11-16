package it.unisa.greenbottle.catalogoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Verifica che l'identità di {@link Prodotto} dipenda solo dall'id, come richiesto per usare
 * l'entità come chiave nelle mappe del carrello e dei wrapper.
 */
class ProdottoEqualsHashCodeTest {

  /**
   * Due istanze con lo stesso id ma campi diversi (immagine, categoria) devono risultare uguali
   * e avere lo stesso hashCode.
   */
  @Test
  void identitaBasataSullId() {
    final Prodotto primo = prodotto(1L, new byte[] {1, 2, 3}, new Categoria("Bevande"));
    final Prodotto secondo = prodotto(1L, new byte[] {9, 9}, new Categoria("Snack"));

    assertEquals(primo, secondo);
    assertEquals(primo.hashCode(), secondo.hashCode());
  }

  /**
   * Prodotti con id diversi non devono mai risultare uguali.
   */
  @Test
  void idDiversiNonSonoUguali() {
    final Prodotto primo = prodotto(1L, null, null);
    final Prodotto secondo = prodotto(2L, null, null);

    assertNotEquals(primo, secondo);
  }

  /**
   * La ricerca in mappa deve riuscire con una istanza equivalente ma distinta, senza che img e
   * categoria debbano coincidere.
   */
  @Test
  void lookupInMappaConIstanzaEquivalente() {
    final Map<Prodotto, Integer> mappa = new HashMap<>();
    mappa.put(prodotto(7L, new byte[] {1, 2, 3}, new Categoria("Bevande")), 4);

    final Prodotto chiaveRicerca = prodotto(7L, null, null);

    assertTrue(mappa.containsKey(chiaveRicerca));
    assertEquals(4, mappa.get(chiaveRicerca));
  }

  /**
   * Costruisce un prodotto con l'id indicato.
   *
   * @param id        identificativo del prodotto
   * @param img       immagine del prodotto, può essere null
   * @param categoria categoria del prodotto, può essere null
   * @return il prodotto costruito
   */
  private static Prodotto prodotto(final Long id, final byte[] img, final Categoria categoria) {
    final Prodotto prodotto =
        new Prodotto("prodotto-" + id, "descrizione", img, 1.0f, 10, categoria);
    prodotto.setId(id);
    return prodotto;
  }
}
