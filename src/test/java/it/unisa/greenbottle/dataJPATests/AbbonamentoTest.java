package it.unisa.greenbottle.dataJPATests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import it.unisa.greenbottle.storage.abbonamento.dao.AbbonamentoDao;
import it.unisa.greenbottle.storage.abbonamento.dao.DisposizioneDao;
import it.unisa.greenbottle.storage.abbonamento.entity.Abbonamento;
import it.unisa.greenbottle.storage.abbonamento.entity.Disposizione;
import it.unisa.greenbottle.storage.catalogo.dao.CategoriaDao;
import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test per la classe Abbonamento.
 */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AbbonamentoTest {
  @Autowired
  private AbbonamentoDao abbonamentoDao;
  @Autowired
  private DisposizioneDao disposizioneDao;
  @Autowired
  private ProdottoDao prodottoDao;
  @Autowired
  private CategoriaDao categoriaDao;

  @Test
  void abbonamentiTier() {
    Abbonamento abbonamento1 = new Abbonamento(Abbonamento.TipoAbbonamento.BRONZE,
        Abbonamento.RinnovoAbbonamento.SEMESTRALE, Abbonamento.FrequenzaAbbonamento.MENSILE, 100f);
    Abbonamento abbonamento2 = new Abbonamento(Abbonamento.TipoAbbonamento.SILVER,
        Abbonamento.RinnovoAbbonamento.BIMESTRALE, Abbonamento.FrequenzaAbbonamento.SETTIMANALE,
        20f);
    Abbonamento abbonamento3 =
        new Abbonamento(Abbonamento.TipoAbbonamento.GOLD, Abbonamento.RinnovoAbbonamento.MENSILE,
            Abbonamento.FrequenzaAbbonamento.GIORNALIERO, 5f);
    abbonamentoDao.save(abbonamento1);
    abbonamentoDao.save(abbonamento2);
    abbonamentoDao.save(abbonamento3);

    Categoria categoriaDefault = new Categoria("Bevande gassate");
    categoriaDao.save(categoriaDefault);
    Prodotto prodotto1 = new Prodotto("Fanta", "Bevanda", null, 2.5f, 100, null);
    Prodotto prodotto2 = new Prodotto("Coca Cola", "Bevanda", null, 1.5f, 100, null);
    Prodotto prodotto3 = new Prodotto("Sprite", "Bevanda", null, 2.0f, 100, categoriaDefault);
    prodottoDao.save(prodotto1);
    prodottoDao.save(prodotto2);
    prodottoDao.save(prodotto3);

    disposizioneDao.save(new Disposizione(1, prodotto1, abbonamento1));
    disposizioneDao.save(new Disposizione(2, prodotto2, abbonamento1));
    disposizioneDao.save(new Disposizione(3, prodotto3, abbonamento1));
    disposizioneDao.save(new Disposizione(2, prodotto1, abbonamento2));


    List<Abbonamento> abbonamenti = abbonamentoDao.findAbbonamentoByTipo(
        Abbonamento.TipoAbbonamento.BRONZE);
    HashMap<Abbonamento, List<Disposizione>> disposizione =
        new HashMap<Abbonamento, List<Disposizione>>();
    for (Abbonamento abbonamento : abbonamenti) {
      disposizione.put(abbonamento, disposizioneDao.findDisposizioneByAbbonamento(abbonamento));
    }

    assertFalse(disposizione.isEmpty());
    assertEquals(1, disposizione.size());
    assertEquals(3, disposizione.get(abbonamento1).size());
  }
}
