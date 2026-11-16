package it.unisa.greenbottle.benchmark;

import it.unisa.greenbottle.storage.catalogoStorage.entity.Categoria;
import it.unisa.greenbottle.storage.catalogoStorage.entity.Prodotto;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark JMH del contratto {@code equals}/{@code hashCode} generato da
 * Lombok ({@code @Data}) su {@link Prodotto}.
 *
 * <p>Nel codice di produzione {@code Prodotto} viene usato come chiave di
 * {@code HashMap} in più punti ({@link it.unisa.greenbottle.controller.ordineControl.util.OrdineWrapper},
 * {@link it.unisa.greenbottle.controller.abbonamentoControl.util.AbbonamentoWrapper},
 * {@code SessionCarrello}). Con {@code @Data} Lombok genera un hashCode che
 * percorre <em>tutti</em> i campi, compreso il blob {@code byte[] img}, e un
 * equals che confronta anch'esso tutti i campi, ricorsivamente sulla
 * {@link Categoria}. Il problema non è solo il costo: finché l'id è nullo
 * (entità non ancora persistita) due prodotti distinti possono risultare
 * {@code equals}, e comunque ogni operazione sulla mappa paga l'intero
 * confronto invece del solo id.
 *
 * <p>Il benchmark isola due effetti: il costo del singolo {@code hashCode} e
 * del singolo {@code equals} (con e senza categoria valorizzata) e il costo di
 * una lookup in {@code HashMap} con chiave entità rispetto alla stessa lookup
 * con chiave {@code Long} (baseline), che è ciò che pagherebbe una mappa
 * indicizzata per id.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class ProdottoHashingBenchmark {

  /** Numero di elementi inseriti nelle mappe confrontate. */
  private static final int DIMENSIONE_MAPPA = 1000;

  /** Dimensione in byte dell'immagine associata a ogni prodotto. */
  private static final int DIMENSIONE_IMMAGINE = 128;

  /** Lunghezza della descrizione di ogni prodotto. */
  private static final int LUNGHEZZA_DESCRIZIONE = 256;

  /**
   * Stato condiviso: prodotto sotto misurazione, suo duplicato uguale ma
   * distinto e le due mappe da confrontare.
   */
  @State(Scope.Benchmark)
  public static class StatoHashing {

    /** Se true il prodotto ha una categoria valorizzata, altrimenti ha categoria null. */
    @Param({"false", "true"})
    public boolean conCategoria;

    /** Prodotto usato per hashCode ed equals. */
    public Prodotto prodotto;

    /** Duplicato del prodotto: stessi valori, istanze di String, byte[] e Categoria distinte. */
    public Prodotto duplicato;

    /** Chiave di ricerca uguale per valore a una chiave presente nella mappa. */
    public Prodotto chiaveRicerca;

    /** Mappa indicizzata per entità, come nel codice di produzione. */
    public HashMap<Prodotto, Integer> mappaProdotti;

    /** Mappa di controllo indicizzata per id. */
    public HashMap<Long, Integer> mappaId;

    /** Chiave {@code Long} di ricerca, distinta per identità da quella inserita. */
    public Long chiaveId;

    /**
     * Prepara prodotto, duplicato e mappe con {@value #DIMENSIONE_MAPPA} elementi.
     */
    @Setup(Level.Trial)
    public void setup() {
      Categoria categoria = conCategoria ? new Categoria("Categoria-benchmark") : null;
      if (categoria != null) {
        categoria.setId(7L);
      }
      byte[] immagine = new byte[DIMENSIONE_IMMAGINE];
      Arrays.fill(immagine, (byte) 1);

      prodotto = creaProdotto(0L, immagine, categoria);
      duplicato = creaProdotto(0L, immagine.clone(), copiaCategoria(categoria));

      mappaProdotti = new HashMap<>(DIMENSIONE_MAPPA * 2);
      mappaId = new HashMap<>(DIMENSIONE_MAPPA * 2);
      for (long id = 0; id < DIMENSIONE_MAPPA; id++) {
        byte[] img = new byte[DIMENSIONE_IMMAGINE];
        Arrays.fill(img, (byte) 1);
        mappaProdotti.put(creaProdotto(id, img, copiaCategoria(categoria)), 1);
        mappaId.put(id, 1);
      }
      chiaveRicerca = creaProdotto(0L, immagine.clone(), copiaCategoria(categoria));
      chiaveId = Long.valueOf(DIMENSIONE_MAPPA / 2);
    }

    /**
     * Crea un prodotto con valori derivati dall'id, così ogni istanza è distinta.
     *
     * @param id        identificativo del prodotto
     * @param immagine  blob immagine
     * @param categoria categoria associata, può essere null
     * @return il prodotto costruito
     */
    private static Prodotto creaProdotto(long id, byte[] immagine, Categoria categoria) {
      Prodotto prodotto = new Prodotto(
          new String("prodotto-" + id),
          new String("descrizione-" + "x".repeat(LUNGHEZZA_DESCRIZIONE) + id),
          immagine,
          1.0f,
          10,
          categoria);
      prodotto.setId(id);
      prodotto.setMediaVoti(4.5f);
      return prodotto;
    }

    /**
     * Copia la categoria (null-safe), così l'uguaglianza passa per due istanze
     * distinte e non per la stessa referenza.
     *
     * @param categoria categoria da copiare, può essere null
     * @return la copia oppure null
     */
    private static Categoria copiaCategoria(Categoria categoria) {
      if (categoria == null) {
        return null;
      }
      Categoria copia = new Categoria(new String(categoria.getNome()));
      copia.setId(categoria.getId());
      return copia;
    }
  }

  /**
   * Misura il costo di {@link Object#hashCode()} su un'entità Lombok.
   *
   * @param stato stato con il prodotto pronto
   * @return l'hash code calcolato
   */
  @Benchmark
  public int hashCodeProdotto(StatoHashing stato) {
    return stato.prodotto.hashCode();
  }

  /**
   * Misura il costo di {@link Object#equals(Object)} tra due prodotti uguali
   * per valore ma distinti per identità.
   *
   * @param stato stato con prodotto e duplicato
   * @return true, perché tutti i campi coincidono
   */
  @Benchmark
  public boolean equalsProdotti(StatoHashing stato) {
    return stato.prodotto.equals(stato.duplicato);
  }

  /**
   * Misura una lookup in {@code HashMap} con chiave {@link Prodotto}.
   *
   * @param stato stato con la mappa indicizzata per entità
   * @return il valore associato alla chiave
   */
  @Benchmark
  public Integer lookupConChiaveProdotto(StatoHashing stato) {
    return stato.mappaProdotti.get(stato.chiaveRicerca);
  }

  /**
   * Misura la stessa lookup con chiave {@code Long}, a parità di dimensione
   * della mappa: è la baseline che isola il costo dell'entità come chiave.
   *
   * @param stato stato con la mappa indicizzata per id
   * @return il valore associato alla chiave
   */
  @Benchmark
  public Integer lookupConChiaveId(StatoHashing stato) {
    return stato.mappaId.get(stato.chiaveId);
  }
}
