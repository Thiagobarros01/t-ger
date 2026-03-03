package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.commercial.*;
import br.com.tger.api.dto.common.PagedResponseDto;
import br.com.tger.api.persistence.service.CommercialPersistenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commercial")
public class CommercialPersistenceController {
    private final CommercialPersistenceService service;
    public CommercialPersistenceController(CommercialPersistenceService service) { this.service = service; }

    @GetMapping("/sellers")
    public List<SellerResponseDto> sellers(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.listSellers(authorizationHeader);
    }
    @GetMapping("/sellers/paged")
    public PagedResponseDto<SellerResponseDto> sellersPaged(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String erpCode,
            @RequestParam(required = false) String email,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return service.searchSellers(authorizationHeader, name, erpCode, email, page, pageSize);
    }
    @PostMapping("/sellers")
    public SellerResponseDto createSeller(
            @Valid @RequestBody SellerRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.createSeller(req, authorizationHeader);
    }
    @PutMapping("/sellers/{id}")
    public SellerResponseDto updateSeller(
            @PathVariable Long id,
            @Valid @RequestBody SellerRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.updateSeller(id, req, authorizationHeader);
    }
    @DeleteMapping("/sellers/{id}")
    public void deleteSeller(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deleteSeller(id, authorizationHeader);
    }

    @GetMapping("/products")
    public List<ProductResponseDto> products(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.listProducts(authorizationHeader);
    }
    @GetMapping("/products/paged")
    public PagedResponseDto<ProductResponseDto> productsPaged(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String line,
            @RequestParam(required = false) String erpCode,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return service.searchProducts(authorizationHeader, description, line, erpCode, page, pageSize);
    }
    @PostMapping("/products")
    public ProductResponseDto createProduct(
            @Valid @RequestBody ProductRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.createProduct(req, authorizationHeader);
    }
    @PutMapping("/products/{id}")
    public ProductResponseDto updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.updateProduct(id, req, authorizationHeader);
    }
    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deleteProduct(id, authorizationHeader);
    }
    @PostMapping("/products/bulk-upsert")
    public BulkUpsertResultDto bulkProducts(
            @RequestBody List<ProductRequestDto> rows,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.bulkUpsertProducts(rows, authorizationHeader);
    }

    @GetMapping("/customers")
    public List<CustomerResponseDto> customers(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.listCustomers(authorizationHeader);
    }
    @GetMapping("/customers/paged")
    public PagedResponseDto<CustomerResponseDto> customersPaged(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String corporateName,
            @RequestParam(required = false) String erpCode,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String erpSellerCode,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return service.searchCustomers(authorizationHeader, corporateName, erpCode, type, erpSellerCode, page, pageSize);
    }
    @PostMapping("/customers")
    public CustomerResponseDto createCustomer(
            @Valid @RequestBody CustomerRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.createCustomer(req, authorizationHeader);
    }
    @PutMapping("/customers/{id}")
    public CustomerResponseDto updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.updateCustomer(id, req, authorizationHeader);
    }
    @DeleteMapping("/customers/{id}")
    public void deleteCustomer(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deleteCustomer(id, authorizationHeader);
    }
    @PostMapping("/customers/bulk-upsert")
    public BulkUpsertResultDto bulkCustomers(
            @RequestBody List<CustomerRequestDto> rows,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.bulkUpsertCustomers(rows, authorizationHeader);
    }
}
