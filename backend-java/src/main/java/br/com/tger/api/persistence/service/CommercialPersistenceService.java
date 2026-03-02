package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.commercial.*;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.entity.ProductEntity;
import br.com.tger.api.persistence.entity.SellerEntity;
import br.com.tger.api.persistence.repository.CustomerRepository;
import br.com.tger.api.persistence.repository.ProductRepository;
import br.com.tger.api.persistence.repository.SellerRepository;
import br.com.tger.api.service.AccessControlService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommercialPersistenceService {
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AccessControlService accessControlService;

    public CommercialPersistenceService(
            SellerRepository sellerRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            AccessControlService accessControlService
    ) {
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.accessControlService = accessControlService;
    }

    public List<SellerResponseDto> listSellers(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (!accessControlService.isOperator(user)) {
            return sellerRepository.findAll().stream().map(this::toDto).toList();
        }
        return sellerRepository.findByEmailIgnoreCase(user.email()).stream().map(this::toDto).toList();
    }

    public List<ProductResponseDto> listProducts(String authorizationHeader) {
        accessControlService.requireUser(authorizationHeader);
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<CustomerResponseDto> listCustomers(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (!accessControlService.isOperator(user)) {
            return customerRepository.findAll().stream().map(this::toDto).toList();
        }
        SellerEntity ownSeller = resolveOperatorSeller(user);
        if (ownSeller == null || ownSeller.getErpCode() == null || ownSeller.getErpCode().isBlank()) {
            return List.of();
        }
        return customerRepository.findByErpSellerCodeIgnoreCase(ownSeller.getErpCode()).stream().map(this::toDto).toList();
    }

    public SellerResponseDto createSeller(SellerRequestDto req, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        SellerEntity e = new SellerEntity();
        e.setCode("VDR-" + String.format("%05d", sellerRepository.count() + 1));
        e.setErpCode(req.erpCode().trim());
        e.setName(req.name().trim());
        e.setEmail(trim(req.email()));
        e.setPhone(trim(req.phone()));
        return toDto(sellerRepository.save(e));
    }

    public SellerResponseDto updateSeller(Long id, SellerRequestDto req, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        SellerEntity e = sellerRepository.findById(id).orElseThrow();
        e.setErpCode(req.erpCode().trim());
        e.setName(req.name().trim());
        e.setEmail(trim(req.email()));
        e.setPhone(trim(req.phone()));
        return toDto(sellerRepository.save(e));
    }

    public void deleteSeller(Long id, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        sellerRepository.deleteById(id);
    }

    public ProductResponseDto createProduct(ProductRequestDto req, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        ProductEntity e = new ProductEntity();
        apply(req, e);
        e.setCode("PRD-" + String.format("%05d", productRepository.count() + 1));
        return toDto(productRepository.save(e));
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto req, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        ProductEntity e = productRepository.findById(id).orElseThrow();
        apply(req, e);
        return toDto(productRepository.save(e));
    }

    public void deleteProduct(Long id, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        productRepository.deleteById(id);
    }

    public CustomerResponseDto createCustomer(CustomerRequestDto req, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        CustomerEntity e = new CustomerEntity();
        apply(req, e, user);
        e.setCode("CLI-" + String.format("%05d", customerRepository.count() + 1));
        return toDto(customerRepository.save(e));
    }

    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto req, String authorizationHeader) {
        CustomerEntity e = customerRepository.findById(id).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsCustomer(user, e);
        apply(req, e, user);
        return toDto(customerRepository.save(e));
    }

    public void deleteCustomer(Long id, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (accessControlService.isOperator(user)) {
            CustomerEntity customer = customerRepository.findById(id).orElseThrow();
            assertOperatorOwnsCustomer(user, customer);
        }
        customerRepository.deleteById(id);
    }

    @Transactional
    public BulkUpsertResultDto bulkUpsertProducts(List<ProductRequestDto> rows, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
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
    public BulkUpsertResultDto bulkUpsertCustomers(List<CustomerRequestDto> rows, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        int created = 0, updated = 0, errors = 0;
        for (CustomerRequestDto row : rows) {
            try {
                if (row.corporateName() == null || row.corporateName().isBlank()) { errors++; continue; }
                String type = trim(row.type());
                if (type == null || (!type.equalsIgnoreCase("PJ") && !type.equalsIgnoreCase("PF"))) { errors++; continue; }
                String erp = trim(row.erpCode());
                if (erp != null) {
                    var existing = customerRepository.findByErpCodeIgnoreCase(erp).orElse(null);
                    if (existing != null) { apply(row, existing, null); updated++; continue; }
                }
                CustomerEntity e = new CustomerEntity();
                apply(row, e, null);
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

    private void apply(CustomerRequestDto req, CustomerEntity e, UserDto user) {
        String erpSellerCode = trim(req.erpSellerCode());
        if (accessControlService.isOperator(user)) {
            SellerEntity ownSeller = resolveOperatorSeller(user);
            if (ownSeller == null || ownSeller.getErpCode() == null || ownSeller.getErpCode().isBlank()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador sem vendedor vinculado");
            }
            erpSellerCode = ownSeller.getErpCode();
        }
        e.setErpCode(trim(req.erpCode()));
        e.setCorporateName(req.corporateName().trim());
        e.setEmail(trim(req.email()));
        e.setType(req.type().trim().toUpperCase());
        e.setTradeName(trim(req.tradeName()));
        e.setPhone(trim(req.phone()));
        e.setErpSellerCode(erpSellerCode);
    }

    private String trim(String v) { return v == null || v.isBlank() ? null : v.trim(); }

    private SellerEntity resolveOperatorSeller(UserDto user) {
        if (user == null || user.email() == null || user.email().isBlank()) return null;
        return sellerRepository.findByEmailIgnoreCase(user.email()).orElse(null);
    }

    private void assertNotOperator(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        accessControlService.assertNotOperator(user);
    }

    private void assertOperatorOwnsCustomer(UserDto user, CustomerEntity customer) {
        if (!accessControlService.isOperator(user)) return;
        SellerEntity ownSeller = resolveOperatorSeller(user);
        String ownErp = ownSeller == null ? null : ownSeller.getErpCode();
        if (ownErp == null || customer.getErpSellerCode() == null || !ownErp.equalsIgnoreCase(customer.getErpSellerCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador sem acesso a esse cliente");
        }
    }

    private SellerResponseDto toDto(SellerEntity e) { return new SellerResponseDto(e.getId(), e.getCode(), e.getErpCode(), e.getName(), e.getEmail(), e.getPhone()); }
    private ProductResponseDto toDto(ProductEntity e) { return new ProductResponseDto(e.getId(), e.getCode(), e.getErpCode(), e.getDescription(), e.getDepartment(), e.getCategory(), e.getLine(), e.getManufacturer()); }
    private CustomerResponseDto toDto(CustomerEntity e) { return new CustomerResponseDto(e.getId(), e.getCode(), e.getErpCode(), e.getCorporateName(), e.getEmail(), e.getType(), e.getTradeName(), e.getPhone(), e.getErpSellerCode()); }
}
