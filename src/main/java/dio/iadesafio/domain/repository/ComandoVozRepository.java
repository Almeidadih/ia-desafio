package dio.iadesafio.domain.repository;

import dio.iadesafio.domain.model.ComandoVoz;
import dio.iadesafio.domain.voz.ComandoVozId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComandoVozRepository extends JpaRepository<ComandoVoz, ComandoVozId> {
}
