package it.unisa.greenbottle.storage.accesso.dao;

import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati dei clienti.
 */
public interface ClienteDao extends JpaRepository<Cliente, Long> {
  Optional<Cliente> findClienteById(final Long idCliente);

  Optional<Cliente> findClienteByEmail(final String email);
}