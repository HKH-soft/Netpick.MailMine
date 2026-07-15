package ir.netpick.platform.dealfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.dealfarm.model.Deal;
import ir.netpick.platform.dealfarm.model.DealStage;
import ir.netpick.platform.dealfarm.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealService {
    private final DealRepository dealRepository;

    public PageDTO<Deal> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Deal> page = dealRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Deal> getByStage(DealStage stage, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Deal> page = dealRepository.findByStageAndDeletedFalse(stage, pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<Deal> getByOwnerId(UUID ownerId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Deal> page = dealRepository.findByOwnerIdAndDeletedFalse(ownerId, pageable);
        return PageDTOMapper.map(page);
    }

    public Deal getById(UUID dealId) {
        return dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal with id [%s] was not found".formatted(dealId)));
    }

    public Deal create(Deal deal) {
        deal.setId(null);
        return dealRepository.save(deal);
    }

    public Deal update(UUID dealId, Deal deal) {
        Deal existing = getById(dealId);
        deal.setId(dealId);
        deal.setCreatedAt(existing.getCreatedAt());
        return dealRepository.save(deal);
    }

    public void delete(UUID dealId) {
        dealRepository.softDelete(dealId);
    }

    public void restore(UUID dealId) {
        dealRepository.restore(dealId);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDeals", dealRepository.countByDeletedFalse());
        
        BigDecimal totalValue = BigDecimal.ZERO;
        for (DealStage stage : DealStage.values()) {
            BigDecimal stageValue = dealRepository.sumValueByStage(stage);
            if (stageValue != null) {
                totalValue = totalValue.add(stageValue);
            }
        }
        stats.put("totalValue", totalValue);
        
        long wonDeals = dealRepository.countByStageAndDeletedFalse(DealStage.CLOSED_WON);
        long totalClosed = wonDeals + dealRepository.countByStageAndDeletedFalse(DealStage.CLOSED_LOST);
        double winRate = totalClosed > 0 ? (double) wonDeals / totalClosed * 100 : 0;
        stats.put("winRate", winRate);
        
        Map<String, Long> dealsByStage = new HashMap<>();
        for (DealStage stage : DealStage.values()) {
            dealsByStage.put(stage.name(), dealRepository.countByStageAndDeletedFalse(stage));
        }
        stats.put("dealsByStage", dealsByStage);
        
        return stats;
    }
}