package it.unisa.greenbottle.controller.ordine.util;

import it.unisa.greenbottle.storage.accesso.entity.Admin;
import it.unisa.greenbottle.storage.areapersonale.entity.Indirizzo;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import it.unisa.greenbottle.storage.ordine.entity.Composizione;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe che rappresenta un wrapper per l'oggetto Ordine, che contiene anche la lista delle
 * composizioni di un ordine.
 */
public class OrdineWrapper implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Ordine ord;
  private final List<Composizione> comp;

  public OrdineWrapper(Ordine ord, List<Composizione> comp) {
    this.ord = ord;
    this.comp = comp;
  }

  public Long getId() {
    return ord.getId();
  }

  public String getDescrizione() {
    return ord.getDescrizione();
  }

  public float getPrezzo() {
    return ord.getPrezzo();
  }

  public Ordine.StatoSpedizione getStato() {
    return ord.getStato();
  }

  public boolean isRitiro() {
    return ord.isRitiro();
  }

  public boolean isSupporto() {
    return ord.isSupporto();
  }

  public String getNomeCliente() {
    return ord.getCliente().getNome();
  }

  public String getCognomeCliente() {
    return ord.getCliente().getCognome();
  }

  public String getEmailCliente() {
    return ord.getCliente().getEmail();
  }

  public Timestamp getData() {
    return ord.getData();
  }

  public Admin getAdmin() {
    return ord.getAdmin();
  }

  /**
   * Metodo che restituisce l'indirizzo dell'ordine.
   *
   * @return Stringa contenente l'indirizzo dell'ordine.
   */
  public String getIndirizzo() {
    final Indirizzo i = ord.getIndirizzo();
    return i.getVia() + " " + i.getCivico() + " " + i.getCap() + " " + i.getCitta() + " "
        + i.getProvincia();
  }

  /**
   * Metodo che restituisce una mappa contenente i prodotti dell'ordine e le relative quantità.
   *
   * @return Mappa contenente i prodotti dell'ordine e le relative quantità.
   */
  public Map<Prodotto, Integer> getProdottiOrdine() {
    final Map<Prodotto, Integer> mappa = new HashMap<>();
    for (final Composizione composizione : comp) {
      final Prodotto chiave = composizione.getProdotto();
      final Integer valore = composizione.getQuantita();
      mappa.put(chiave, valore);
    }
    return mappa;
  }

  @Override
  public String toString() {
    return "OrdineWrapper{"
        + "id=" + getId()
        + ", descrizione='" + getDescrizione() + '\''
        + ", prezzo=" + getPrezzo()
        + ", stato=" + getStato()
        + ", ritiro=" + isRitiro()
        + ", supporto=" + isSupporto()
        + ", nomeCliente='" + getNomeCliente() + '\''
        + ", cognomeCliente='" + getCognomeCliente() + '\''
        + ", emailCliente='" + getEmailCliente() + '\''
        + ", data=" + getData() + '\''
        + ", indirizzo='" + getIndirizzo() + '\''
        + ", prodottiOrdine=" + getProdottiOrdine()
        + '}';
  }
}
