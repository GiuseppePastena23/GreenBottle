package it.unisa.greenbottle.benchmark;

import it.unisa.greenbottle.controller.ordineControl.util.SessionCarrello;
import it.unisa.greenbottle.storage.catalogoStorage.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogoStorage.entity.Prodotto;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
 * Benchmark JMH di {@link SessionCarrello#getRealCarrello()}, cioè del percorso
 * che il carrello segue per tradurre gli id salvati in sessione nelle entità
 * {@link Prodotto} da mostrare a video.
 *
 * <p>Il DAO è sostituito da un doppio in memoria (un {@link Proxy} dinamico che
 * implementa solo {@code findById}) per due ragioni: rende il benchmark
 * deterministico e, soprattutto, isola il costo computazionale del ciclo dal
 * tempo di accesso al database. Il valore misurato è quindi il <em>limite
 * inferiore</em> del costo reale: in produzione ogni {@code findById} è una
 * query SQL, mentre qui è una lookup su {@code HashMap}.
 *
 * <p>Il parametro {@code percentualeProdottiPresenti} modella il caso reale in
 * cui il carrello conserva id di prodotti nel frattempo rimossi dal catalogo:
 * il {@code Optional} vuoto obbliga comunque il codice a eseguire la query.
 * Poiché il costo cresce linearmente con il numero di righe del carrello e ogni
 * riga produce una query separata, il benchmark documenta il classico
 * problema N+1: il tempo è lineare nel benchmark, ma nel sistema reale è
 * lineare nel numero di round-trip verso il DB.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class SessionCarrelloRealeBenchmark {

  /**
   * Stato del benchmark: carrello precompilato e DAO in memoria con un
   * catalogo parametrizzato.
   */
  @State(Scope.Thread)
  public static class StatoCarrello {

    /** Numero di righe presenti nel carrello. */
    @Param({"10", "100", "1000"})
    public int prodottiNelCarrello;

    /** Percentuale di id del carrello ancora presenti nel catalogo (50 = metà mancanti). */
    @Param({"100", "50"})
    public int percentualeProdottiPresenti;

    /** Carrello sotto misurazione. */
    public SessionCarrello carrello;

    /**
     * Costruisce il catalogo in memoria, il DAO fittizio e il carrello.
     */
    @Setup(Level.Trial)
    public void setup() {
      final Map<Long, Prodotto> catalogo = new HashMap<>();
      for (long id = 1; id <= prodottiNelCarrello; id++) {
        if (id % 100 < percentualeProdottiPresenti) {
          catalogo.put(id, prodotto(id));
        }
      }

      carrello = new SessionCarrello();
      carrello.setProdottoDao(daoInMemoria(catalogo));
      for (long id = 1; id <= prodottiNelCarrello; id++) {
        carrello.addToCarrello(id, 1);
      }
    }

    /**
     * Crea un prodotto minimo ma completo, con id valorizzato.
     *
     * @param id identificativo del prodotto
     * @return il prodotto con quell'id
     */
    private static Prodotto prodotto(long id) {
      Prodotto prodotto =
          new Prodotto("prodotto-" + id, "descrizione-" + id, null, 1.0f, 10, null);
      prodotto.setId(id);
      return prodotto;
    }
  }

  /**
   * Crea un {@link ProdottoDao} in memoria che delega {@code findById} a una
   * mappa e rifiuta ogni altro metodo del repository.
   *
   * <p>Un proxy dinamico evita di dover implementare l'intera
   * {@code JpaRepository} (decine di metodi) e mantiene il benchmark
   * indipendente da Mockito, che non è nel classpath dei benchmark.
   *
   * @param catalogo mappa id-prodotto che simula il database
   * @return un DAO che legge solo da quella mappa
   */
  private static ProdottoDao daoInMemoria(final Map<Long, Prodotto> catalogo) {
    return (ProdottoDao) Proxy.newProxyInstance(
        ProdottoDao.class.getClassLoader(),
        new Class<?>[] {ProdottoDao.class},
        (proxy, metodo, argomenti) -> {
          if (metodo.getDeclaringClass() == Object.class) {
            return switch (metodo.getName()) {
              case "toString" -> "ProdottoDaoInMemoria";
              case "hashCode" -> System.identityHashCode(proxy);
              case "equals" -> proxy == argomenti[0];
              default -> null;
            };
          }
          if ("findById".equals(metodo.getName())) {
            return Optional.ofNullable(catalogo.get((Long) argomenti[0]));
          }
          throw new UnsupportedOperationException(
              "Metodo non previsto dal benchmark: " + metodo.getName());
        });
  }

  /**
   * Misura il tempo medio di {@link SessionCarrello#getRealCarrello()}.
   *
   * @param stato stato con carrello e catalogo già pronti
   * @return la mappa prodotto-quantità restituita al chiamante
   */
  @Benchmark
  public Map<Prodotto, Integer> getRealCarrello(StatoCarrello stato) {
    return stato.carrello.getRealCarrello();
  }
}
