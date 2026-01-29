package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UnifiedCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import org.springframework.stereotype.Service;

/**
 * Service for validating and retrieving asset information from Alpaca.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AlpacaClientFactory clientFactory;
    private final UnifiedCredentialsService credentialsService;

    /**
     * Validates that a stock symbol exists and is tradeable on Alpaca.
     *
     * @param userId User ID for credentials
     * @param symbol Stock symbol to validate
     * @return true if the symbol is valid and tradeable
     */
    public boolean isSymbolTradeable(Long userId, String symbol) {
        try {
            Assets asset = getAsset(userId, symbol);
            return asset != null
                    && asset.getTradable()
                    && Assets.StatusEnum.ACTIVE.equals(asset.getStatus());
        } catch (AlpacaException e) {
            if (e.getErrorCode() == AlpacaException.ErrorCode.INVALID_SYMBOL) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Gets asset information for a symbol.
     *
     * @param userId User ID for credentials
     * @param symbol Stock symbol
     * @return Asset information
     * @throws AlpacaException if symbol not found or API error
     */
    public Assets getAsset(Long userId, String symbol) {
        log.debug("Fetching asset info for symbol: {}", symbol);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);

            return client.trader().assets().getV2AssetsSymbolOrAssetId(symbol.toUpperCase());
        } catch (net.jacobpeterson.alpaca.openapi.trader.ApiException e) {
            if (e.getCode() == 404) {
                log.warn("Symbol not found: {}", symbol);
                throw new AlpacaException(
                        AlpacaException.ErrorCode.INVALID_SYMBOL,
                        "Symbol not found: " + symbol
                );
            }
            log.error("Failed to fetch asset for symbol: {}", symbol, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to validate symbol: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            log.error("Failed to fetch asset for symbol: {}", symbol, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to validate symbol: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Validates that a symbol exists and is tradeable.
     * Throws an exception if validation fails.
     *
     * @param userId User ID for credentials
     * @param symbol Stock symbol to validate
     * @throws AlpacaException if symbol is not found or not tradeable
     */
    public void validateSymbolTradeable(Long userId, String symbol) {
        Assets asset = getAsset(userId, symbol);

        if (!asset.getTradable()) {
            throw new AlpacaException(
                    AlpacaException.ErrorCode.INVALID_SYMBOL,
                    "Symbol '" + symbol + "' is not tradeable on Alpaca"
            );
        }

        if (!Assets.StatusEnum.ACTIVE.equals(asset.getStatus())) {
            throw new AlpacaException(
                    AlpacaException.ErrorCode.INVALID_SYMBOL,
                    "Symbol '" + symbol + "' is not active (status: " + asset.getStatus() + ")"
            );
        }

        log.debug("Symbol {} validated successfully: tradeable={}, status={}",
                symbol, asset.getTradable(), asset.getStatus());
    }
}
