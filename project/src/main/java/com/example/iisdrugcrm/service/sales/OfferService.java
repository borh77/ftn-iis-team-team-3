package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.portfolio.Product;
import com.example.iisdrugcrm.domain.sales.*;
import com.example.iisdrugcrm.dto.sales.offer.*;
import com.example.iisdrugcrm.repository.portfolio.ProductRepository;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.OfferRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.iisdrugcrm.repository.sales.SalesProcessHistoryRepository;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.repository.UserRepository;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final CustomerRepository customerRepository;
    private final SalesProcessRepository salesProcessRepository;
    private final ProductRepository productRepository;
    private final SalesProcessHistoryRepository salesProcessHistoryRepository;
    private final UserRepository userRepository;

    public OfferService(
            OfferRepository offerRepository,
            CustomerRepository customerRepository,
            SalesProcessRepository salesProcessRepository,
            ProductRepository productRepository,
            SalesProcessHistoryRepository salesProcessHistoryRepository,
            UserRepository userRepository
    ) {
        this.offerRepository = offerRepository;
        this.customerRepository = customerRepository;
        this.salesProcessRepository = salesProcessRepository;
        this.productRepository = productRepository;
        this.salesProcessHistoryRepository = salesProcessHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OfferResponseDTO create(CreateOfferRequestDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        SalesProcess salesProcess = salesProcessRepository.findWithCustomerById(dto.getSalesProcessId())
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        String offerNumber = generateOfferNumber();

        Offer offer = new Offer(
                offerNumber,
                customer,
                salesProcess,
                dto.getValidUntil(),
                dto.getNotes()
        );

        for (OfferItemRequestDTO itemDto : dto.getItems()) {

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));

            OfferItem item = new OfferItem(
                    product,
                    itemDto.getQuantity(),
                    itemDto.getUnitPrice()
            );

            offer.addItem(item);
        }

        return mapToDto(offerRepository.save(offer));
    }

    @Transactional(readOnly = true)
    public List<OfferResponseDTO> getAll() {
        return offerRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OfferResponseDTO getById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found."));

        return mapToDto(offer);
    }

    private String generateOfferNumber() {

        Long nextId = offerRepository.findTopByOrderByIdDesc()
                .map(Offer::getId)
                .orElse(0L) + 1;

        return "OFF-" + String.format("%05d", nextId);
    }

    @Transactional
    public OfferResponseDTO update(Long id, UpdateOfferRequestDTO dto) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found."));

        if (offer.getStatus() == OfferStatus.ACCEPTED || offer.getStatus() == OfferStatus.REJECTED) {
            throw new IllegalArgumentException("Accepted or rejected offers cannot be edited.");
        }

        offer.update(dto.getValidUntil(), dto.getNotes());

        return mapToDto(offer);
    }

    @Transactional
    public OfferResponseDTO acceptOffer(Long id, String username) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found."));

        if (offer.getStatus() == OfferStatus.ACCEPTED) {
            throw new IllegalArgumentException("Offer is already accepted.");
        }

        SalesProcess salesProcess = offer.getSalesProcess();
        SalesStage previousStage = salesProcess.getStage();

        offer.markAsAccepted();

        if (previousStage != SalesStage.WON) {
            salesProcess.changeStage(SalesStage.WON);

            User changedBy = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

            salesProcessHistoryRepository.save(
                new SalesProcessHistory(salesProcess, previousStage, SalesStage.WON, changedBy)
            );
        }

        return mapToDto(offer);
    }

    private OfferResponseDTO mapToDto(Offer offer) {

        List<OfferItemResponseDTO> items = offer.getItems()
                .stream()
                .map(item -> new OfferItemResponseDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();

        return new OfferResponseDTO(
                offer.getId(),
                offer.getOfferNumber(),
                offer.getCustomer().getId(),
                offer.getCustomer().getName(),
                offer.getSalesProcess().getId(),
                offer.getSalesProcess().getTitle(),
                offer.getStatus(),
                offer.getValidUntil(),
                offer.getTotalAmount(),
                offer.getNotes(),
                offer.getCreatedAt(),
                items
        );
    }
}