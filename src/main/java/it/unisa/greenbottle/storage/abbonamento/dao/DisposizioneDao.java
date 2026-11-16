package it.unisa.greenbottle.storage.abbonamento.dao;

import it.unisa.greenbottle.storage.abbonamento.entity.Abbonamento;
import it.unisa.greenbottle.storage.abbonamento.entity.Disposizione;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati delle disposizioni.
 */
public interface DisposizioneDao extends JpaRepository<Disposizione, Long> {
  List<Disposizione> findDisposizioneByAbbonamento(final Abbonamento abbonamento);

  List<Disposizione> findDisposizioneByAbbonamentoIn(final Collection<Abbonamento> abbonamenti);
}
