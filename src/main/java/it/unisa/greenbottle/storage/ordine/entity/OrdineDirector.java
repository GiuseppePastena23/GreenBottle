package it.unisa.greenbottle.storage.ordine.entity;

import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.areapersonale.entity.Indirizzo;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe che funge da regista per la creazione degli oggetti {@link Ordine}.
 * Utilizza il pattern {@link OrdineBuilder} per costruire ordini in vari stati e configurazioni.
 */
public class OrdineDirector {

  private OrdineDirector() {
    // Classe di utilità: non deve essere istanziata.
  }

  /**
   * Crea un ordine vuoto con valori di default.
   * Questo ordine viene inizializzato con uno stato di elaborazione, un prezzo di 0.0 e altre
   * impostazioni predefinite.
   *
   * @param indirizzo L'indirizzo di spedizione per l'ordine
   * @param cliente   Il cliente che ha effettuato l'ordine
   * @return Un nuovo oggetto {@link Ordine} vuoto
   */
  public static Ordine createEmptyOrdine(final Indirizzo indirizzo, final Cliente cliente) {
    return new OrdineBuilder()
        .prezzo(0.0f)
        .stato(Ordine.StatoSpedizione.ELABORAZIONE)
        .isRitiro(false)
        .carta("0000-0000-0000-0000")
        .isSupporto(false)
        .descrizione("Default description")
        .data(new Timestamp(System.currentTimeMillis()))
        .indirizzo(indirizzo)
        .cliente(cliente)
        .composizioni(new HashSet<>())
        .admin(null)
        .build();
  }

  /**
   * Crea un ordine con supporto (che può essere associato a prodotti e altri dettagli).
   * Questo ordine avrà un prezzo totale, uno stato di elaborazione e altre informazioni necessarie.
   *
   * @param prezzoTotale Il prezzo totale dell'ordine
   * @param isRitiro     Indica se l'ordine è per il ritiro
   * @param carta        Il numero della carta di pagamento
   * @param descrizione  La descrizione dell'ordine
   * @param indirizzo    L'indirizzo di spedizione per l'ordine
   * @param cliente      Il cliente che ha effettuato l'ordine
   * @param composizioni Un set di composizioni (prodotti) associati all'ordine
   * @return Un nuovo oggetto {@link Ordine} con supporto
   */
  public static Ordine createOrdineConSupporto(final float prezzoTotale,
                                               final boolean isRitiro,
                                               final String carta,
                                               final String descrizione,
                                               final Indirizzo indirizzo,
                                               final Cliente cliente,
                                               final Set<Composizione> composizioni) {
    final Ordine ordine = new OrdineBuilder()
        .prezzo(prezzoTotale)
        .stato(Ordine.StatoSpedizione.ELABORAZIONE)
        .isRitiro(isRitiro)
        .carta(carta)
        .isSupporto(true)
        .descrizione(descrizione)
        .data(new Timestamp(System.currentTimeMillis()))
        .indirizzo(indirizzo)
        .cliente(cliente)
        .admin(null)
        .build();
    for (final Composizione composizione : composizioni) {
      composizione.setOrdine(ordine);
    }
    ordine.setComposizioni(composizioni);
    return ordine;
  }

  /**
   * Crea un ordine con uno stato di elaborazione, senza supporto e con il prezzo e
   * composizioni specificati.
   *
   * @param prezzo       Il prezzo dell'ordine
   * @param isRitiro     Indica se l'ordine è per il ritiro
   * @param carta        Il numero della carta di pagamento
   * @param indirizzo    L'indirizzo di spedizione
   * @param cliente      Il cliente che ha effettuato l'ordine
   * @param composizioni Un set di composizioni (prodotti) associati all'ordine
   * @return Un nuovo oggetto {@link Ordine} senza supporto
   */
  public static Ordine createOrdine(final float prezzo, final boolean isRitiro, final String carta,
                                    final Indirizzo indirizzo, final Cliente cliente,
                                    final Set<Composizione> composizioni) {
    final Ordine ordine = new OrdineBuilder()
        .prezzo(prezzo)
        .stato(Ordine.StatoSpedizione.ELABORAZIONE)
        .isRitiro(isRitiro)
        .carta(carta)
        .isSupporto(false)
        .descrizione(null)
        .data(new Timestamp(System.currentTimeMillis()))
        .indirizzo(indirizzo)
        .cliente(cliente)
        .admin(null)
        .build();
    for (final Composizione composizione : composizioni) {
      composizione.setOrdine(ordine);
    }
    ordine.setComposizioni(composizioni);
    return ordine;
  }
}
