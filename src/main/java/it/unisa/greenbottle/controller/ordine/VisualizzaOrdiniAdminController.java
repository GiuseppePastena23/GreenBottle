package it.unisa.greenbottle.controller.ordine;

import it.unisa.greenbottle.controller.ordine.util.OrdineWrapper;
import it.unisa.greenbottle.storage.ordine.dao.ComposizioneDao;
import it.unisa.greenbottle.storage.ordine.dao.OrdineDao;
import it.unisa.greenbottle.storage.ordine.entity.Composizione;
import it.unisa.greenbottle.storage.ordine.entity.Ordine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Questa classe si occupa di gestire la visualizzazione degli ordini da parte dell'admin.
 */
@Controller
@RequestMapping("/admin/visualizzaOrdini")
@RequiredArgsConstructor
public class VisualizzaOrdiniAdminController {

  private static final String VISUALIZZA_ORDINI_VIEW = "OrdineView/VisualizzaOrdiniAdmin";
  private static final String ORDINI_ATTRIBUTE = "ordini";
  private static final String STATO_SPEDIZIONE_ATTRIBUTE = "StatoSpedizione";

  private final ComposizioneDao composizioneDao;
  private final OrdineDao ordineDao;

  /**
   * Questo metodo si occupa di visualizzare gli ordini da parte dell'admin.
   *
   * @param model modello che contiene i dati da visualizzare
   * @return la view da visualizzare
   */
  @GetMapping
  public String get(final Model model) {
    final List<Ordine> ordiniDaElaborare =
        ordineDao.findOrdineByStato(Ordine.StatoSpedizione.ELABORAZIONE);

    final Map<Ordine, List<Composizione>> composizioniPerOrdine =
        composizioneDao.findComposizioneByOrdineIn(ordiniDaElaborare).stream()
            .collect(Collectors.groupingBy(Composizione::getOrdine));

    final List<OrdineWrapper> ordiniFinale = new ArrayList<>();
    for (final Ordine ordine : ordiniDaElaborare) {
      ordiniFinale.add(new OrdineWrapper(ordine,
          composizioniPerOrdine.getOrDefault(ordine, List.of())));
    }

    model.addAttribute(ORDINI_ATTRIBUTE, ordiniFinale);
    model.addAttribute(STATO_SPEDIZIONE_ATTRIBUTE, Ordine.StatoSpedizione.class);
    return VISUALIZZA_ORDINI_VIEW;
  }
}
