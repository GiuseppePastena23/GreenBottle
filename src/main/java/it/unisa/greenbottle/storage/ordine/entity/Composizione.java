package it.unisa.greenbottle.storage.ordine.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Rappresenta una composizione di un ordine, che associa un prodotto a una quantità.
 * Ogni composizione fa parte di un ordine specifico.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = "ordine")
// Campi valorizzati da JPA/Lombok: final non è applicabile.
@SuppressWarnings("creedengo-java:GCI82")
public class Composizione implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // Identificativo univoco della composizione

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "ordine_id", nullable = false)
  private Ordine ordine; // Ordine a cui è associata questa composizione

  @ManyToOne
  @JoinColumn(name = "prodotto_id", nullable = false)
  private Prodotto prodotto; // Prodotto associato alla composizione

  @Column(nullable = false)
  private int quantita; // Quantità del prodotto nell'ordine

  /**
   * Metodo chiamato prima del persist per aggiornare la quantità del prodotto.
   * Se la quantità richiesta è maggiore della quantità disponibile del prodotto,
   * solleva un'eccezione.
   */
  @PrePersist
  public void updateQuantitaProdotto() {
    if (prodotto.getQuantita() < quantita) {
      throw new IllegalArgumentException(
          "Quantità richiesta per " + prodotto.getNome() + " non disponibile."
      );
    }
    prodotto.setQuantita(
        prodotto.getQuantita() - quantita); // Decrementa la quantità disponibile del prodotto
  }

  /**
   * Costruttore per creare una composizione con il prodotto e la quantità.
   *
   * @param prodotto Prodotto associato alla composizione
   * @param quantita Quantità del prodotto
   */
  public Composizione(Prodotto prodotto, int quantita) {
    this.prodotto = prodotto;
    this.quantita = quantita;
  }

  /**
   * Costruttore per creare una composizione con il prodotto, la quantità e l'ordine associato.
   *
   * @param prodotto Prodotto associato alla composizione
   * @param quantita Quantità del prodotto
   * @param ordine   Ordine a cui appartiene questa composizione
   */
  public Composizione(Prodotto prodotto, int quantita, Ordine ordine) {
    this.prodotto = prodotto;
    this.quantita = quantita;
    this.ordine = ordine;
  }

  /**
   * Verifica l'uguaglianza tra due composizioni.
   * Due composizioni già persistite sono uguali quando hanno lo stesso identificativo.
   * Due composizioni transienti (id {@code null}) sono uguali solo se sono la stessa istanza:
   * prima del salvataggio non esiste un identificativo di dominio, quindi righe distinte
   * non devono mai risultare uguali.
   *
   * @param o oggetto da confrontare
   * @return true se i due oggetti rappresentano la stessa composizione
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Composizione that = (Composizione) o;
    return id != null && id.equals(that.id); // Confronta per ID
  }

  /**
   * Calcola l'hash della composizione.
   * Se l'entità è persistita si usa l'identificativo; se è ancora transiente si usa l'hash di
   * identità dell'istanza. Restituire una costante (ad esempio {@code 0}) per le entità senza
   * id provocherebbe collisioni totali: tutte le righe finirebbero nello stesso bucket di un
   * {@code HashSet}, trasformando l'inserimento da O(N) a O(N^2) per via dei confronti a catena
   * con {@link #equals(Object)}.
   *
   * @return l'hash della composizione
   */
  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : System.identityHashCode(this);
  }
}
