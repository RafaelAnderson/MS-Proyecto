package com.ms.bank.account.service.impl;

import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.service.CreditService;
import com.ms.bank.account.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class CreditServiceImpl implements CreditService {

    private static final Logger logger = LoggerFactory.getLogger(CreditServiceImpl.class);

    private final CreditRepository creditRepository;

    public CreditServiceImpl(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    public Mono<ResponseEntity<ModelApiResponse>> createCredit(Credit credit) {
        return Mono.just(creditRepository.existsByClientIdAndType(credit.getClientId(), credit.getType()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        logger.warn("Client ID: {} already has a credit of type {}", credit.getClientId(), credit.getType());
                        return Mono.just(ResponseUtil.getResponse(
                                HttpStatus.CONFLICT.value(),
                                "The customer already has a credit of type " + credit.getType(),
                                null));
                    }

                    credit.setStatus(Credit.StatusEnum.ACTIVE);
                    return Mono.fromCallable(() -> creditRepository.save(credit))
                            .doOnSuccess(savedCredit -> logger.info("Credit product created successfully with ID: {}", savedCredit.getId()))
                            .map(savedCredit -> ResponseUtil.getResponse(HttpStatus.CREATED.value(),
                                    "Credit product created successfully", savedCredit));
                });
    }

    public Mono<ResponseEntity<Void>> deleteCredit(String creditId) {
        logger.info("Request received to delete credit product with ID: {}", creditId);

        return Mono.fromCallable(() -> creditRepository.existsById(creditId))
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        logger.warn("Credit product with ID: {} not found", creditId);
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                    }

                    return Mono.fromRunnable(() -> creditRepository.deleteById(creditId))
                            .doOnTerminate(() -> logger.info("Credit product with ID: {} deleted successfully", creditId))
                            .then(Mono.just(ResponseEntity.ok().build()));
                });
    }


    public Mono<ResponseEntity<ModelApiResponse>> getCreditById(String creditId) {

        return Mono.fromCallable(() -> creditRepository.findById(creditId))
                .flatMap(creditOpt -> {
                    if (creditOpt.isPresent()) {
                        Credit credit = creditOpt.get();
                        logger.info("Credit product found: {}", credit.getId());
                        return Mono.just(ResponseUtil.getResponse(HttpStatus.OK.value(), "Credit", credit));
                    } else {
                        return Mono.just(ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Credit", null));
                    }
                });
    }


    public Mono<ResponseEntity<ModelApiResponse>> getAllCredits() {

        return Flux.fromIterable(creditRepository.findAll())
                .collectList()
                .map(credits -> {
                    logger.info("Total credit products found: {}", credits.size());
                    return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of credit products", credits);
                });
    }

    public Mono<ResponseEntity<ModelApiResponse>> getCreditsByClientId(String clientId) {
        logger.info("Fetching credits for client ID: {}", clientId);

        return Flux.fromIterable(creditRepository.findByClientId(clientId))
                .collectList()
                .flatMap(credits -> {
                    if (credits.isEmpty()) {
                        logger.warn("No credits found for client ID: {}", clientId);
                        return Mono.just(ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(),
                                "No credits found for client", null));
                    }

                    logger.info("Credits found for client ID: {}", clientId);
                    return Mono.just(ResponseUtil.getResponse(HttpStatus.OK.value(),
                            "List of client credits", credits));
                });
    }
}
