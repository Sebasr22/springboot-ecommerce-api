package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.port.in.TokenizeCardUseCase;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.TokenizeCardRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.TokenResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CreditCardRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for credit card tokenization.
 * Provides endpoint for securely tokenizing credit card information.
 *
 * SECURITY CRITICAL: This endpoint handles sensitive PCI data.
 */
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cards", description = "Credit card tokenization API")
public class CardController {

    private final TokenizeCardUseCase tokenizeCardUseCase;
    private final CreditCardRestMapper mapper;

    @PostMapping("/tokenize")
    @Operation(
        summary = "Tokenize credit card",
        description = "Tokenizes credit card information for secure payment processing. " +
                      "Full card number is NEVER stored, only the generated token (encrypted) and masked card info."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Card tokenized successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation errors",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing API key"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Tokenization failed - internal error"
        )
    })
    public ResponseEntity<TokenResponse> tokenizeCard(
            @Valid @RequestBody TokenizeCardRequest request
    ) {
        log.info("Tokenizing credit card ending in: {}",
                request.getCardNumber().substring(Math.max(0, request.getCardNumber().length() - 4)));

        CreditCard creditCard = mapper.toDomain(request);
        CreditCard tokenized = tokenizeCardUseCase.tokenize(creditCard);

        TokenResponse response = mapper.toTokenResponse(tokenized);
        log.info("Card tokenized successfully: {}", response.getToken());

        return ResponseEntity.ok(response);
    }
}
