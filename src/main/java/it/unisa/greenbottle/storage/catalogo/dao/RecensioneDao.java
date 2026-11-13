package it.unisa.greenbottle.storage.catalogo.dao;

import it.unisa.greenbottle.storage.catalogo.entity.Recensione;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati delle recensioni.
 */
public interface RecensioneDao extends JpaRepository<Recensione, Long> {
  Optional<Recensione> findRecensioneById(final Long idRecensione);
}
