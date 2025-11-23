package ir.netpick.mailmine.scrape.service.base;

import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.scrape.model.Contact;
import ir.netpick.mailmine.scrape.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;


    public boolean isEmpty(){
        return contactRepository.count() == 0;
    }

    public PageDTO<Contact> allContacts(int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber - 1 , GeneralConstants.PAGE_SIZE, Sort.by("createdAt").descending());
        Page<Contact> page = contactRepository.findAll(pageable);
        return new PageDTO<>(
                page.getContent(),
                page.getTotalPages(),
                pageNumber
        );
    }

    public PageDTO<Contact> allContacts(int pageNumber, String sortBy , Direction direction){
        Pageable pageable = PageRequest.of(pageNumber - 1 , GeneralConstants.PAGE_SIZE, Sort.by(direction,sortBy) );
        Page<Contact> page = contactRepository.findAll(pageable);
        return new PageDTO<>(
                page.getContent(),
                page.getTotalPages(),
                pageNumber
                );
    }
    public Contact getContact(UUID contactId){
        return contactRepository.findById(contactId)
                .orElseThrow( () -> new ResourceNotFoundException("Contact with id [%s] was not found".formatted(contactId)));
    }

    public void createContact(Contact contact){
        contactRepository.save(contact);
    }

    public void updateContact(UUID contactId ,Contact contact){
        contact.setId(contactId);
        contactRepository.save(contact);

    }

    public void softDeleteContact(UUID contactId){
        contactRepository.softDelete(contactId);
    }

    public void restoreContact(UUID contactId){
        contactRepository.restore(contactId);
    }

    public void deleteContact(UUID contactId){
        contactRepository.deleteById(contactId);
    }
}
