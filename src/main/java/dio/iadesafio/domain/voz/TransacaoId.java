package dio.iadesafio.domain.voz;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record TransacaoId(UUID id)  implements Serializable {

    public TransacaoId {
        Objects.requireNonNull(id , "O id da transacao nao pode ser nulo");
    }

    public static TransacaoId novo () {
        return new TransacaoId(UUID.randomUUID());
    }

    public static TransacaoId de(String valor) {
        return new TransacaoId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return id.toString();
    }
}

