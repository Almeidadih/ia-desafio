package dio.iadesafio.application.service;

import dio.iadesafio.application.dto.response.ResumoPeriodoResponse;
import dio.iadesafio.application.dto.response.SaldoResponse;
import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.TipoTransacao;
import dio.iadesafio.domain.model.Transacao;
import dio.iadesafio.domain.repository.TransacaoRepository;
import dio.iadesafio.domain.voz.Valor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso central de transacoes financeiras: registrar, consultar saldo,
 * historico, gasto por categoria e resumo por periodo.
 *
 * Ponto unico de verdade para essa logica - tanto as tools de IA
 * (infrastructure/ai/tools) quanto o TransacaoController (REST) chamam
 * este servico, evitando duplicar regra de negocio e garantindo que o
 * cache de saldo funcione da mesma forma nos dois caminhos de entrada.
 */

@Service
public class TransacaoService {

    /** Nome do cache de saldo - definido aqui (camada de aplicacao) e usado
     *  pelo CacheConfig (infraestrutura) para configurar o Caffeine. */
    public static final String CACHE_SALDO = "saldo";

    private static final Logger log = LoggerFactory.getLogger(TransacaoService.class);

    private final TransacaoRepository transacaoRepository;

    public TransacaoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    @CacheEvict(value = CACHE_SALDO, allEntries = true)
    public Transacao registrar(String descricao, Valor valor, TipoTransacao tipo, Categoria categoria) {
        Transacao transacao = Transacao.registrar(descricao, valor, tipo, categoria);
        transacaoRepository.save(transacao);
        log.info("Transacao registrada. id={}, tipo={}, categoria={}, valor={}",
                transacao.getId(), tipo, categoria, valor.quantia());
        return transacao;
    }

    @Cacheable(CACHE_SALDO)
    public SaldoResponse consultarSaldo() {
        BigDecimal receitas = transacaoRepository.somarPorTipo(TipoTransacao.RECEITA);
        BigDecimal despesas = transacaoRepository.somarPorTipo(TipoTransacao.DESPESA);
        return new SaldoResponse(receitas, despesas, receitas.subtract(despesas));
    }

    public List<Transacao> consultarHistorico(Categoria categoria, int limite) {
        Pageable pagina = PageRequest.of(0, Math.max(1, limite));
        return categoria == null
                ? transacaoRepository.findAllByOrderByCriadaEmDesc(pagina)
                : transacaoRepository.findByCategoriaOrderByCriadaEmDesc(categoria, pagina);
    }

    public BigDecimal consultarGastoPorCategoria(Categoria categoria) {
        return transacaoRepository.somarPorTipoECategoria(TipoTransacao.DESPESA, categoria);
    }

    public ResumoPeriodoResponse consultarResumoPeriodo(int dias) {
        LocalDateTime desde = LocalDateTime.now().minusDays(Math.max(1, dias));
        BigDecimal receitas = transacaoRepository.somarPorTipoDesde(TipoTransacao.RECEITA, desde);
        BigDecimal despesas = transacaoRepository.somarPorTipoDesde(TipoTransacao.DESPESA, desde);
        return new ResumoPeriodoResponse(dias, receitas, despesas, receitas.subtract(despesas));
    }
}
