package ir.netpick.platform.financefarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.financefarm.dto.TransactionDTO;
import ir.netpick.platform.financefarm.model.Transaction;
import ir.netpick.platform.financefarm.model.TransactionType;
import ir.netpick.platform.financefarm.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public PageDTO<Transaction> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("date").descending());
        Page<Transaction> page = transactionRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Transaction> getByType(TransactionType type, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("date").descending());
        Page<Transaction> page = transactionRepository.findByTypeAndDeletedFalse(type, pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Transaction> getByCreatedBy(UUID createdBy, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("date").descending());
        Page<Transaction> page = transactionRepository.findByCreatedByIdAndDeletedFalse(createdBy, pageable);
        return PageDTOMapper.map(page);
    }

    public Transaction getById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with id [%s] was not found".formatted(transactionId)));
    }

    public Transaction create(Transaction transaction) {
        transaction.setId(null);
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        return transactionRepository.save(transaction);
    }

    public Transaction update(UUID transactionId, Transaction transaction) {
        Transaction existing = getById(transactionId);
        transaction.setId(transactionId);
        transaction.setCreatedAt(existing.getCreatedAt());
        return transactionRepository.save(transaction);
    }

    public void delete(UUID transactionId) {
        transactionRepository.softDelete(transactionId);
    }

    public void restore(UUID transactionId) {
        transactionRepository.restore(transactionId);
    }

    public List<Transaction> importFromCsv(MultipartFile file, UUID createdBy) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirst = true;
            while ((line = reader.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    continue; // Skip header
                }
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    Transaction transaction = new Transaction();
                    transaction.setAmount(new BigDecimal(parts[0].trim()));
                    transaction.setType(TransactionType.valueOf(parts[1].trim().toUpperCase()));
                    transaction.setCategory(parts[2].trim());
                    transaction.setDescription(parts.length > 3 ? parts[3].trim() : "");
                    transaction.setDate(LocalDateTime.parse(parts.length > 4 ? parts[4].trim() : LocalDateTime.now().toString()));
                    transaction.setCreatedBy(createdBy);
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }
}