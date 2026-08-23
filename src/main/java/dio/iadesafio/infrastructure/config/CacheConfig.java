package dio.iadesafio.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import dio.iadesafio.application.service.TransacaoService;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Cache local (em memoria) usado para nao recalcular o saldo a cada
 * chamada de TransacaoService.consultarSaldo() - a somatoria de todas as
 * transacoes e' uma query que so muda quando uma nova transacao e
 * registrada, entao vale a pena manter o resultado por alguns segundos.
 * O nome do cache (CACHE_SALDO) e' definido no TransacaoService (camada de
 * aplicacao), nao aqui - a infraestrutura depende da aplicacao, nunca o
 * contrario.
 *
 * E' um cache LOCAL (por instancia da aplicacao), diferente do Redis
 * (usado para o status de processamento, que precisa ser compartilhado
 * entre instancias). Nao faz sentido usar Redis aqui: o saldo e barato
 * de recalcular e o ganho de latencia de um cache em memoria e maior
 * do que ir ate o Redis pela rede.
 */
@Configuration
@EnableCaching
public class CacheConfig {


    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(TransacaoService.CACHE_SALDO);
        manager.setCaffeine(caffeineSaldoSpec());
        return manager;
    }

    private Caffeine<Object, Object> caffeineSaldoSpec() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30))
                .maximumSize(100);
    }
}
