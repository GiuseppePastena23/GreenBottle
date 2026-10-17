package it.unisa.greenbottle.benchmark;

import it.unisa.greenbottle.controller.accessoControl.form.RegistrazioneForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark JMH della validazione Bean Validation di {@link RegistrazioneForm}.
 * Le vincolistiche (regex, lunghezze, email) sono definite sul form:
 * il benchmark misura il costo di quel insieme di regole su input valido e non valido.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class RegistrazioneFormBenchmark {

  /**
   * Stato del benchmark: validator condiviso e due form precompilati.
   */
  @State(Scope.Benchmark)
  public static class StatoValidazione {

    /** Fabbrica dei validator, chiusa al termine del benchmark. */
    public ValidatorFactory factory;

    /** Validator Bean Validation condiviso. */
    public Validator validator;

    /** Form che rispetta tutte le vincolistiche. */
    public RegistrazioneForm formValido;

    /** Form che viola tutte le vincolistiche. */
    public RegistrazioneForm formNonValido;

    /**
     * Inizializza il validator e i due form di prova.
     */
    @Setup
    public void setup() {
      factory = Validation.buildDefaultValidatorFactory();
      validator = factory.getValidator();
      formValido = new RegistrazioneForm(
          "Mario", "Rossi", "mario.rossi@example.com", "Password1!");
      formNonValido = new RegistrazioneForm("", "x", "non-una-email", "semplice");
    }

    /**
     * Chiude la fabbrica dei validator.
     */
    @TearDown
    public void tearDown() {
      factory.close();
    }
  }

  /**
   * Misura il tempo medio di validazione di un form valido.
   *
   * @param stato stato con validator e form precompilati
   * @return l'insieme delle violazioni (vuoto per un form valido)
   */
  @Benchmark
  public Set<ConstraintViolation<RegistrazioneForm>> validateFormValido(StatoValidazione stato) {
    return stato.validator.validate(stato.formValido);
  }

  /**
   * Misura il tempo medio di validazione di un form non valido.
   *
   * @param stato stato con validator e form precompilati
   * @return l'insieme delle violazioni rilevate
   */
  @Benchmark
  public Set<ConstraintViolation<RegistrazioneForm>> validateFormNonValido(
      StatoValidazione stato) {
    return stato.validator.validate(stato.formNonValido);
  }
}
