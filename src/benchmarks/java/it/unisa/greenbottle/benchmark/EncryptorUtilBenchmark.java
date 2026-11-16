package it.unisa.greenbottle.benchmark;

import it.unisa.greenbottle.controller.accessoControl.util.EncryptorUtil;
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
 * Benchmark JMH per i metodi di {@link EncryptorUtil}.
 * Misura il tempo medio di cifratura e decifratura AES al variare della
 * lunghezza della stringa di input.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class EncryptorUtilBenchmark {

  /**
   * Stato del benchmark: contiene il testo di input e la sua versione cifrata.
   */
  @State(Scope.Thread)
  public static class StatoTesto {

    /** Lunghezza in caratteri del testo di input. */
    @Param({"8", "64", "512"})
    public int lunghezzaTesto;

    /** Testo di input da cifrare. */
    public String testo;

    /** Testo già cifrato, usato come input del benchmark di decifratura. */
    public String testoCifrato;

    /**
     * Prepara il testo di input e la sua versione cifrata.
     */
    @Setup
    public void setup() {
      testo = "a".repeat(lunghezzaTesto);
      testoCifrato = EncryptorUtil.encrypt(testo);
    }
  }

  /**
   * Misura il tempo medio di {@link EncryptorUtil#encrypt(String)}.
   *
   * @param stato stato contenente il testo di input
   * @return la stringa cifrata in Base64
   */
  @Benchmark
  public String encrypt(StatoTesto stato) {
    return EncryptorUtil.encrypt(stato.testo);
  }

  /**
   * Misura il tempo medio di {@link EncryptorUtil#decrypt(String)}.
   *
   * @param stato stato contenente il testo già cifrato
   * @return la stringa decifrata
   */
  @Benchmark
  public String decrypt(StatoTesto stato) {
    return EncryptorUtil.decrypt(stato.testoCifrato);
  }
}
