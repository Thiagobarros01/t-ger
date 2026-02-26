package br.com.tger.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ti_ticket_messages")
public class TiTicketMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TiTicketEntity ticket;

    @Column(nullable = false)
    private String author;

    @Column(name = "sent_at", nullable = false)
    private String sentAt;

    @Column(nullable = false, length = 4000)
    private String message;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TiTicketEntity getTicket() { return ticket; }
    public void setTicket(TiTicketEntity ticket) { this.ticket = ticket; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
