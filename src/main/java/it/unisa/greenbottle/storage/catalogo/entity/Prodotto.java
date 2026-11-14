package it.unisa.greenbottle.storage.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Rappresenta un prodotto nel catalogo del sistema GreenBottle.
 * Ogni prodotto include informazioni sul nome, descrizione, prezzo, quantità disponibile,
 * media dei voti, immagine e categoria di appartenenza.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
// Identità JPA: equals/hashCode sul solo id, senza hashing su img (byte[]) e categoria.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "img")
// Campi valorizzati da JPA/Lombok: final non è applicabile.
@SuppressWarnings("creedengo-java:GCI82")
public class Prodotto implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id; // Identificativo univoco del prodotto

  @Column(nullable = false, length = 256)
  private String nome; // Nome del prodotto

  @Column(nullable = false, length = 1024)
  private String descrizione; // Descrizione del prodotto

  @Lob
  private byte[] img; // Immagine del prodotto (se presente)

  @Column(nullable = false)
  private float prezzo; // Prezzo del prodotto

  @Column(nullable = false)
  private int quantita; // Quantità disponibile del prodotto

  @Column()
  private float mediaVoti; // Media dei voti del prodotto

  @ManyToOne
  private Categoria categoria; // Categoria di appartenenza del prodotto

  /**
   * Costruttore per creare un prodotto con parametri specifici.
   *
   * @param nome        Nome del prodotto
   * @param descrizione Descrizione del prodotto
   * @param img         Immagine del prodotto (se presente)
   * @param prezzo      Prezzo del prodotto
   * @param quantita    Quantità disponibile del prodotto
   * @param categoria   Categoria di appartenenza del prodotto
   */
  public Prodotto(String nome, String descrizione, byte[] img, float prezzo, int quantita,
                  Categoria categoria) {
    this.nome = nome;
    this.descrizione = descrizione;
    this.img = img;
    this.prezzo = prezzo;
    this.quantita = quantita;
    this.categoria = categoria;
  }
}
