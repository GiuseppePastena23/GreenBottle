package it.unisa.greenbottle.storage.catalogo.dao;

import it.unisa.greenbottle.storage.catalogo.dto.ProdottoDettaglio;
import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Interfaccia per la gestione dei dati dei prodotti nel database.
 */
public interface ProdottoDao extends JpaRepository<Prodotto, Long> {
  List<Prodotto> findProdottoByCategoria(final Categoria categoria);

  List<Prodotto> findAll(final Specification<Prodotto> spec);

  Optional<Prodotto> findProdottoById(final Long idProdotto);

  /**
   * Restituisce l'immagine di un prodotto senza caricare l'entità completa.
   *
   * @param idProdotto id del prodotto
   * @return byte dell'immagine, se presente
   */
  @Query("select p.img from Prodotto p where p.id = :idProdotto")
  Optional<byte[]> findImgById(@Param("idProdotto") Long idProdotto);

  /**
   * Restituisce i dati di un prodotto per la scheda prodotto, senza caricare l'immagine.
   *
   * @param idProdotto id del prodotto
   * @return dettaglio del prodotto, se presente
   */
  @Query("""
      select new it.unisa.greenbottle.storage.catalogo.dto.ProdottoDettaglio(
        p.id, p.nome, p.descrizione, p.prezzo, p.quantita, p.mediaVoti, c.id, c.nome)
      from Prodotto p left join p.categoria c
      where p.id = :idProdotto
      """)
  Optional<ProdottoDettaglio> findDettaglioById(@Param("idProdotto") Long idProdotto);
}
