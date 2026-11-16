package it.unisa.greenbottle.storage.ordine.dao;

import it.unisa.greenbottle.storage.ordine.entity.Composizione;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati delle composizioni nel database.
 */
public interface ComposizioneDao extends JpaRepository<Composizione, Long> {
  List<Composizione> findComposizioneByOrdine(final Ordine ordine);

  List<Composizione> findComposizioneByOrdineIn(final Collection<Ordine> ordini);
}
