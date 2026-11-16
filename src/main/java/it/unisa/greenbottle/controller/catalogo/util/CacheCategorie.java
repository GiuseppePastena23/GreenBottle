package it.unisa.greenbottle.controller.catalogo.util;

import it.unisa.greenbottle.storage.catalogo.dao.CategoriaDao;
import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cache in memoria dell'elenco delle categorie: sono pochi dati, quasi mai modificati,
 * ma richiesti a ogni visualizzazione del catalogo.
 */
@Component
@RequiredArgsConstructor
public class CacheCategorie {

  private static final long DURATA_MS = 60_000L;

  private final CategoriaDao categoriaDao;

  private volatile List<Categoria> categorie = List.of();
  private volatile long scadenza;

  /**
   * Restituisce le categorie, ricaricandole dal database solo quando la cache è scaduta.
   *
   * @return lista delle categorie
   */
  public List<Categoria> getCategorie() {
    if (System.currentTimeMillis() >= scadenza) {
      categorie = List.copyOf(categoriaDao.findAll());
      scadenza = System.currentTimeMillis() + DURATA_MS;
    }
    return categorie;
  }
}
