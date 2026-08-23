package dio.iadesafio.domain.repository;

import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.TipoTransacao;
import dio.iadesafio.domain.model.Transacao;
import dio.iadesafio.domain.voz.TransacaoId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, TransacaoId> {

    List<Transacao> findByCategoria(Categoria categoria)
            ;
    List<Transacao> findAllByOrderByCriadaEmDesc(Pageable pageable);

    List<Transacao> findByCategoriaOrderByCriadaEmDesc(Categoria categoria, Pageable pageable);

    @Query("""
           SELECT COALESCE(SUM(t.valor.quantia),0)
           FROM Transacao t
           WHERE t.tipo =:tipo           
           """)
    BigDecimal somarPorTipo(@Param("tipo")TipoTransacao tipo);

    @Query("""
            SELECT COALESCE(SUM(t.valor.quantia), 0)
            FROM Transacao t
            WHERE t.tipo = :tipo AND t.categoria = :categoria
            """)
    BigDecimal somarPorTipoECategoria(@Param("tipo") TipoTransacao tipo, @Param("categoria") Categoria categoria);

    @Query("""
            SELECT COALESCE(SUM(t.valor.quantia), 0)
            FROM Transacao t
            WHERE t.tipo = :tipo AND t.criadaEm >= :desde
            """)
    BigDecimal somarPorTipoDesde(@Param("tipo") TipoTransacao tipo, @Param("desde") LocalDateTime desde);
}
