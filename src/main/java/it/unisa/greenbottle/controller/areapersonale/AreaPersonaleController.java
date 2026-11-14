package it.unisa.greenbottle.controller.areapersonale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller che si occupa di gestire le richieste relative all'area personale.
 */
@Controller
@RequestMapping("/areaPersonale")
public class AreaPersonaleController {

  private static final String AREA_PERSONALE_VIEW = "AreaPersonaleView/AreaPersonale";

  @GetMapping
  public String get(final Model model) {
    return AREA_PERSONALE_VIEW;
  }
}
