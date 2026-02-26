package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.commercial.*;
import br.com.tger.api.persistence.service.CommercialPersistenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commercial")
public class CommercialPersistenceController {
    private final CommercialPersistenceService service;
    public CommercialPersistenceController(CommercialPersistenceService service) { this.service = service; }

    @GetMapping("/sellers") public List<SellerResponseDto> sellers() { return service.listSellers(); }
    @PostMapping("/sellers") public SellerResponseDto createSeller(@Valid @RequestBody SellerRequestDto req) { return service.createSeller(req); }
    @PutMapping("/sellers/{id}") public SellerResponseDto updateSeller(@PathVariable Long id, @Valid @RequestBody SellerRequestDto req) { return service.updateSeller(id, req); }
    @DeleteMapping("/sellers/{id}") public void deleteSeller(@PathVariable Long id) { service.deleteSeller(id); }

    @GetMapping("/products") public List<ProductResponseDto> products() { return service.listProducts(); }
    @PostMapping("/products") public ProductResponseDto createProduct(@Valid @RequestBody ProductRequestDto req) { return service.createProduct(req); }
    @PutMapping("/products/{id}") public ProductResponseDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDto req) { return service.updateProduct(id, req); }
    @DeleteMapping("/products/{id}") public void deleteProduct(@PathVariable Long id) { service.deleteProduct(id); }
    @PostMapping("/products/bulk-upsert") public BulkUpsertResultDto bulkProducts(@RequestBody List<ProductRequestDto> rows) { return service.bulkUpsertProducts(rows); }

    @GetMapping("/customers") public List<CustomerResponseDto> customers() { return service.listCustomers(); }
    @PostMapping("/customers") public CustomerResponseDto createCustomer(@Valid @RequestBody CustomerRequestDto req) { return service.createCustomer(req); }
    @PutMapping("/customers/{id}") public CustomerResponseDto updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequestDto req) { return service.updateCustomer(id, req); }
    @DeleteMapping("/customers/{id}") public void deleteCustomer(@PathVariable Long id) { service.deleteCustomer(id); }
    @PostMapping("/customers/bulk-upsert") public BulkUpsertResultDto bulkCustomers(@RequestBody List<CustomerRequestDto> rows) { return service.bulkUpsertCustomers(rows); }
}
