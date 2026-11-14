package it.unisa.greenbottle.storage.abbonamento.entity;

import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Rappresenta una disposizione di prodotto associata a un abbonamento nel sistema GreenBottle.
 * Ogni disposizione specifica una quantità di un prodotto legata a un abbonamento.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
// Campi valorizzati da JPA/Lombok: final non è applicabile.
@SuppressWarnings("creedengo-java:GCI82")
public class Disposizione implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // Identificativo univoco della disposizione

  @Column(nullable = false)
  private int quantita; // Quantità del prodotto associato all'abbonamento

  @ManyToOne
  private Prodotto prodotto; // Prodotto associato alla disposizione

  @ManyToOne
  private Abbonamento abbonamento; // Abbonamento al quale è associata la disposizione

  /**
   * Costruttore per creare una disposizione con parametri specifici.
   *
   * @param quantita    Quantità di prodotto per questa disposizione
   * @param prodotto    Prodotto associato alla disposizione
   * @param abbonamento Abbonamento associato alla disposizione
   */
  public Disposizione(int quantita, Prodotto prodotto, Abbonamento abbonamento) {
    this.quantita = quantita;
    this.prodotto = prodotto;
    this.abbonamento = abbonamento;
  }
}
