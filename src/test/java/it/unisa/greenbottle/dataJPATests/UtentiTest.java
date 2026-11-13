package it.unisa.greenbottle.dataJPATests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.greenbottle.storage.accesso.dao.AdminDao;
import it.unisa.greenbottle.storage.accesso.dao.ClienteDao;
import it.unisa.greenbottle.storage.accesso.entity.Admin;
import it.unisa.greenbottle.storage.accesso.entity.Cliente;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Testa la creazione di un cliente e di un admin.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UtentiTest {

  @Autowired
  private ClienteDao clienteDao;


  @Autowired
  private AdminDao adminDao;


  Cliente clienteTest =
      new Cliente("mario.rossi@gmail.com", "asdfASDF1234!", "Mario", "Rossi", 0, 0, null, null);

  Admin adminTest = new Admin("admin1@greenbottle.it", "asdfASDF1234!");


  @Test
  void createCliente() {
    clienteDao.save(clienteTest);

    Optional<Cliente> c2 = clienteDao.findClienteByEmail("mario.rossi@gmail.com");

    assertTrue(c2.isPresent());
    assertEquals(clienteTest, c2.get());
  }

  @Test
  void createAdmin() {
    adminDao.save(adminTest);

    Optional<Admin> a2 = adminDao.findAdminByEmail("admin1@greenbottle.it");

    assertTrue(a2.isPresent());
    assertEquals(adminTest, a2.get());
  }
}
