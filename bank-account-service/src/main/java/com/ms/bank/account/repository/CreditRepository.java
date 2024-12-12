package com.ms.bank.account.repository;

import com.ms.bank.account.model.Credit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditRepository extends MongoRepository<Credit, String> {
    boolean existsByClientIdAndType(String clientId, Credit.TypeEnum type);

    List<Credit> findByClientId(String clientId);
}
