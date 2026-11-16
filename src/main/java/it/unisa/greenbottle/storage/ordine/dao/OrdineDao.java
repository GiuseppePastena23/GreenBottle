package it.unisa.greenbottle.storage.ordine.dao;

import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati degli ordini.
 */
public interface OrdineDao extends JpaRepository<Ordine, Long> {
  List<Ordine> findOrdineByStato(final Ordine.StatoSpedizione statoSpedizione);

  List<Ordine> findOrdineByCliente(final Cliente cliente);

  Optional<Ordine> findOrdineById(final Long idOrdine);
}
