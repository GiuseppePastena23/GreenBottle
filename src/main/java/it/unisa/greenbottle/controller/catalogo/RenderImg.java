package it.unisa.greenbottle.controller.catalogo;

import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Classe che si occupa di restituire l'immagine di un prodotto.
 */
@RestController
@RequiredArgsConstructor
public class RenderImg {

  private final ProdottoDao prodottoDao;

  /**
   * Metodo che restituisce l'immagine di un prodotto.
   *
   * @param id id del prodotto
   * @return ResponseEntity (array di byte) immagine del prodotto
   */
  @GetMapping("/productImg")
  public ResponseEntity<byte[]> getImage(@RequestParam("id") final Long id) {
    final Optional<Prodotto> opPro = prodottoDao.findProdottoById(id);
    if (opPro.isEmpty() || opPro.get().getImg() == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    final Prodotto prodotto = opPro.get();
    final byte[] image = prodotto.getImg();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "image/jpeg");
    headers.add("Content-Length", String.valueOf(image.length));
    return new ResponseEntity<>(image, headers, HttpStatus.OK);
  }
}
