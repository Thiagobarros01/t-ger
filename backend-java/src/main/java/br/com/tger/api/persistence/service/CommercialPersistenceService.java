package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.commercial.*;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.entity.ProductEntity;
import br.com.tger.api.persistence.entity.SellerEntity;
import br.com.tger.api.persistence.repository.CustomerRepository;
import br.com.tger.api.persistence.repository.ProductRepository;
import br.com.tger.api.persistence.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommercialPersistenceService {
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public CommercialPersistenceService(SellerRepository sellerRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public List<SellerResponseDto> listSellers() { return sellerRepository.findAll().stream().map(this::toDto).toList(); }
    public List<ProductResponseDto> listProducts() { return productRepository.findAll().stream().map(this::toDto).toList(); }
    public List<CustomerResponseDto> listCustomers() { return customerRepository.findAll().stream().map(this::toDto).toList(); }

    public SellerResponseDto createSeller(SellerRequestDto req) {
        SellerEntity e = new SellerEntity();
        e.setCode("VDR-" + String.format("%05d", sellerRepository.count() + 1));
        e.setErpCode(req.erpCode().trim());
        e.setName(req.name().trim());
        e.setEmail(trim(req.email()));
        e.setPhone(trim(req.phone()));
        return toDto(sellerRepository.save(e));
    }

    public SellerResponseDto updateSeller(Long id, SellerRequestDto req) {
        SellerEntity e = sellerRepository.findById(id).orElseThrow();
        e.setErpCode(req.erpCode().trim());
        e.setName(req.name().trim());
        e.setEmail(trim(req.email()));
        e.setPhone(trim(req.phone()));
        return toDto(sellerRepository.save(e));
    }

    public void deleteSeller(Long id) { sellerRepository.deleteById(id); }

    public ProductResponseDto createProduct(ProductRequestDto req) {
        ProductEntity e = new ProductEntity();
        apply(req, e);
        e.setCode("PRD-" + String.format("%05d", productRepository.count() + 1));
        return toDto(productRepository.save(e));
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto req) {
        ProductEntity e = productRepository.findById(id).orElseThrow();
        apply(req, e);
        return toDto(productRepository.save(e));
    }

    public void deleteProduct(Long id) { productRepository.deleteById(id); }

    public CustomerResponseDto createCustomer(CustomerRequestDto req) {
        CustomerEntity e = new CustomerEntity();
        apply(req, e);
        e.setCode("CLI-" + String.format("%05d", customerRepository.count() + 1));
        return toDto(customerRepository.save(e));
    }

    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto req) {
        CustomerEntity e = customerRepository.findById(id).orElseThrow();
        apply(req, e);
        return toDto(customerRepository.save(e));
    }

    public void deleteCustomer(Long id) { customerRepository.deleteById(id); }

    @Transactional
    public BulkUpsertResultDto bulkUpsertProducts(List<ProductRequestDto> rows) {
        int created = 0, updated = 0, errors = 0;
        for (ProductRequestDto row : rows) {
            try {
                if (row.description() == null || row.description().isBlank()) { errors++; continue; }
                String erp = trim(row.erpCode());
                if (erp != null) {
                    var existing = productRepository.findByErpCodeIgnoreCase(erp).orElse(null);
                    if (existing != null) { apply(row, existing); updated++; continue; }
                }
                ProductEntity e = new ProductEntity();
                apply(row, e);
                e.setCode("PRD-" + String.format("%05d", productRepository.count() + created + 1));
                productRepository.save(e);
                created++;
            } catch (Exception ex) { errors++; }
        }
        return new BulkUpsertResultDto(rows.size(), created, updated, errors);
    }

    @Transactional
    public BulkUpsertResultDto bulkUpsertCustomers(List<CustomerRequestDto> rows) {
        int created = 0, updated = 0, errors = 0;
        for (CustomerRequestDto row : rows) {
            try {
                if (row.corporateName() == null || row.corporateName().isBlank()) { errors++; continue; }
                String type = trim(row.type());
                if (type == null || (!type.equalsIgnoreCase("PJ") && !type.equalsIgnoreCase("PF"))) { errors++; continue; }
                String erp = trim(row.erpCode());
                if (erp != null) {
                    var existing = customerRepository.findByErpCodeIgnoreCase(erp).orElse(null);
                    if (existing != null) { apply(row, existing); updated++; continue; }
                }
                CustomerEntity e = new CustomerEntity();
                apply(row, e);
                e.setCode("CLI-" + String.format("%05d", customerRepository.count() + created + 1));
                customerRepository.save(e);
                created++;
            } catch (Exception ex) { errors++; }
        }
        return new BulkUpsertResultDto(rows.size(), created, updated, errors);
    }

    private void apply(ProductRequestDto req, ProductEntity e) {
        e.setErpCode(trim(req.erpCode()));
        e.setDescription(req.description().trim());
        e.setDepartment(trim(req.department()));
        e.setCategory(trim(req.category()));
        e.setLine(trim(req.line()));
        e.setManufacturer(trim(req.manufacturer()));
    }

    private void apply(CustomerRequestDto req, CustomerEntity e) {
        e.setErpCode(trim(req.erpCode()));
        e.setCorporateName(req.corporateName().trim());
        e.setEmail(trim(req.email()));
        e.setType(req.type().trim().toUpperCase());
        e.setTradeName(trim(req.tradeName()));
        e.setPhone(trim(req.phone()));
        e.setErpSellerCode(trim(req.erpSellerCode()));
    }

    private String trim(String v) { return v == null || v.isBlank() ? null : v.trim(); }

    private SellerResponseDto toDto(SellerEntity e) { return new SellerResponseDto(e.getId(), e.getCode(), e.getErpCode(), e.getName(), e.getEmail(), e.getPhone()); }
    private ProductResponseDto toDto(ProductEntity e) { return new ProductResponseDto(e.getId(), e.getCode(), e.getErpCode(), e.getDescription(), e.getDepartment(), e.getCategory(), e.getLine(), e.getManufacturer()); }
    private CustomerResponseDto toDto(CustomerEntity e) { return new CustomerResponseDto(e.getId(), e.getCode(), e.getErpCode(), e.getCorporateName(), e.getEmail(), e.getType(), e.getTradeName(), e.getPhone(), e.getErpSellerCode()); }
}
