package com.justbinary.service;

import com.justbinary.dto.TradeRequest;
import com.justbinary.model.Trade;
import com.justbinary.model.User;
import com.justbinary.repository.TradeRepository;
import com.justbinary.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    public TradeServiceImpl(TradeRepository tradeRepository,
                            UserRepository userRepository) {
        this.tradeRepository = tradeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String placeTrade(String email, TradeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trade trade = new Trade();
        trade.setUserId(user.getId());
        trade.setAsset(request.getAssetSymbol());        // ← assetSymbol not asset
        trade.setAmount(request.getAmount().doubleValue()); // ← BigDecimal to Double
        trade.setDirection(request.getDirection());
        trade.setDurationSeconds(request.getDurationSeconds());
        trade.setStatus("ACTIVE");
        tradeRepository.save(trade);

        return "Trade placed successfully";
    }

    @Override
    public String getTradeHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Trade> trades = tradeRepository.findByUserId(user.getId());
        return trades.toString();
    }

    @Override
    public String getActiveTrades(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Trade> trades = tradeRepository.findByUserIdAndStatus(
                                user.getId(), "ACTIVE");
        return trades.toString();
    }

    @Override
    public String getTradeById(String email, String tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));
        return trade.toString();
    }
}