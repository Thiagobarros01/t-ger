package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.SalesHistoryEntity;
import br.com.tger.api.dto.commercial.SalesHistorySyncProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

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
}
