package com.justbinary.controller;

import com.justbinary.dto.TradeRequest;
import com.justbinary.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "*")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/place")
    public ResponseEntity<String> placeTrade(@RequestBody TradeRequest request,
                                             Authentication authentication) {
        String email = authentication.getName();
        String result = tradeService.placeTrade(email, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<String> getTradeHistory(Authentication authentication) {
        String email = authentication.getName();
        String result = tradeService.getTradeHistory(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public ResponseEntity<String> getActiveTrades(Authentication authentication) {
        String email = authentication.getName();
        String result = tradeService.getActiveTrades(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<String> getTradeById(@PathVariable String tradeId, // ← Long to String
                                               Authentication authentication) {
        String email = authentication.getName();
        String result = tradeService.getTradeById(email, tradeId);
        return ResponseEntity.ok(result);
    }
}