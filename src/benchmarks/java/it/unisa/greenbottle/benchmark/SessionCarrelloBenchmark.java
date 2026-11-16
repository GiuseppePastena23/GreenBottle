package it.unisa.greenbottle.benchmark;

import it.unisa.greenbottle.controller.ordineControl.util.SessionCarrello;
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
 * Benchmark JMH per le operazioni in-memory di {@link SessionCarrello}.
 * Le operazioni distruttive (rimozione e svuotamento) usano uno stato
 * ricreato a ogni invocazione, così ogni misura parte da un carrello pieno.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class SessionCarrelloBenchmark {

  /**
   * Stato con carrello precompilato, per le operazioni non distruttive.
   */
  @State(Scope.Thread)
  public static class StatoCarrello {

    /** Numero di prodotti presenti nel carrello all'avvio. */
    @Param({"1", "10", "100"})
    public int prodottiNelCarrello;

    /** Istanza del carrello sotto misurazione. */
    public SessionCarrello carrello;

    /**
     * Crea il carrello e lo riempie con un numero parametrizzato di prodotti.
     */
    @Setup(Level.Trial)
    public void setup() {
      carrello = new SessionCarrello();
      for (long id = 1; id <= prodottiNelCarrello; id++) {
        carrello.addToCarrello(id, 1);
      }
    }
  }

  /**
   * Stato con carrello ricreato a ogni invocazione, per le operazioni
   * distruttive che svuotano o modificano la struttura del carrello.
   */
  @State(Scope.Thread)
  public static class StatoCarrelloPieno {

    /** Numero di prodotti presenti nel carrello a ogni invocazione. */
    @Param({"1", "10", "100"})
    public int prodottiNelCarrello;

    /** Istanza del carrello sotto misurazione. */
    public SessionCarrello carrello;

    /**
     * Ricrea e riempie il carrello prima di ogni singola invocazione.
     */
    @Setup(Level.Invocation)
    public void setup() {
      carrello = new SessionCarrello();
      for (long id = 1; id <= prodottiNelCarrello; id++) {
        carrello.addToCarrello(id, 1);
      }
    }
  }

  /**
   * Misura l'aggiunta di quantità a un prodotto già presente nel carrello.
   *
   * @param stato stato con il carrello precompilato
   * @return true se l'aggiunta è andata a buon fine
   */
  @Benchmark
  public boolean addToCarrelloProdottoEsistente(StatoCarrello stato) {
    return stato.carrello.addToCarrello(1L, 1);
  }

  /**
   * Misura la rimozione di un prodotto presente nel carrello.
   *
   * @param stato stato con il carrello ricreato a ogni invocazione
   * @return true se la rimozione è andata a buon fine
   */
  @Benchmark
  public boolean removeFromCarrello(StatoCarrelloPieno stato) {
    return stato.carrello.removeFromCarrello(1L);
  }

  /**
   * Misura lo svuotamento completo del carrello.
   *
   * @param stato stato con il carrello ricreato a ogni invocazione
   * @return true se il carrello non era già vuoto
   */
  @Benchmark
  public boolean clearCarrello(StatoCarrelloPieno stato) {
    return stato.carrello.clearCarrello();
  }
}
