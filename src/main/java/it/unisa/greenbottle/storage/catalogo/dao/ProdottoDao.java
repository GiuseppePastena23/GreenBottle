package it.unisa.greenbottle.storage.catalogo.dao;

import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaccia per la gestione dei dati dei prodotti nel database.
 */
public interface ProdottoDao extends JpaRepository<Prodotto, Long> {
  List<Prodotto> findProdottoByCategoria(final Categoria categoria);

  List<Prodotto> findAll(final Specification<Prodotto> spec);

  Optional<Prodotto> findProdottoById(final Long idProdotto);
}
