package com.ms.bank.account.repository;

import com.ms.bank.account.model.BankAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends MongoRepository<BankAccount, String> {
    List<BankAccount> findByClientId(String clientId);
}