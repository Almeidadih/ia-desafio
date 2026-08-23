package dio.iadesafio.application.service;

import dio.iadesafio.application.dto.response.StatusComandoResponse;
import dio.iadesafio.domain.exception.ComandoVozNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Guarda o status "vivo" de cada comando de voz no Redis, para que o
 * cliente possa consultar via polling sem sobrecarregar o Postgres.
 * TTL evita acumulo indefinido de chaves.
 */
@Service
public class StatusProcessamentoService {

    private static final Logger log = LoggerFactory.getLogger(StatusProcessamentoService.class);
    private static final String PREFIXO_CHAVE = "comando-voz:status:";
    private static final Duration TTL = Duration.ofHours(2);

    private final RedisTemplate<String, Object> redisTemplate;

    public StatusProcessamentoService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void salvar(String comandoId, StatusComandoResponse status) {
        String chave = PREFIXO_CHAVE + comandoId;
        redisTemplate.opsForValue().set(chave, status, TTL);
        log.debug("Status salvo no Redis. chave={}, status={}", chave, status.status());
    }

    public StatusComandoResponse buscar(String comandoId) {
        String chave = PREFIXO_CHAVE + comandoId;
        Object valor = redisTemplate.opsForValue().get(chave);

        if (valor == null) {
            throw new ComandoVozNaoEncontradoException(comandoId);
        }

        return (StatusComandoResponse) valor;
    }

}
