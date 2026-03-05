package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.commercial.*;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.dto.common.PagedResponseDto;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.entity.ProductEntity;
import br.com.tger.api.persistence.entity.SalesHistoryEntity;
import br.com.tger.api.persistence.entity.SellerEntity;
import br.com.tger.api.persistence.repository.CustomerRepository;
import br.com.tger.api.persistence.repository.ProductRepository;
import br.com.tger.api.persistence.repository.SalesHistoryRepository;
import br.com.tger.api.persistence.repository.SellerRepository;
import br.com.tger.api.service.AccessControlService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommercialPersistenceService {
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalesHistoryRepository salesHistoryRepository;
    private final AccessControlService accessControlService;

    public CommercialPersistenceService(
            SellerRepository sellerRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            SalesHistoryRepository salesHistoryRepository,
            AccessControlService accessControlService
    ) {
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesHistoryRepository = salesHistoryRepository;
        this.accessControlService = accessControlService;
    }

    public List<SellerResponseDto> listSellers(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (!accessControlService.isOperator(user)) {
            return sellerRepository.findAll().stream().map(this::toDto).toList();
        }
        SellerEntity ownSeller = resolveOperatorSeller(user);
        return ownSeller == null ? List.of() : List.of(toDto(ownSeller));
    }

    public List<ProductResponseDto> listProducts(String authorizationHeader) {
        accessControlService.requireUser(authorizationHeader);
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    public PagedResponseDto<ProductResponseDto> searchProducts(
            String authorizationHeader,
            String description,
            String line,
            String erpCode,
            Integer page,
            Integer pageSize
    ) {
        accessControlService.requireUser(authorizationHeader);
        int safePageSize = normalizePageSize(pageSize);
        int safePage = Math.max(1, page == null ? 1 : page);

        Pageable pageable = PageRequest.of(
                safePage - 1,
                safePageSize,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Specification<ProductEntity> filters = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String byDescription = trim(description);
            String byLine = trim(line);
            String byErp = trim(erpCode);

            if (byDescription != null) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + byDescription.toLowerCase() + "%"));
            }
            if (byLine != null) {
                predicates.add(cb.like(cb.lower(root.get("line")), "%" + byLine.toLowerCase() + "%"));
            }
            if (byErp != null) {
                predicates.add(cb.like(cb.lower(root.get("erpCode")), "%" + byErp.toLowerCase() + "%"));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductEntity> result = productRepository.findAll(filters, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
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

    public PagedResponseDto<SellerResponseDto> searchSellers(
            String authorizationHeader,
            String name,
            String erpCode,
            String email,
            Integer page,
            Integer pageSize
    ) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));

        if (accessControlService.isOperator(user)) {
            SellerEntity ownSeller = resolveOperatorSeller(user);
            List<SellerResponseDto> own = ownSeller == null ? List.of() : List.of(toDto(ownSeller));
            return new PagedResponseDto<>(own, 1, safePageSize, own.size(), own.isEmpty() ? 0 : 1);
        }

        Specification<SellerEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String byName = trim(name);
            String byErp = trim(erpCode);
            String byEmail = trim(email);

            if (byName != null) predicates.add(cb.like(cb.lower(root.get("name")), "%" + byName.toLowerCase() + "%"));
            if (byErp != null) predicates.add(cb.like(cb.lower(root.get("erpCode")), "%" + byErp.toLowerCase() + "%"));
            if (byEmail != null) predicates.add(cb.like(cb.lower(root.get("email")), "%" + byEmail.toLowerCase() + "%"));
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SellerEntity> result = sellerRepository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public PagedResponseDto<CustomerResponseDto> searchCustomers(
            String authorizationHeader,
            String corporateName,
            String erpCode,
            String type,
            String erpSellerCode,
            Integer page,
            Integer pageSize
    ) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));

        Specification<CustomerEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String byCorporateName = trim(corporateName);
            String byErp = trim(erpCode);
            String byType = trim(type);
            String bySellerErp = trim(erpSellerCode);

            if (byCorporateName != null) predicates.add(cb.like(cb.lower(root.get("corporateName")), "%" + byCorporateName.toLowerCase() + "%"));
            if (byErp != null) predicates.add(cb.like(cb.lower(root.get("erpCode")), "%" + byErp.toLowerCase() + "%"));
            if (byType != null) predicates.add(cb.equal(cb.upper(root.get("type")), byType.toUpperCase()));
            if (bySellerErp != null) predicates.add(cb.like(cb.lower(root.get("erpSellerCode")), "%" + bySellerErp.toLowerCase() + "%"));

            if (accessControlService.isOperator(user)) {
                SellerEntity ownSeller = resolveOperatorSeller(user);
                String ownErp = ownSeller == null ? null : trim(ownSeller.getErpCode());
                if (ownErp == null) {
                    predicates.add(cb.equal(cb.literal(1), 0));
                } else {
                    predicates.add(cb.equal(cb.lower(root.get("erpSellerCode")), ownErp.toLowerCase()));
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CustomerEntity> result = customerRepository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public List<CustomerResponseDto> listCustomersByIds(List<Long> ids, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (ids == null || ids.isEmpty()) return List.of();
        List<CustomerEntity> customers = customerRepository.findAllById(ids);
        if (accessControlService.isOperator(user)) {
            SellerEntity ownSeller = resolveOperatorSeller(user);
            String ownErp = ownSeller == null ? null : trim(ownSeller.getErpCode());
            if (ownErp == null) return List.of();
            customers = customers.stream()
                    .filter(item -> item.getErpSellerCode() != null && ownErp.equalsIgnoreCase(item.getErpSellerCode()))
                    .toList();
        }
        return customers.stream().map(this::toDto).toList();
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
    public BulkUpsertResultDto bulkUpsertSellers(List<SellerRequestDto> rows, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        int created = 0, updated = 0, errors = 0;
        List<BulkUpsertErrorDto> errorDetails = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            SellerRequestDto row = rows.get(i);
            try {
                String erp = trim(row.erpCode());
                String name = trim(row.name());
                if (erp == null || name == null) {
                    errors++;
                    addError(errorDetails, i + 1, "Codigo ERP e nome sao obrigatorios");
                    continue;
                }

                var existing = sellerRepository.findByErpCodeIgnoreCase(erp).orElse(null);
                if (existing != null) {
                    existing.setName(name);
                    existing.setEmail(trim(row.email()));
                    existing.setPhone(trim(row.phone()));
                    sellerRepository.save(existing);
                    updated++;
                    continue;
                }

                SellerEntity e = new SellerEntity();
                e.setCode("VDR-" + String.format("%05d", sellerRepository.count() + created + 1));
                e.setErpCode(erp);
                e.setName(name);
                e.setEmail(trim(row.email()));
                e.setPhone(trim(row.phone()));
                sellerRepository.save(e);
                created++;
            } catch (Exception ex) {
                errors++;
                addError(errorDetails, i + 1, normalizeExceptionMessage(ex));
            }
        }
        return new BulkUpsertResultDto(rows.size(), created, updated, errors, errorDetails);
    }

    @Transactional
    public BulkUpsertResultDto bulkUpsertProducts(List<ProductRequestDto> rows, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        int created = 0, updated = 0, errors = 0;
        List<BulkUpsertErrorDto> errorDetails = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ProductRequestDto row = rows.get(i);
            try {
                if (row.description() == null || row.description().isBlank()) {
                    errors++;
                    addError(errorDetails, i + 1, "Descricao obrigatoria");
                    continue;
                }
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
            } catch (Exception ex) {
                errors++;
                addError(errorDetails, i + 1, normalizeExceptionMessage(ex));
            }
        }
        return new BulkUpsertResultDto(rows.size(), created, updated, errors, errorDetails);
    }

    @Transactional
    public BulkUpsertResultDto bulkUpsertCustomers(List<CustomerRequestDto> rows, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        int created = 0, updated = 0, errors = 0;
        List<BulkUpsertErrorDto> errorDetails = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            CustomerRequestDto row = rows.get(i);
            try {
                if (row.corporateName() == null || row.corporateName().isBlank()) {
                    errors++;
                    addError(errorDetails, i + 1, "Razao social obrigatoria");
                    continue;
                }
                String type = trim(row.type());
                if (type == null || (!type.equalsIgnoreCase("PJ") && !type.equalsIgnoreCase("PF"))) {
                    errors++;
                    addError(errorDetails, i + 1, "Tipo deve ser PJ ou PF");
                    continue;
                }
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
            } catch (Exception ex) {
                errors++;
                addError(errorDetails, i + 1, normalizeExceptionMessage(ex));
            }
        }
        return new BulkUpsertResultDto(rows.size(), created, updated, errors, errorDetails);
    }

    @Transactional
    public BulkUpsertResultDto bulkUpsertSalesHistory(List<SalesHistoryImportRequestDto> rows, String authorizationHeader) {
        assertNotOperator(authorizationHeader);
        int created = 0, updated = 0, errors = 0;
        List<BulkUpsertErrorDto> errorDetails = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            SalesHistoryImportRequestDto row = rows.get(i);
            try {
                String key = buildSalesExternalKey(row.orderNumber(), row.nfNumber(), row.sequence());
                if (key == null) {
                    errors++;
                    addError(errorDetails, i + 1, "Numero do pedido ou NF obrigatorio");
                    continue;
                }

                SalesHistoryEntity entity = salesHistoryRepository.findByExternalKey(key).orElse(null);
                if (entity == null) {
                    entity = new SalesHistoryEntity();
                    entity.setExternalKey(key);
                    created++;
                } else {
                    updated++;
                }

                entity.setCompanyErpCode(trim(row.companyErpCode()));
                entity.setOrderNumber(trim(row.orderNumber()));
                entity.setSequence(trim(row.sequence()));
                entity.setNfNumber(trim(row.nfNumber()));
                entity.setOrderDate(row.orderDate());
                entity.setBilledDate(row.billedDate());
                entity.setReturnedDate(row.returnedDate());
                entity.setCanceledDate(row.canceledDate());
                entity.setOrderStatusCode(trim(row.orderStatusCode()));
                entity.setCustomerErpCode(trim(row.customerErpCode()));
                entity.setSellerErpCode(trim(row.sellerErpCode()));
                entity.setProductErpCode(trim(row.productErpCode()));
                entity.setQuantity(row.quantity());
                entity.setNetValue(row.netValue());
                entity.setTotalNfValue(row.totalNfValue());
                entity.setCanceledValue(row.canceledValue());
                entity.setReturnedValue(row.returnedValue());
                salesHistoryRepository.save(entity);
            } catch (Exception ex) {
                errors++;
                addError(errorDetails, i + 1, normalizeExceptionMessage(ex));
            }
        }
        return new BulkUpsertResultDto(rows.size(), created, updated, errors, errorDetails);
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

    private void addError(List<BulkUpsertErrorDto> errors, int row, String message) {
        if (errors.size() >= 1000) return;
        errors.add(new BulkUpsertErrorDto(row, message));
    }

    private String normalizeExceptionMessage(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) return "Erro ao processar registro";
        String compact = msg.replace('\n', ' ').replace('\r', ' ').trim();
        return compact.length() > 180 ? compact.substring(0, 180) + "..." : compact;
    }

    private String buildSalesExternalKey(String orderNumber, String nfNumber, String sequence) {
        String order = trim(orderNumber);
        String nf = trim(nfNumber);
        String seq = trim(sequence);
        if (order != null) return "PED:" + order + ":" + (seq == null ? "0" : seq);
        if (nf != null) return "NF:" + nf + ":" + (seq == null ? "0" : seq);
        return null;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) return 10;
        if (pageSize < 1) return 10;
        return Math.min(pageSize, 100);
    }

    private SellerEntity resolveOperatorSeller(UserDto user) {
        if (user == null) return null;
        String linkedSellerErp = trim(user.linkedSellerErpCode());
        if (linkedSellerErp != null) {
            return sellerRepository.findByErpCodeIgnoreCase(linkedSellerErp).orElse(null);
        }
        if (user.email() == null || user.email().isBlank()) return null;
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
