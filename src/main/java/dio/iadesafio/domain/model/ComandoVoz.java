package dio.iadesafio.domain.model;

import dio.iadesafio.domain.voz.ComandoVozId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "comando_voz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComandoVoz {

    @EmbeddedId
    @AttributeOverride(name = "valor", column = @Column(name = "id"))
    private ComandoVozId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusComando status;

    private String textoTranscrito;

    private String mensagemErro;

    @Column(nullable = false)
    private LocalDateTime recebidoEm;

    private LocalDateTime finalizadoEm;

    private ComandoVoz (ComandoVozId id) {
        this.id = id;
        this.status = StatusComando.RECEBIDO;
        this.recebidoEm = LocalDateTime.now();
    }

    public void marcarProcessando() {
        this.status = StatusComando.PROCESSANDO;
    }

    public void marcarConcluido(String textoTranscrito) {
        this.status = StatusComando.CONCLUIDO;
        this.textoTranscrito = textoTranscrito;
        this.finalizadoEm = LocalDateTime.now();
    }

    public void marcarErro(String mensagemErro) {
        this.status = StatusComando.ERRO;
        this.mensagemErro = mensagemErro;
        this.finalizadoEm = LocalDateTime.now();
    }

    public static ComandoVoz receber(ComandoVozId id) {
        return new ComandoVoz(id);
    }
}
