package it.unisa.greenbottle.controller.accesso.util;

import it.unisa.greenbottle.storage.accesso.dao.AdminDao;
import it.unisa.greenbottle.storage.accesso.entity.Admin;
import java.io.Serializable;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Classe SessionAdmin, utilizzata per la gestione della sessione dell'admin.
 */
@Component
@SessionScope
public class SessionAdmin implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long idAdmin;

  private transient AdminDao adminDao;

  /**
   * Inietta il DAO dell'admin.
   *
   * @param adminDao DAO dell'admin
   */
  @Autowired
  public void setAdminDao(AdminDao adminDao) {
    this.adminDao = adminDao;
  }

  /**
   * Metodo per settare l'admin.
   *
   * @param admin admin da settare
   */
  public void setAdmin(final Admin admin) {
    if (admin == null) {
      this.idAdmin = null;
    } else {
      this.idAdmin = admin.getId();
    }
  }

  /**
   * Metodo per ottenere l'admin.
   *
   * @return admin
   */
  @ModelAttribute("admin")
  public Optional<Admin> getAdmin() {
    if (idAdmin != null) {
      return adminDao.findById(idAdmin);
    }
    return Optional.empty();
  }

  /**
   * Svuota la sessione dell'admin.
   */
  public void emptyAdmin() {
    this.idAdmin = null;
  }
}
