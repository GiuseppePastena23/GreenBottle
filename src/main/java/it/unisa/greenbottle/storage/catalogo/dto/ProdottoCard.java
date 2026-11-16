package it.unisa.greenbottle.storage.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Dati essenziali di un prodotto per la visualizzazione del catalogo.
 * Non contiene l'immagine, che viene richiesta separatamente tramite /productImg.
 */
@Data
@AllArgsConstructor
// Campi valorizzati da Lombok/JavaBean: final non è applicabile.
@SuppressWarnings("creedengo-java:GCI82")
public class ProdottoCard {
  private Long id;
  private String nome;
  private float prezzo;
}
