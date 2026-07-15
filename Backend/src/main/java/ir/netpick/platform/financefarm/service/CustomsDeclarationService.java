package ir.netpick.platform.financefarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.financefarm.model.CustomsDeclaration;
import ir.netpick.platform.financefarm.repository.CustomsDeclarationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomsDeclarationService {

    private final CustomsDeclarationRepository customsDeclarationRepository;

    public PageDTO<CustomsDeclaration> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("declarationDate").descending());
        Page<CustomsDeclaration> page = customsDeclarationRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<CustomsDeclaration> getByStatus(String status, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("declarationDate").descending());
        Page<CustomsDeclaration> page = customsDeclarationRepository.findByStatusAndDeletedFalse(status, pageable);
        return PageDTOMapper.map(page);
    }

    public CustomsDeclaration getByDeclarationNumber(String declarationNumber) {
        return customsDeclarationRepository.findByDeclarationNumber(declarationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customs declaration with number [%s] was not found".formatted(declarationNumber)));
    }

    public CustomsDeclaration getById(UUID id) {
        return customsDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customs declaration with id [%s] was not found".formatted(id)));
    }

    public CustomsDeclaration create(CustomsDeclaration declaration) {
        declaration.calculateDuty();
        declaration.calculateVat();
        declaration.calculateTotalTax();
        return customsDeclarationRepository.save(declaration);
    }

    public CustomsDeclaration update(UUID id, CustomsDeclaration declaration) {
        CustomsDeclaration existing = getById(id);
        existing.setDeclarationNumber(declaration.getDeclarationNumber());
        existing.setInvoiceId(declaration.getInvoiceId());
        existing.setDeclarationDate(declaration.getDeclarationDate());
        existing.setCustomsOffice(declaration.getCustomsOffice());
        existing.setOriginCountry(declaration.getOriginCountry());
        existing.setDestinationCountry(declaration.getDestinationCountry());
        existing.setHsCode(declaration.getHsCode());
        existing.setProductDescription(declaration.getProductDescription());
        existing.setQuantity(declaration.getQuantity());
        existing.setUnit(declaration.getUnit());
        existing.setCurrency(declaration.getCurrency());
        existing.setCustomsValue(declaration.getCustomsValue());
        existing.setDutyRate(declaration.getDutyRate());
        existing.setVatRate(declaration.getVatRate());
        existing.setStatus(declaration.getStatus());
        existing.setTrackingNumber(declaration.getTrackingNumber());
        
        existing.calculateDuty();
        existing.calculateVat();
        existing.calculateTotalTax();
        
        return customsDeclarationRepository.save(existing);
    }

    public void delete(UUID id) {
        CustomsDeclaration declaration = getById(id);
        declaration.setDeleted(true);
        customsDeclarationRepository.save(declaration);
    }

    public CustomsDeclaration submitForApproval(UUID id) {
        CustomsDeclaration declaration = getById(id);
        declaration.setStatus("SUBMITTED");
        return customsDeclarationRepository.save(declaration);
    }

    public CustomsDeclaration approve(UUID id) {
        CustomsDeclaration declaration = getById(id);
        declaration.setStatus("APPROVED");
        return customsDeclarationRepository.save(declaration);
    }
}