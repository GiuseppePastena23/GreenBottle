package it.unisa.greenbottle.controller.ordine;

import it.unisa.greenbottle.controller.ordine.util.SessionCarrello;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller per la visualizzazione del carrello.
 */
@Controller
@RequiredArgsConstructor
public class VisualizzaCarrelloController {

  private static final String CARRELLO_VIEW = "OrdineView/Carrello";

  private final SessionCarrello sessionCarrello;

  /**
   * Metodo per la visualizzazione del carrello.
   *
   * @param model modello per la view
   * @return la view del carrello
   */
  @GetMapping("/carrello")
  public String get(final Model model) {
    model.addAttribute("carrello", sessionCarrello.getRealCarrello());
    return CARRELLO_VIEW;
  }
}
