package dio.iadesafio.domain.repository;

import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.Transacao;
import dio.iadesafio.domain.voz.TransacaoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, TransacaoId> {

    List<Transacao> findByCategoria(Categoria categoria);
}
