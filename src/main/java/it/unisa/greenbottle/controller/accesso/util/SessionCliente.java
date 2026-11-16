package it.unisa.greenbottle.controller.accesso.util;

import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import java.io.Serializable;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Classe SessionCliente, utilizzata per la gestione della sessione del cliente.
 */
@Component
@SessionScope
public class SessionCliente implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long idCliente;

  private transient ClienteDao clienteDao;

  /**
   * Inietta il DAO del cliente.
   *
   * @param clienteDao DAO del cliente
   */
  @Autowired
  public void setClienteDao(ClienteDao clienteDao) {
    this.clienteDao = clienteDao;
  }

  /**
   * Metodo per ottenere il cliente.
   *
   * @return cliente
   */
  @ModelAttribute("cliente")
  public Optional<Cliente> getCliente() {
    if (idCliente != null) {
      return clienteDao.findClienteById(idCliente);
    }
    return Optional.empty();
  }

  /**
   * Metodo per settare il cliente.
   *
   * @param cliente cliente da settare
   */
  public void setCliente(final Cliente cliente) {
    this.idCliente = (cliente == null) ? null : cliente.getId();
  }

  /**
   * Svuota la sessione del cliente.
   */
  public void emptyCliente() {
    this.idCliente = null;
  }
}
