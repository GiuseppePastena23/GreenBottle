package it.unisa.greenbottle.controller.ordine;

import it.unisa.greenbottle.controller.accesso.util.SessionAdmin;
import it.unisa.greenbottle.storage.ordine.dao.OrdineDao;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller per l'accettazione di un ordine da parte dell'admin.
 */
@Controller
@RequestMapping("/admin/accettazioneOrdine")
@RequiredArgsConstructor
public class AccettazioneOrdineAdminController {

  private final OrdineDao ordineDao;
  private final SessionAdmin sessionAdmin;

  /**
   * Metodo per l'accettazione di un ordine da parte dell'admin.
   *
   * @param id              id dell'ordine da accettare
   * @param statoSpedizione stato di spedizione dell'ordine
   * @return ResponseEntity con messaggio di successo o errore
   */
  @PostMapping
  public ResponseEntity<Object> post(@RequestParam("ordineId") final String id,
                                     @RequestParam("newState") final String statoSpedizione) {
    final Long veroId = Long.valueOf(id);
    if (veroId <= 0) { // formato errato dell'id
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Id non valido.");
    }

    final Optional<Ordine> optOrdine = ordineDao.findOrdineById(veroId);
    if (optOrdine.isEmpty()) {
      // Risponde con un errore se l'ordine non esiste
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordine non presente.");
    }

    final Ordine ordine = optOrdine.get();
    ordine.setStato(Ordine.StatoSpedizione.valueOf(statoSpedizione));
    ordine.setAdmin(sessionAdmin.getAdmin().get());
    ordineDao.save(ordine);

    // Risponde con un messaggio di successo
    return ResponseEntity.status(HttpStatus.OK)
        .body("Stato dell'ordine aggiornato a: " + statoSpedizione);
  }
}
