package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.SalesHistoryEntity;
import br.com.tger.api.dto.commercial.SalesHistorySyncProjection;
import br.com.tger.api.crm.dto.CrmCustomerOrderHistoryProjection;
import br.com.tger.api.crm.dto.CrmCustomerTopProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

public interface SalesHistoryRepository extends JpaRepository<SalesHistoryEntity, Long> {
    Optional<SalesHistoryEntity> findByExternalKey(String externalKey);

    @Query(value = """
            select
                sh.customer_erp_code as customerErpCode,
                sh.seller_erp_code as sellerErpCode,
                max(sh.order_date) as lastOrderDate,
                (array_agg(sh.order_number order by sh.order_date desc, sh.id desc))[1] as lastOrderNumber,
                (array_agg(sh.order_status_code order by sh.order_date desc, sh.id desc))[1] as lastOrderStatusCode,
                (array_agg(sh.billed_date order by sh.order_date desc, sh.id desc))[1] as lastBilledDate,
                (array_agg(sh.returned_date order by sh.order_date desc, sh.id desc))[1] as lastReturnedDate,
                (array_agg(sh.canceled_date order by sh.order_date desc, sh.id desc))[1] as lastCanceledDate,
                (array_agg(coalesce(sh.net_value, sh.total_nf_value, 0) order by sh.order_date desc, sh.id desc))[1] as lastOrderValue,
                sum(case when sh.order_date >= (current_date - interval '90 day')
                    then coalesce(sh.net_value, sh.total_nf_value, 0)
                    else 0 end) as value90d,
                count(case when sh.order_date >= (current_date - interval '90 day') then 1 end) as orders90d
            from sales_history sh
            where sh.order_date is not null
              and sh.customer_erp_code is not null and sh.customer_erp_code <> ''
              and sh.seller_erp_code is not null and sh.seller_erp_code <> ''
            group by sh.customer_erp_code, sh.seller_erp_code
            """, nativeQuery = true)
    List<SalesHistorySyncProjection> summarizeLastOrderByCustomerSeller();

    @Query("""
            select max(sh.orderDate)
            from SalesHistoryEntity sh
            where lower(sh.customerErpCode) = lower(:customerErpCode)
              and sh.orderDate is not null
            """)
    LocalDate findLastOrderDateByCustomerErpCode(@Param("customerErpCode") String customerErpCode);

    @Query("""
            select count(sh.id)
            from SalesHistoryEntity sh
            where lower(sh.customerErpCode) = lower(:customerErpCode)
            """)
    long countByCustomerErpCode(@Param("customerErpCode") String customerErpCode);

    @Query(value = """
            select
                sh.product_erp_code as productErpCode,
                max(coalesce(p.description, '')) as productName,
                sum(coalesce(sh.quantity, 1)) as totalQuantity,
                count(sh.id) as totalOrders
            from sales_history sh
            left join products p
              on lower(p.erp_code) = lower(sh.product_erp_code)
            where lower(sh.customer_erp_code) = lower(:customerErpCode)
              and sh.product_erp_code is not null and sh.product_erp_code <> ''
            group by sh.product_erp_code
            order by sum(coalesce(sh.quantity, 1)) desc, count(sh.id) desc, sh.product_erp_code asc
            limit 1
            """, nativeQuery = true)
    Optional<CrmCustomerTopProductProjection> findTopProductByCustomerErpCode(@Param("customerErpCode") String customerErpCode);

    @Query(value = """
            select
                sh.id as historyId,
                sh.order_date as orderDate,
                sh.order_number as orderNumber,
                sh.order_status_code as orderStatusCode,
                sh.product_erp_code as productErpCode,
                coalesce(p.description, sh.product_erp_code) as productName,
                sh.quantity as quantity,
                coalesce(sh.net_value, sh.total_nf_value, 0) as totalValue
            from sales_history sh
            left join products p
              on lower(p.erp_code) = lower(sh.product_erp_code)
            where lower(sh.customer_erp_code) = lower(:customerErpCode)
            order by sh.order_date desc nulls last, sh.id desc
            """,
            countQuery = """
            select count(1)
            from sales_history sh
            where lower(sh.customer_erp_code) = lower(:customerErpCode)
            """,
            nativeQuery = true)
    Page<CrmCustomerOrderHistoryProjection> findOrderHistoryByCustomerErpCode(
            @Param("customerErpCode") String customerErpCode,
            Pageable pageable
    );
}
