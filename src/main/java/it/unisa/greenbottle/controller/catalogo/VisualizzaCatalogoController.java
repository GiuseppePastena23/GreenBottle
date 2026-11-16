package it.unisa.greenbottle.controller.catalogo;

import it.unisa.greenbottle.controller.catalogo.form.FiltroForm;
import it.unisa.greenbottle.controller.catalogo.util.CacheCategorie;
import it.unisa.greenbottle.controller.ordine.form.ProdottoForm;
import it.unisa.greenbottle.storage.catalogo.dao.CategoriaDao;
import it.unisa.greenbottle.storage.catalogo.dto.ProdottoCard;
import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller che si occupa di visualizzare il catalogo dei prodotti.
 */
@Controller
@RequestMapping("/catalogo")
@RequiredArgsConstructor
public class VisualizzaCatalogoController {

  private static final String CATALOGO_VIEW = "CatalogoView/Catalogo";
  private static final String ERROR_VIEW = "error";
  private static final String MESSAGE_ATTRIBUTE = "message";
  private static final String STATUS_ATTRIBUTE = "status";
  private static final String PRODOTTI_ATTRIBUTE = "prodotti";
  private static final String CATEGORIE_ATTRIBUTE = "categorie";

  private final CategoriaDao categoriaDao;
  private final CacheCategorie cacheCategorie;
  private final EntityManager entityManager;

  /**
   * Metodo che si occupa di visualizzare il catalogo dei prodotti.
   *
   * @param filtroForm          filtro per la ricerca dei prodotti
   * @param bindingResult       binding result
   * @param model               model
   * @param httpServletResponse httpServletResponse
   * @return catalogoView
   * @throws IOException IOException
   */
  @GetMapping
  public String get(@ModelAttribute @Valid final FiltroForm filtroForm,
                    final BindingResult bindingResult, final Model model,
                    final HttpServletResponse httpServletResponse) throws IOException {

    if (bindingResult.hasErrors()) {
      // Se c'è un errore specifico per un campo, gestisci il messaggio
      final FieldError fieldError = bindingResult.getFieldErrors().getFirst();
      return mostraErrore(model, httpServletResponse, HttpServletResponse.SC_BAD_REQUEST,
          fieldError.getDefaultMessage());
    }

    if (!filtroForm.isPrezzoMinMaxValid()) {
      return mostraErrore(model, httpServletResponse, HttpServletResponse.SC_BAD_REQUEST,
          "Prezzo minimo non può essere maggiore del prezzo massimo.");
    }

    final List<ProdottoCard> prodotti = cercaProdotti(filtroForm);
    if (prodotti == null) {
      return mostraErrore(model, httpServletResponse, HttpServletResponse.SC_NOT_FOUND,
          "Categoria non presente.");
    }

    final ProdottoForm prodottoForm = new ProdottoForm();
    model.addAttribute(CATEGORIE_ATTRIBUTE, cacheCategorie.getCategorie());
    model.addAttribute(PRODOTTI_ATTRIBUTE, prodotti);
    model.addAttribute("filterForm", filtroForm);
    model.addAttribute("prodottoForm", prodottoForm);
    return CATALOGO_VIEW;
  }

  /**
   * Seleziona dal database solo i dati dei prodotti mostrati nel catalogo,
   * applicando i filtri richiesti. L'immagine non viene caricata: è servita da /productImg.
   *
   * @param filtroForm filtro per la ricerca dei prodotti
   * @return lista dei prodotti, oppure null se la categoria richiesta non esiste
   */
  private List<ProdottoCard> cercaProdotti(final FiltroForm filtroForm) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
    final Root<Prodotto> root = criteriaQuery.from(Prodotto.class);
    criteriaQuery.multiselect(root.get("id"), root.get("nome"), root.get("prezzo"));

    final List<Predicate> filtri = new ArrayList<>();

    if (filtroForm.getIdCategoria() != null) {
      if (!categoriaEsiste(filtroForm.getIdCategoria())) {
        return null;
      }
      filtri.add(criteriaBuilder.equal(root.get("categoria").get("id"),
          filtroForm.getIdCategoria()));
    }

    if (filtroForm.getPrezzoMin() != null) {
      filtri.add(criteriaBuilder.greaterThanOrEqualTo(root.get("prezzo"),
          filtroForm.getPrezzoMin()));
    }

    if (filtroForm.getPrezzoMax() != null) {
      filtri.add(criteriaBuilder.lessThanOrEqualTo(root.get("prezzo"),
          filtroForm.getPrezzoMax()));
    }

    if (filtroForm.getMedia() != null) {
      filtri.add(criteriaBuilder.greaterThanOrEqualTo(root.get("mediaVoti"),
          filtroForm.getMedia()));
    }

    if (!filtri.isEmpty()) {
      criteriaQuery.where(filtri.toArray(Predicate[]::new));
    }

    return entityManager.createQuery(criteriaQuery)
        .getResultList()
        .stream()
        .map(riga -> new ProdottoCard((Long) riga[0], (String) riga[1], (Float) riga[2]))
        .toList();
  }

  /**
   * Verifica che la categoria richiesta esista: usa la lista in cache e, solo se non
   * la contiene, interroga il database.
   *
   * @param idCategoria id della categoria richiesta
   * @return true se la categoria esiste, false altrimenti
   */
  private boolean categoriaEsiste(final Long idCategoria) {
    for (final Categoria categoria : cacheCategorie.getCategorie()) {
      if (idCategoria.equals(categoria.getId())) {
        return true;
      }
    }
    return categoriaDao.existsById(idCategoria);
  }

  /**
   * Imposta gli attributi di errore e restituisce la vista di errore.
   *
   * @param model               model
   * @param httpServletResponse httpServletResponse
   * @param status              codice di stato HTTP
   * @param message             messaggio di errore
   * @return la vista di errore
   * @throws IOException IOException
   */
  private String mostraErrore(final Model model, final HttpServletResponse httpServletResponse,
                              final int status, final String message) throws IOException {
    model.addAttribute(MESSAGE_ATTRIBUTE, message);
    model.addAttribute(STATUS_ATTRIBUTE, status);
    httpServletResponse.sendError(status, message);
    return ERROR_VIEW;
  }
}
