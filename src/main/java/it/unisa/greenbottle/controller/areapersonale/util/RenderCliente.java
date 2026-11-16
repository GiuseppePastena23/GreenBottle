package it.unisa.greenbottle.controller.areapersonale.util;

import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Classe per la gestione del caricamento delle immagini dei clienti.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RenderCliente {

  private static final String DEFAULT_IMAGE_PATH =
      "src/main/resources/static/img/defaultImgCliente.png";

  private final ClienteDao clienteDao;

  /**
   * Metodo per ottenere l'immagine di un cliente.
   * Se l'immagine non è presente, viene restituita l'immagine di default.
   *
   * @param id L'id del cliente di cui si vuole ottenere l'immagine.
   * @return L'immagine del cliente come array di byte.
   */
  @GetMapping("/userImg")
  public ResponseEntity<byte[]> getImage(@RequestParam final Long id) {
    final Optional<Cliente> optCliente = clienteDao.findClienteById(id);
    if (optCliente.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    final Cliente cliente = optCliente.get();
    final byte[] image = cliente.getImg() != null ? cliente.getImg() : loadDefaultImage();

    if (image.length == 0) {
      // Se non è possibile caricare l'immagine di default, restituisci un errore 500
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "image/jpeg");
    headers.add("Content-Length", String.valueOf(image.length));
    return new ResponseEntity<>(image, headers, HttpStatus.OK);
  }

  /**
   * Metodo per caricare l'immagine di default.
   *
   * @return L'immagine di default come array di byte, vuoto in caso di errore.
   */
  private byte[] loadDefaultImage() {
    final Path defaultImagePath = Paths.get(DEFAULT_IMAGE_PATH);
    try {
      return Files.readAllBytes(defaultImagePath);
    } catch (final IOException e) {
      log.error("Impossibile caricare l'immagine di default del cliente", e);
      return new byte[0];
    }
  }
}
