package com.ms.bank.account.impl;

import com.ms.bank.account.api.CreditsApiDelegate;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CreditApiDelegateImpl implements CreditsApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreditApiDelegateImpl.class);

    private final CreditRepository creditRepository;

    public CreditApiDelegateImpl(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    @Override
    public ResponseEntity<ModelApiResponse> createCredit(Credit credit) {

        // Check if the client already has a credit of the same type (e.g., one personal credit per client)
        if (creditRepository.existsByClientIdAndType(credit.getClientId(), credit.getType())) {
            logger.warn("Client ID: {} already has a credit of type {}", credit.getClientId(), credit.getType());
            return ResponseUtil.getResponse(HttpStatus.CONFLICT.value(),
                    "The customer already has a credit of type " + credit.getType(), null);
        }

        Credit createdCredit = creditRepository.save(credit);
        logger.info("Credit product created successfully with ID: {}", createdCredit.getId());

        return ResponseUtil.getResponse(HttpStatus.CREATED.value(), "Credit product created successfully", createdCredit);
    }

    @Override
    public ResponseEntity<Void> deleteCredit(String creditId) {
        logger.info("Request received to delete credit product with ID: {}", creditId);

        if (!creditRepository.existsById(creditId)) {
            logger.warn("Credit product with ID: {} not found", creditId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        creditRepository.deleteById(creditId);
        logger.info("Credit product with ID: {} deleted successfully", creditId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ModelApiResponse> getCreditById(String creditId) {
        logger.info("Fetching credit product with ID: {}", creditId);

        Optional<Credit> credit = creditRepository.findById(creditId);

        return credit
                .map(c -> {
                    logger.info("Credit product found: {}", c.getId());
                    return ResponseUtil.getResponse(HttpStatus.OK.value(), "Credit product", c);
                })
                .orElseGet(() -> {
                    logger.warn("Credit product with ID: {} not found", creditId);
                    return ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Credit product", null);
                });
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAllCredits() {
        logger.info("Request received to fetch all credit products");
        List<Credit> credits = creditRepository.findAll();

        logger.info("Total credit products found: {}", credits.size());
        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of credit products", credits);
    }

    @Override
    public ResponseEntity<ModelApiResponse> getCreditsByClientId(String clientId) {
        logger.info("Fetching credits for client ID: {}", clientId);
        List<Credit> credits = creditRepository.findByClientId(clientId);

        if (credits.isEmpty()) {
            logger.warn("No credits found for client ID: {}", clientId);
            return ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "No credits found for client", null);
        }

        logger.info("Credits found for client ID: {}", clientId);
        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of client credits", credits);
    }
}
