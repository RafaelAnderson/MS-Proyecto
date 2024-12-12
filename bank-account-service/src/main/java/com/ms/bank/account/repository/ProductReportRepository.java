package com.ms.bank.account.repository;

import com.ms.bank.account.model.ProductReport;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductReportRepository extends MongoRepository<ProductReport, String> {
}
