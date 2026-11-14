package it.unisa.greenbottle.controller.catalogo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Questa classe gestisce le richieste GET per la home page e la pagina aboutUs.
 */
@Controller
public class HomeController {
  private static final String HOME_VIEW = "CatalogoView/Home";
  private static final String ABOUT_US_VIEW = "CatalogoView/AboutUs";

  @GetMapping(value = {"/", "/home"})
  public String home() {
    return HOME_VIEW;
  }

  @GetMapping(value = {"/aboutUs"})
  public String aboutUs() {
    return ABOUT_US_VIEW;
  }
}
