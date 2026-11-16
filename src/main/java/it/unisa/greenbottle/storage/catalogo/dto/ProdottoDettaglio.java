package it.unisa.greenbottle.storage.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Dati di un prodotto per la scheda prodotto.
 * Non contiene l'immagine, servita separatamente tramite /productImg.
 */
@Data
@AllArgsConstructor
// Campi valorizzati da Lombok/Jackson: final non è applicabile.
@SuppressWarnings("creedengo-java:GCI82")
public class ProdottoDettaglio {
  private Long id;
  private String nome;
  private String descrizione;
  private float prezzo;
  private int quantita;
  private float mediaVoti;
  private Long idCategoria;
  private String nomeCategoria;
}
