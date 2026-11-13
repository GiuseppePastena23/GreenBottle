package it.unisa.greenbottle.controller.catalogo;

import it.unisa.greenbottle.storage.catalogo.dao.ProdottoDao;
import it.unisa.greenbottle.storage.catalogo.entity.Prodotto;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Classe che si occupa di restituire l'immagine di un prodotto.
 * L'immagine puo essere ridimensionata alla misura richiesta, cosi il browser
 * non deve scaricare e rimpicciolire l'originale.
 */
@RestController
@RequiredArgsConstructor
public class RenderImg {

  private static final int MIN_SIZE = 50;
  private static final int MAX_SIZE = 1000;
  private static final int CACHE_MAX_ENTRIES = 300;
  private static final String CACHE_CONTROL = "public, max-age=604800";

  /**
   * Cache delle miniature gia calcolate (LRU, al massimo 300 elementi).
   */
  private final Map<String, byte[]> thumbnails = Collections.synchronizedMap(
      new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, byte[]> eldest) {
          return size() > CACHE_MAX_ENTRIES;
        }
      });

  private final ProdottoDao prodottoDao;

  /**
   * Metodo che restituisce l'immagine di un prodotto.
   *
   * @param id     id del prodotto
   * @param width  larghezza desiderata in pixel (opzionale)
   * @param height altezza desiderata in pixel (opzionale)
   * @return ResponseEntity (array di byte) immagine del prodotto
   */
  @GetMapping("/productImg")
  public ResponseEntity<byte[]> getImage(@RequestParam("id") final Long id,
                                         @RequestParam(value = "w", required = false) final Integer width,
                                         @RequestParam(value = "h", required = false) final Integer height) {
    final Optional<Prodotto> opPro = prodottoDao.findProdottoById(id);
    if (opPro.isEmpty() || opPro.get().getImg() == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    final Prodotto prodotto = opPro.get();
    byte[] image = prodotto.getImg();
    if (width != null) {
      final int w = clamp(width);
      final int h = height != null ? clamp(height) : 0;
      image = thumbnail(id, image, w, h);
    }

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");
    headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(image.length));
    headers.add(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL);
    return new ResponseEntity<>(image, headers, HttpStatus.OK);
  }

  /**
   * Restituisce la miniatura richiesta, calcolandola solo la prima volta.
   *
   * @param id       id del prodotto
   * @param original immagine originale
   * @param width    larghezza desiderata
   * @param height   altezza desiderata, 0 per mantenerne le proporzioni
   * @return byte della miniatura
   */
  private byte[] thumbnail(final Long id, final byte[] original, final int width, final int height) {
    final String key = id + ":" + width + "x" + height;
    final byte[] cached = thumbnails.get(key);
    if (cached != null) {
      return cached;
    }
    final byte[] scaled = resize(original, width, height);
    thumbnails.put(key, scaled);
    return scaled;
  }

  /**
   * Ridimensiona e ritaglia l'immagine sulla misura richiesta.
   *
   * @param original immagine originale
   * @param width    larghezza desiderata
   * @param height   altezza desiderata, 0 per mantenerne le proporzioni
   * @return byte dell'immagine ridimensionata
   */
  private byte[] resize(final byte[] original, final int width, final int height) {
    try {
      final BufferedImage source = ImageIO.read(new ByteArrayInputStream(original));
      if (source == null) {
        return original;
      }
      final int targetWidth = width;
      final int targetHeight = height > 0
          ? height
          : Math.max(1, Math.round(source.getHeight() * (float) width / source.getWidth()));
      if (source.getWidth() <= targetWidth && source.getHeight() <= targetHeight) {
        return original;
      }
      final BufferedImage resized = new BufferedImage(targetWidth, targetHeight,
          BufferedImage.TYPE_INT_RGB);
      final Graphics2D g = resized.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      final double scale = Math.max((double) targetWidth / source.getWidth(),
          (double) targetHeight / source.getHeight());
      final int drawWidth = (int) Math.ceil(source.getWidth() * scale);
      final int drawHeight = (int) Math.ceil(source.getHeight() * scale);
      g.drawImage(source, (targetWidth - drawWidth) / 2, (targetHeight - drawHeight) / 2,
          drawWidth, drawHeight, null);
      g.dispose();
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      ImageIO.write(resized, "jpg", buffer);
      return buffer.toByteArray();
    } catch (IOException e) {
      return original;
    }
  }

  /**
   * Limita la misura richiesta a un intervallo ragionevole.
   *
   * @param value misura richiesta
   * @return misura compresa tra MIN_SIZE e MAX_SIZE
   */
  private int clamp(final int value) {
    return Math.max(MIN_SIZE, Math.min(MAX_SIZE, value));
  }
}
