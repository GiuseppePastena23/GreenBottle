package it.unisa.greenbottle.storage.accesso.dao;

import it.unisa.greenbottle.storage.accesso.entity.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati degli amministratori.
 */
public interface AdminDao extends JpaRepository<Admin, Long> {
  Optional<Admin> findAdminByEmail(final String email);
}
