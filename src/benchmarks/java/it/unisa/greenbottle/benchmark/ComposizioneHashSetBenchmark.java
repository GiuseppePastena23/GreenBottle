package it.unisa.greenbottle.benchmark;

import it.unisa.greenbottle.storage.catalogoStorage.entity.Prodotto;
import it.unisa.greenbottle.storage.ordineStorage.entity.Composizione;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * Benchmark JMH della costruzione del {@code HashSet<Composizione>} che
 * rappresenta le righe di un ordine.
 *
 * <p>Il codice di produzione esegue esattamente questa operazione in
 * {@code CreazioneOrdineController#post}: itera sulla mappa dei prodotti del
 * carrello e inserisce in un {@code HashSet} delle {@link Composizione} appena
 * create, quindi con {@code id} ancora nullo. {@link Composizione} però
 * definisce {@code hashCode()} come {@code id != null ? id.hashCode() : 0}:
 * tutte le righe nuove finiscono nello stesso bucket e ogni inserimento
 * confronta la nuova riga con tutte quelle già presenti, perché
 * {@code equals} restituisce sempre false quando gli id sono nulli. Il
 * risultato è un costo quadratico mascherato da un innocuo {@code add}.
 *
 * <p>Il benchmark confronta le composizioni transienti (id nullo, caso di
 * produzione) con le stesse composizioni con id valorizzato (baseline): la
 * differenza di andamento tra le due curve, al crescere di
 * {@code numeroComposizioni}, è la misura del difetto. La creazione degli
 * oggetti è esclusa dalla misura (avviene in {@code @Setup}) perché è identica
 * nei due casi e servirebbe solo a sporcare il confronto.
 *
 * <p>Il loop del controller non è invocabile senza un contesto Spring
 * completo; il benchmark ne riproduce la sola struttura significativa ai fini
 * della misura (inserimento nel set di entità transienti). Se il controller
 * cambia, questo benchmark va riallineato.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class ComposizioneHashSetBenchmark {

  /**
   * Stato del benchmark: le due liste di composizioni, create una sola volta.
   */
  @State(Scope.Thread)
  public static class StatoComposizioni {

    /** Numero di righe (composizioni) dell'ordine. */
    @Param({"10", "100", "1000"})
    public int numeroComposizioni;

    /** Composizioni transienti, come quelle costruite dal controller. */
    public List<Composizione> transienti;

    /** Composizioni con id valorizzato, usate come baseline. */
    public List<Composizione> conId;

    /**
     * Costruisce le due liste con gli stessi prodotti di riferimento.
     */
    @Setup(Level.Trial)
    public void setup() {
      transienti = new ArrayList<>(numeroComposizioni);
      conId = new ArrayList<>(numeroComposizioni);
      for (long i = 0; i < numeroComposizioni; i++) {
        Prodotto prodotto = new Prodotto("prodotto-" + i, "descrizione-" + i, null, 1.0f, 10, null);
        prodotto.setId(i);
        transienti.add(new Composizione(prodotto, 1));

        Composizione persistita = new Composizione(prodotto, 1);
        persistita.setId(i);
        conId.add(persistita);
      }
    }
  }

  /**
   * Misura l'inserimento di composizioni con id nullo: è il caso del
   * controller, in cui {@code hashCode()} vale 0 per tutte.
   *
   * @param stato stato con le composizioni transienti pronte
   * @return il set costruito
   */
  @Benchmark
  public Set<Composizione> inserisciComposizioniTransienti(StatoComposizioni stato) {
    Set<Composizione> composizioni = new HashSet<>();
    for (Composizione composizione : stato.transienti) {
      composizioni.add(composizione);
    }
    return composizioni;
  }

  /**
   * Misura l'inserimento delle stesse composizioni con id valorizzato, cioè
   * con hash distinti: è la baseline che mostra il costo evitabile.
   *
   * @param stato stato con le composizioni identificate pronte
   * @return il set costruito
   */
  @Benchmark
  public Set<Composizione> inserisciComposizioniConId(StatoComposizioni stato) {
    Set<Composizione> composizioni = new HashSet<>();
    for (Composizione composizione : stato.conId) {
      composizioni.add(composizione);
    }
    return composizioni;
  }
}
