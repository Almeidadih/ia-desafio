package dio.iadesafio.domain.model;

import dio.iadesafio.domain.voz.TransacaoId;
import dio.iadesafio.domain.voz.Valor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transacao {

    @EmbeddedId
    @AttributeOverride(name = "valor", column = @Column(name = "id"))
    private TransacaoId id;

    @Column(nullable = false)
    private String descricao;

    @Embedded
    @AttributeOverride(name = "quantia", column = @Column(name = "valor", nullable = false))
    private Valor valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    private Transacao(String descricao, Valor valor, TipoTransacao tipo, Categoria categoria) {
        this.id = TransacaoId.novo();
        this.descricao = validarDescricao(descricao);
        this.valor = validarValor(valor);
        this.tipo = validarTipo(tipo);
        this.categoria = validarCategoria(categoria);
        this.criadaEm = LocalDateTime.now();
    }

    public static Transacao registrar(String descricao, Valor valor, TipoTransacao tipo, Categoria categoria) {
        return new Transacao(descricao, valor, tipo, categoria);
    }

    private String validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new TransacaoInvalidaException("A descricao da transacao nao pode ser vazia");
        }
        return descricao;
    }

    private Valor validarValor(Valor valor) {
        if (valor == null || valor.quantia().signum() <= 0) {
            throw new TransacaoInvalidaException("O valor da transacao deve ser maior que zero");
        }
        return valor;
    }

    private TipoTransacao validarTipo(TipoTransacao tipo) {
        if (tipo == null) {
            throw new TransacaoInvalidaException("O tipo da transacao (RECEITA ou DESPESA) e obrigatorio");
        }
        return tipo;
    }

    private Categoria validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new TransacaoInvalidaException("A categoria da transacao e obrigatoria");
        }
        return categoria;
    }
}

}
