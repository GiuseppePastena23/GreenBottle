package it.unisa.greenbottle.storage.ordine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Rappresenta un corriere nel sistema GreenBottle.
 * Il corriere è associato alla gestione della consegna degli ordini.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
// Campi valorizzati da JPA/Lombok: final non è applicabile.
@SuppressWarnings("creedengo-java:GCI82")
public class Corriere implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // Identificativo univoco del corriere

  @Column(nullable = false, length = 30)
  private String nome; // Nome del corriere

  @Column(nullable = false, length = 30)
  private String cognome; // Cognome del corriere

  @Column(nullable = false, length = 10)
  private String telefono; // Numero di telefono del corriere

  /**
   * Costruttore per creare un corriere con nome, cognome e numero di telefono.
   *
   * @param nome     Nome del corriere
   * @param cognome  Cognome del corriere
   * @param telefono Numero di telefono del corriere
   */
  public Corriere(String nome, String cognome, String telefono) {
    this.nome = nome;
    this.cognome = cognome;
    this.telefono = telefono;
  }
}
