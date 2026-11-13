package it.unisa.greenbottle.dataJPATests;

import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.areapersonale.dao.IndirizzoDao;
import it.unisa.greenbottle.storage.areapersonale.entity.Indirizzo;
import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import it.unisa.greenbottle.storage.ordine.dao.OrdineDao;
import it.unisa.greenbottle.storage.ordine.entity.Composizione;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import it.unisa.greenbottle.storage.ordine.entity.OrdineDirector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Testa la creazione di un ordine e la sua persistenza nel database.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrdineTest {

  @Autowired
  private OrdineDao ordineDao;

  @Autowired
  private IndirizzoDao indirizzoDao;

  @Autowired
  private ProdottoDao prodottoDao;

  @Autowired
  private ClienteDao clienteDao;

  private final Cliente clienteTest =
      new Cliente("mario.rossi@gmail.com", "asdfASDF1234!", "Mario", "Rossi", 0, 0, null, null);

  private final Indirizzo indirizzoTest =
      new Indirizzo("Via Roma", 5, "Potenza", "PZ", "85100", clienteTest);

  private final Categoria categoriaTest = new Categoria("Bevande");
  private final Prodotto prodottoTest =
      new Prodotto("Acqua minerale", "Acqua minerale naturale", null, 0.5f, 1000, categoriaTest);

  private final Prodotto[] arrayProdottiTest = new Prodotto[] {
      prodottoTest,
      new Prodotto("Acqua frizzante", "Acqua frizzante naturale", null, 0.5f, 1000, categoriaTest),
      new Prodotto("Acqua tonica", "Acqua tonica naturale", null, 0.5f, 1000, categoriaTest),
      new Prodotto("Acqua gassata", "Acqua gassata naturale", null, 0.5f, 1000, categoriaTest),
      new Prodotto("Acqua liscia", "Acqua liscia naturale", null, 0.5f, 1000, categoriaTest),
      new Prodotto("Fantasia", "Fantasia naturale", null, 0.5f, 1000, categoriaTest),
      new Prodotto("Acqua intostata", "Acqua intostata naturale", null, 0.5f, 1000, categoriaTest),
      new Prodotto("Aranzata", "Aranzata naturale", null, 0.5f, 1000, categoriaTest)
  };
  private final List<Prodotto> prodottiTest = Arrays.asList(arrayProdottiTest);
  private final Set<Composizione> composizioniTest = new HashSet<>();

  @Test
  void createOrdine() {

    clienteDao.save(clienteTest);
    indirizzoDao.save(indirizzoTest);
    prodottoDao.save(prodottoTest);

    composizioniTest.add(new Composizione(prodottoTest, 2));

    float prezzoTotale = (float) composizioniTest.stream()
        .mapToDouble(c -> c.getProdotto().getPrezzo() * c.getQuantita())
        .sum();

    Ordine ordineTest = OrdineDirector.createOrdineConSupporto(
        prezzoTotale,
        false,
        "1234-5678-9876-5432",
        "Supporto Aggiuntivo",
        indirizzoTest,
        clienteTest,
        composizioniTest);

    ordineDao.save(ordineTest);

    Optional<Ordine> o2 = ordineDao.findById(ordineTest.getId());

    assertTrue(o2.isPresent());
    assertEquals(ordineTest, o2.get());
  }

  @Test
  void createAndVerifyOrdineAndComposizione() {
    clienteDao.save(clienteTest);
    indirizzoDao.save(indirizzoTest);
    prodottoDao.saveAll(prodottiTest);

    composizioniTest.add(new Composizione(prodottoTest, 2));
    composizioniTest.add(new Composizione(prodottiTest.get(1), 100));

    float prezzoTotale = (float) composizioniTest.stream()
        .mapToDouble(c -> c.getProdotto().getPrezzo() * c.getQuantita())
        .sum();

    Ordine ordineTest = OrdineDirector.createOrdineConSupporto(
        prezzoTotale,
        false,
        "1234-5678-9876-5432",
        "Supporto Aggiuntivo",
        indirizzoTest,
        clienteTest,
        composizioniTest);

    ordineDao.save(ordineTest);

    Optional<Ordine> retrievedOrdine = ordineDao.findById(ordineTest.getId());
    assertTrue(retrievedOrdine.isPresent());

    Ordine savedOrdine = retrievedOrdine.get();

    assertFalse(savedOrdine.getComposizioni().isEmpty());
    assertEquals(2, savedOrdine.getComposizioni().size());

    for (Composizione composizione : savedOrdine.getComposizioni()) {
      Prodotto prodotto = composizione.getProdotto();
      assertTrue(prodottiTest.contains(prodotto));
    }
  }
}