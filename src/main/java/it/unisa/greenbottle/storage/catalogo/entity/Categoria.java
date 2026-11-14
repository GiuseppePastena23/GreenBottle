package it.unisa.greenbottle.storage.catalogo.entity;

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
 * Rappresenta una categoria di prodotti nel sistema GreenBottle.
 * Ogni categoria ha un nome univoco che identifica il gruppo di prodotti a cui appartiene.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
// Campi valorizzati da JPA/Lombok: final non è applicabile.
@SuppressWarnings("creedengo-java:GCI82")
public class Categoria implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // Identificativo univoco della categoria

  @Column(nullable = false, length = 256)
  private String nome; // Nome della categoria

  /**
   * Costruttore per creare una categoria con un nome specifico.
   *
   * @param nome Nome della categoria
   */
  public Categoria(String nome) {
    this.nome = nome;
  }
}
