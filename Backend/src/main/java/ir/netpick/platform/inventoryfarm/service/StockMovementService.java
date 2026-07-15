package ir.netpick.platform.inventoryfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.inventoryfarm.dto.StockMovementDTO;
import ir.netpick.platform.inventoryfarm.model.StockMovement;
import ir.netpick.platform.inventoryfarm.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockMovementService {
    private final StockMovementRepository stockMovementRepository;

    public PageDTO<StockMovement> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("movementDate").descending());
        Page<StockMovement> page = stockMovementRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<StockMovement> getByProduct(UUID productId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("movementDate").descending());
        Page<StockMovement> page = stockMovementRepository.findByProductIdAndDeletedFalse(productId, pageable);
        return PageDTOMapper.map(page);
    }

    public List<StockMovement> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return stockMovementRepository.findByDateBetweenAndDeletedFalse(startDate, endDate);
    }

    public StockMovement getById(UUID movementId) {
        return stockMovementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("StockMovement with id [%s] was not found".formatted(movementId)));
    }

    public StockMovement create(StockMovement movement) {
        movement.setId(null);
        if (movement.getMovementDate() == null) {
            movement.setMovementDate(LocalDateTime.now());
        }
        return stockMovementRepository.save(movement);
    }

    public void delete(UUID movementId) {
        stockMovementRepository.softDelete(movementId);
    }

    public void restore(UUID movementId) {
        stockMovementRepository.restore(movementId);
    }
}