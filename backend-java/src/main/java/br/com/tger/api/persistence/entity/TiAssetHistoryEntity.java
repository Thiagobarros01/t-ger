package br.com.tger.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ti_asset_history", indexes = {
        @Index(name = "idx_ti_asset_history_asset", columnList = "asset_id"),
        @Index(name = "idx_ti_asset_history_changed_at", columnList = "changed_at")
})
public class TiAssetHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private TiAssetEntity asset;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "previous_responsible_user_id")
    private Long previousResponsibleUserId;
    @Column(name = "previous_responsible_user_name")
    private String previousResponsibleUserName;
    @Column(name = "new_responsible_user_id")
    private Long newResponsibleUserId;
    @Column(name = "new_responsible_user_name")
    private String newResponsibleUserName;

    @Column(name = "previous_status")
    private String previousStatus;
    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "previous_term_id")
    private Long previousTermId;
    @Column(name = "previous_term_title")
    private String previousTermTitle;
    @Column(name = "new_term_id")
    private Long newTermId;
    @Column(name = "new_term_title")
    private String newTermTitle;

    @Column(length = 2000)
    private String note;

    @Column(name = "changed_by_name")
    private String changedByName;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @PrePersist
    void onCreate() {
        changedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TiAssetEntity getAsset() { return asset; }
    public void setAsset(TiAssetEntity asset) { this.asset = asset; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getPreviousResponsibleUserId() { return previousResponsibleUserId; }
    public void setPreviousResponsibleUserId(Long previousResponsibleUserId) { this.previousResponsibleUserId = previousResponsibleUserId; }
    public String getPreviousResponsibleUserName() { return previousResponsibleUserName; }
    public void setPreviousResponsibleUserName(String previousResponsibleUserName) { this.previousResponsibleUserName = previousResponsibleUserName; }
    public Long getNewResponsibleUserId() { return newResponsibleUserId; }
    public void setNewResponsibleUserId(Long newResponsibleUserId) { this.newResponsibleUserId = newResponsibleUserId; }
    public String getNewResponsibleUserName() { return newResponsibleUserName; }
    public void setNewResponsibleUserName(String newResponsibleUserName) { this.newResponsibleUserName = newResponsibleUserName; }
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public Long getPreviousTermId() { return previousTermId; }
    public void setPreviousTermId(Long previousTermId) { this.previousTermId = previousTermId; }
    public String getPreviousTermTitle() { return previousTermTitle; }
    public void setPreviousTermTitle(String previousTermTitle) { this.previousTermTitle = previousTermTitle; }
    public Long getNewTermId() { return newTermId; }
    public void setNewTermId(Long newTermId) { this.newTermId = newTermId; }
    public String getNewTermTitle() { return newTermTitle; }
    public void setNewTermTitle(String newTermTitle) { this.newTermTitle = newTermTitle; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}
