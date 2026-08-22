package dio.iadesafio.domain.voz;



import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


public record ComandoVozId(UUID id) implements Serializable {

    public ComandoVozId {
        Objects.requireNonNull(id, "O id do comando de voz nao pode ser nulo");
    }
    public static  ComandoVozId novo() {
        return new ComandoVozId(UUID.randomUUID());
    }
    public static ComandoVozId de(String id) {
        return new ComandoVozId(UUID.fromString(id));
    }


    @Override
    public String toString() {
        return id.toString();
    }
}
