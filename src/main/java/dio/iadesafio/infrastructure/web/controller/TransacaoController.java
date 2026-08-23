package dio.iadesafio.infrastructure.web.controller;

import dio.iadesafio.application.dto.response.ResumoPeriodoResponse;
import dio.iadesafio.application.dto.response.SaldoResponse;
import dio.iadesafio.application.dto.response.TransacaoDTO;
import dio.iadesafio.application.dto.resquest.RegistrarTransacaoRequest;
import dio.iadesafio.application.service.TransacaoService;
import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.Transacao;
import dio.iadesafio.domain.voz.Valor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<TransacaoDTO> registrar(@Valid @RequestBody RegistrarTransacaoRequest request) {
        Transacao transacao = transacaoService.registrar(
                request.descricao(),
                new Valor(request.valor()),
                request.tipo(),
                request.categoria()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TransacaoDTO.de(transacao));
    }

    @GetMapping("/saldo")
    public ResponseEntity<SaldoResponse> consultarSaldo() {
        return ResponseEntity.ok(transacaoService.consultarSaldo());
    }

    @GetMapping
    public ResponseEntity<List<TransacaoDTO>> listarHistorico(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(defaultValue = "10") int limite
    ) {
        List<TransacaoDTO> transacoes = transacaoService.consultarHistorico(categoria, limite).stream()
                .map(TransacaoDTO::de)
                .toList();
        return ResponseEntity.ok(transacoes);
    }

    @GetMapping("/categoria/{categoria}/total")
    public ResponseEntity<BigDecimal> consultarGastoPorCategoria(@PathVariable Categoria categoria) {
        return ResponseEntity.ok(transacaoService.consultarGastoPorCategoria(categoria));
    }

    @GetMapping("/periodo")
    public ResponseEntity<ResumoPeriodoResponse> consultarResumoPeriodo(
            @RequestParam(defaultValue = "30") int dias
    ) {
        return ResponseEntity.ok(transacaoService.consultarResumoPeriodo(dias));
    }
}
