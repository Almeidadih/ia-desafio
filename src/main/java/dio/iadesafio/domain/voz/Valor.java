package dio.iadesafio.domain.voz;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Valor(BigDecimal quantia) {

    public Valor {
        if (quantia == null) {
            throw new IllegalArgumentException("A quantia nao pode ser nulo");
        }
        if (quantia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A quantia nao pode ser negativo");
        }
        quantia = quantia.setScale(2, RoundingMode.HALF_UP);
    }

    public static  Valor de(double quantia) {
        return new Valor(BigDecimal.valueOf(quantia));
    }
    public  static Valor zero(){
        return new Valor(BigDecimal.ZERO);
    }
    public  Valor somar(Valor outro){
        return new Valor(this.quantia.add(outro.quantia));
    }
    public  Valor subtrair(Valor outro){
        return new Valor(this.quantia.subtract(outro.quantia));
    }
}
