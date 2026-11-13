package it.unisa.greenbottle.controller.catalogo;

import it.unisa.greenbottle.controller.catalogo.form.FiltroForm;
import it.unisa.greenbottle.controller.ordine.form.ProdottoForm;
import it.unisa.greenbottle.storage.catalogo.dao.CategoriaDao;
import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Categoria;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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

  private final ProdottoDao prodottoDao;
  private final CategoriaDao categoriaDao;

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

    Specification<Prodotto> spec = Specification.where(null);

    if (filtroForm.getIdCategoria() != null) {
      final Optional<Categoria> categoriaOpt =
          categoriaDao.findCategoriaById(filtroForm.getIdCategoria());

      if (categoriaOpt.isEmpty()) {
        return mostraErrore(model, httpServletResponse, HttpServletResponse.SC_NOT_FOUND,
            "Categoria non presente.");
      }

      final Categoria categoria = categoriaOpt.get();
      spec = spec.and((final Root<Prodotto> root, final CriteriaQuery<?> query,
                       final CriteriaBuilder criteriaBuilder) ->
          criteriaBuilder.equal(root.get("categoria"), categoria));
    }

    if (filtroForm.getPrezzoMin() != null) {
      spec = spec.and((final Root<Prodotto> root, final CriteriaQuery<?> query,
                       final CriteriaBuilder criteriaBuilder) ->
          criteriaBuilder.greaterThanOrEqualTo(root.get("prezzo"), filtroForm.getPrezzoMin()));
    }

    if (filtroForm.getPrezzoMax() != null) {
      spec = spec.and((final Root<Prodotto> root, final CriteriaQuery<?> query,
                       final CriteriaBuilder criteriaBuilder) ->
          criteriaBuilder.lessThanOrEqualTo(root.get("prezzo"), filtroForm.getPrezzoMax()));
    }

    if (filtroForm.getMedia() != null) {
      spec = spec.and((final Root<Prodotto> root, final CriteriaQuery<?> query,
                       final CriteriaBuilder criteriaBuilder) ->
          criteriaBuilder.greaterThanOrEqualTo(root.get("mediaVoti"), filtroForm.getMedia()));
    }

    final List<Prodotto> prodotti = prodottoDao.findAll(spec);
    final ProdottoForm prodottoForm = new ProdottoForm();
    model.addAttribute(CATEGORIE_ATTRIBUTE, categoriaDao.findAll());
    model.addAttribute(PRODOTTI_ATTRIBUTE, prodotti);
    model.addAttribute("filterForm", filtroForm);
    model.addAttribute("prodottoForm", prodottoForm);
    return CATALOGO_VIEW;
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
