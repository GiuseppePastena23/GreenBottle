package it.unisa.greenbottle.ordineTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.unisa.greenbottle.controller.accesso.util.SessionCliente;
import it.unisa.greenbottle.controller.ordine.form.OrdineForm;
import it.unisa.greenbottle.controller.ordine.util.SessionCarrello;
import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.areapersonale.dao.IndirizzoDao;
import it.unisa.greenbottle.storage.areapersonale.entity.Indirizzo;
import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import it.unisa.greenbottle.storage.ordine.dao.ComposizioneDao;
import it.unisa.greenbottle.storage.ordine.dao.OrdineDao;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Testa la funzionalità di creazione di un ordine.
 */
@AutoConfigureMockMvc
@SpringBootTest
class CreazioneOrdineTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private OrdineForm ordineForm;

  @MockitoBean
  private SessionCarrello sessionCarrello;

  @MockitoBean
  private ProdottoDao prodottoDao;

  @MockitoBean
  private ClienteDao clienteDao;

  @MockitoBean
  private OrdineDao ordineDao;
  @MockitoBean
  private ComposizioneDao composizioneDao;

  @MockitoBean
  private SessionCliente sessionCliente;

  @MockitoBean
  private IndirizzoDao indirizzoDao;

  @Test
  void nomeNonValido() throws Exception {
    testCreazioneOrdine("A1", "5032123166322313", "02/26", "123", "1", "false", "false",
        Optional.of("asd"),
        status().isBadRequest(), "Nome del titolare della carta non valido.");
  }

  @Test
  void numeroNonValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "00000000000000000", "02/26", "123", "1", "false", "false",
        Optional.of("asd"), status().isBadRequest(), "Numero di carta non valido.");
  }

  @Test
  void dataNonValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "99/99", "123", "1", "false", "false",
        Optional.of("asd"), status().isBadRequest(), "Data di scadenza della carta non valida.");
  }

  @Test
  void cvvNonValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "ABC", "1", "false", "false",
        Optional.of(""), status().isBadRequest(), "cvv non valido.");
  }

  @Test
  void indirizzoNonValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "-12", "false", "false",
        Optional.of("asd"), status().isBadRequest(), "Indirizzo non valido.");
  }

  @Test
  void indirizzoNonPresente() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "7355608", "false",
        "false",
        Optional.of(""), status().isNotFound(), "Indirizzo non trovato.");
  }

  @Test
  void isSupportoNonValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "1", "false", "maybe",
        Optional.of("Consegna al primo piano"), status().isBadRequest(),
        "Errata selezione dell opzione di richiesta supporto aggiuntivo.");
  }

  @Test
  void isRitiroNonValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "1", "maybe", "true",
        Optional.of("Consegna al primo piano"), status().isBadRequest(),
        "Errata selezione dell opzione di ritiro.");
  }

  @Test
  void descrizioneNonValida() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "1", "false", "true",
        Optional.of(("a").repeat(320)), status().isBadRequest(),
        "Descrizione supporto troppo lunga.");
  }

  @Test
  void descrizioneNonPresente() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "1", "false", "true",
        Optional.of(""), status().isBadRequest(), "Descrizione supporto non inserita.");
  }

  @Test
  void descrizionePresenteIsSupportoFalso() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "1", "false", "false",
        Optional.of("Consegna al primo piano"), status().isBadRequest(),
        "Descrizione presente, ma non si desidera richiedere supporto aggiuntivo.");
  }

  @Test
  void ordineSupportoValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "1", "false", "true",
        Optional.of("Consegna al primo piano"), status().is3xxRedirection(), null);
  }

  @Test
  void ordineValido() throws Exception {
    testCreazioneOrdine("Luigi Rossi", "5032123166322313", "02/26", "123", "1", "false", "false",
        Optional.of(""), status().is3xxRedirection(), null);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private void testCreazioneOrdine(String nomeTitolare, String numeroCarta, String dataScadenza,
                                   String cvv, String indirizz0, String isRitiro, String isSupporto,
                                   Optional<String> descrizioneSupporto,
                                   ResultMatcher resultmatcher,
                                   String expectedMessage)
      throws Exception {
    Cliente cliente = new Cliente();

    clienteDao.save(cliente);
    final Ordine ordine = new Ordine();
    final Indirizzo indirizzo = new Indirizzo();

    Categoria categoria = new Categoria("Bevande");
    Prodotto prodotto1 = new Prodotto(
        "Prodotto1", "Prodotto 1", null, 10.0f, 1000, categoria
    );
    Prodotto prodotto2 = new Prodotto(
        "Prodotto2", "Prodotto 2", null, 5.0f, 1000, categoria
    );
    prodotto1.setId(1L);
    prodotto2.setId(2L);

    when(prodottoDao.findAllById(any()))
        .thenReturn(List.of(prodotto1, prodotto2));

    prodottoDao.save(prodotto1);
    prodottoDao.save(prodotto2);

    Long idIndirizzo = Long.valueOf(indirizz0);

    Map<Long, Integer> carrello = new HashMap<>();
    carrello.put(prodotto1.getId(), 10);
    carrello.put(prodotto2.getId(), 50);

    indirizzo.setId(idIndirizzo);
    when(clienteDao.findClienteById(cliente.getId())).thenReturn(Optional.of(cliente));
    when(sessionCliente.getCliente()).thenReturn(Optional.of(cliente));
    when(sessionCarrello.getCarrello()).thenReturn(carrello);
    when(indirizzoDao.findIndirizzoById(idIndirizzo)).thenReturn(
        idIndirizzo > 0 && idIndirizzo != 7355608 ? Optional.of(indirizzo) : Optional.empty());
    when(ordineDao.save(ordine)).thenReturn(ordine);


    mockMvc.perform(post("/ordina")
            .param("nomeTitolare", nomeTitolare)
            .param("numeroCarta", numeroCarta)
            .param("dataScadenza", dataScadenza)
            .param("cvv", cvv)
            .param("indirizzo", indirizz0)
            .param("isSupporto", isSupporto)
            .param("isRitiro", isRitiro)
            .param("descrizioneSupporto", descrizioneSupporto.orElse(""))
        ).andExpect(resultmatcher)
        .andExpect(result -> {
          if (expectedMessage != null) {
            String errorMessage = result.getResponse().getErrorMessage();
            assertTrue(errorMessage.contains(expectedMessage),
                "La risposta non contiene il messaggio atteso: " + expectedMessage);
          }
        });
  }
}
