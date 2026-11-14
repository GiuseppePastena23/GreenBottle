package it.unisa.greenbottle.storage.areapersonale.dao;

import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.areapersonale.entity.Indirizzo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati degli indirizzi nel database.
 */
public interface IndirizzoDao extends JpaRepository<Indirizzo, Long> {
  Optional<Indirizzo> findIndirizzoById(final Long idIndirizzo);

  Optional<List<Indirizzo>> findAllByCliente(final Cliente cliente);
}
