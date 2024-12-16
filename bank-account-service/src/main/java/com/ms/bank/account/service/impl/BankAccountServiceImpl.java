package com.ms.bank.account.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.account.api.AccountsApiDelegate;
import com.ms.bank.account.client.ClientServiceFeignClient;
import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.Client;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ms.bank.account.util.ResponseUtil.buildConflictResponse;
import static com.ms.bank.account.util.ResponseUtil.buildBadRequestResponse;
import static com.ms.bank.account.util.ResponseUtil.buildCreatedResponse;

@Service
@Transactional
public class BankAccountServiceImpl implements AccountsApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    private final ClientServiceFeignClient clientServiceFeignClient;
    private final BankAccountRepository bankAccountRepository;
    private final CreditRepository creditRepository;

    public BankAccountServiceImpl(ClientServiceFeignClient clientServiceFeignClient,
                                  BankAccountRepository bankAccountRepository,
                                  CreditRepository creditRepository) {
        this.clientServiceFeignClient = clientServiceFeignClient;
        this.bankAccountRepository = bankAccountRepository;
        this.creditRepository = creditRepository;
    }

    @Override
    public ResponseEntity<ModelApiResponse> createAccount(BankAccount bankAccount) {
        if (hasExistingAccount(bankAccount)) {
            return buildConflictResponse("The client already has an account of type " + bankAccount.getType());
        }

        Client client = getClientById(bankAccount.getClientId());
        if (!isValidAccountForClient(client, bankAccount)) {
            return buildConflictResponse("Business client can't have this type of account, just current account");
        }

        if (requiresCreditCard(client) && !hasCreditCard(bankAccount.getClientId())) {
            return buildBadRequestResponse("Client must have a credit card to open this type of account");
        }

        configureAccountByType(bankAccount);

        bankAccount.setBalance(Objects.isNull(bankAccount.getBalance()) ? 0d : bankAccount.getBalance());
        BankAccount createdAccount = bankAccountRepository.save(bankAccount);
        logger.info("Bank account created successfully with ID: {}", createdAccount.getId());

        return buildCreatedResponse("Account created successfully", createdAccount);
    }

    private boolean hasExistingAccount(BankAccount bankAccount) {
        return bankAccountRepository.findByClientId(bankAccount.getClientId())
                .stream()
                .anyMatch(bk -> bk.getType().getValue().equals(bankAccount.getType().getValue()));
    }

    private Client getClientById(String clientId) {
        Object clientObj = clientServiceFeignClient.getClientById(clientId).getData();
        return new ObjectMapper().convertValue(clientObj, Client.class);
    }

    private boolean isValidAccountForClient(Client client, BankAccount bankAccount) {
        return !client.getType().equals(Client.TypeEnum.BUSINESS) || isCurrentAccount(bankAccount);
    }

    private boolean isCurrentAccount(BankAccount bankAccount) {
        return bankAccount.getType().getValue().equals(BankAccount.TypeEnum.CURRENT.getValue());
    }

    private boolean hasCreditCard(String clientId) {
        return Boolean.TRUE.equals(Mono.zip(
                        Mono.just(creditRepository.existsByClientIdAndType(clientId, Credit.TypeEnum.PERSONAL)),
                        Mono.just(creditRepository.existsByClientIdAndType(clientId, Credit.TypeEnum.BUSINESS)))
                .map(t -> t.getT1() || t.getT2())
                .block());
    }

    private boolean requiresCreditCard(Client client) {
        return client.getProfile().equals(Client.ProfileEnum.VIP) || client.getProfile().equals(Client.ProfileEnum.PYME);
    }

    private void configureAccountByType(BankAccount bankAccount) {
        switch (bankAccount.getType()) {
            case SAVINGS:
                bankAccount.setCommissionFree(true);
                bankAccount.setMovementsFree(false);
                bankAccount.setMovementsLimit(10);
                break;
            case CURRENT:
                bankAccount.setCommissionFree(false);
                bankAccount.setCommissionAmount(20.0);
                bankAccount.setMovementsFree(true);
                break;
            case FIXED_TERM:
                bankAccount.setCommissionFree(true);
                bankAccount.setMovementsFree(false);
                bankAccount.setMovementsLimit(1);
                break;
            default:
                logger.error("Unknown account type: {}", bankAccount.getType());
                break;
        }
    }

    @Override
    public ResponseEntity<Void> deleteAccount(String accountId) {

        if (!bankAccountRepository.existsById(accountId)) {
            logger.warn("Bank account with ID: {} not found", accountId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        bankAccountRepository.deleteById(accountId);
        logger.info("Bank account with ID: {} deleted successfully", accountId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAccountById(String accountId) {

        Optional<BankAccount> account = bankAccountRepository.findById(accountId);

        return account
                .map(bankAccount -> {
                    logger.info("Bank account found: {}", bankAccount.getId());
                    return ResponseUtil.getResponse(HttpStatus.OK.value(), "Account", bankAccount);
                })
                .orElseGet(() -> {
                    logger.warn("Bank account with ID: {} not found", accountId);
                    return ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Account", null);
                });
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAllAccounts() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();

        logger.info("Total bank accounts found: {}", bankAccounts.size());
        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of bank accounts", bankAccounts);
    }
}
