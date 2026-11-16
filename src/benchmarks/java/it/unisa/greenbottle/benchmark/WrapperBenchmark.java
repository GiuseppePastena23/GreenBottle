package it.unisa.greenbottle.benchmark;

import it.unisa.greenbottle.controller.abbonamentoControl.util.AbbonamentoWrapper;
import it.unisa.greenbottle.controller.ordineControl.util.OrdineWrapper;
import it.unisa.greenbottle.storage.abbonamentoStorage.entity.Abbonamento;
import it.unisa.greenbottle.storage.abbonamentoStorage.entity.Disposizione;
import it.unisa.greenbottle.storage.catalogoStorage.entity.Prodotto;
import it.unisa.greenbottle.storage.ordineStorage.entity.Composizione;
import it.unisa.greenbottle.storage.ordineStorage.entity.Ordine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark JMH per la costruzione delle mappe prodotto-quantità di
 * {@link OrdineWrapper#getProdottiOrdine()} e
 * {@link AbbonamentoWrapper#getProdottiAbbonamento()}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class WrapperBenchmark {

  /**
   * Stato per il benchmark di {@link OrdineWrapper#getProdottiOrdine()}.
   */
  @State(Scope.Thread)
  public static class StatoOrdine {

    /** Numero di composizioni (righe) dell'ordine. */
    @Param({"10", "100", "1000"})
    public int numeroComposizioni;

    /** Wrapper dell'ordine sotto misurazione. */
    public OrdineWrapper wrapper;

    /**
     * Costruisce un ordine con un numero parametrizzato di composizioni.
     */
    @Setup
    public void setup() {
      List<Composizione> composizioni = new ArrayList<>(numeroComposizioni);
      for (int i = 0; i < numeroComposizioni; i++) {
        Prodotto prodotto = new Prodotto("prodotto-" + i, "descrizione-" + i, null, 1.0f, 10, null);
        prodotto.setId((long) i);
        composizioni.add(new Composizione(prodotto, 1));
      }
      wrapper = new OrdineWrapper(new Ordine(), composizioni);
    }
  }

  /**
   * Stato per il benchmark di {@link AbbonamentoWrapper#getProdottiAbbonamento()}.
   */
  @State(Scope.Thread)
  public static class StatoAbbonamento {

    /** Numero di disposizioni (righe) dell'abbonamento. */
    @Param({"10", "100", "1000"})
    public int numeroDisposizioni;

    /** Wrapper dell'abbonamento sotto misurazione. */
    public AbbonamentoWrapper wrapper;

    /**
     * Costruisce un abbonamento con un numero parametrizzato di disposizioni.
     */
    @Setup
    public void setup() {
      List<Disposizione> disposizioni = new ArrayList<>(numeroDisposizioni);
      for (int i = 0; i < numeroDisposizioni; i++) {
        Prodotto prodotto = new Prodotto("prodotto-" + i, "descrizione-" + i, null, 1.0f, 10, null);
        prodotto.setId((long) i);
        disposizioni.add(new Disposizione(1, prodotto, null));
      }
      wrapper = new AbbonamentoWrapper(new Abbonamento(), disposizioni);
    }
  }

  /**
   * Misura il tempo medio di {@link OrdineWrapper#getProdottiOrdine()}.
   *
   * @param stato stato con l'ordine precompilato
   * @return la mappa prodotto-quantità dell'ordine
   */
  @Benchmark
  public HashMap<Prodotto, Integer> getProdottiOrdine(StatoOrdine stato) {
    return stato.wrapper.getProdottiOrdine();
  }

  /**
   * Misura il tempo medio di {@link AbbonamentoWrapper#getProdottiAbbonamento()}.
   *
   * @param stato stato con l'abbonamento precompilato
   * @return la mappa prodotto-quantità dell'abbonamento
   */
  @Benchmark
  public HashMap<Prodotto, Integer> getProdottiAbbonamento(StatoAbbonamento stato) {
    return stato.wrapper.getProdottiAbbonamento();
  }
}
