package br.com.tger.api.persistence.entity;

import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.UserProfile;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_users")
public class AppUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "erp_code")
    private String erpCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserProfile profile;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_password_reset_at")
    private Instant lastPasswordResetAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_modules", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "module_code")
    private List<ModuleCode> modules = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getErpCode() { return erpCode; }
    public void setErpCode(String erpCode) { this.erpCode = erpCode; }
    public UserProfile getProfile() { return profile; }
    public void setProfile(UserProfile profile) { this.profile = profile; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getLastPasswordResetAt() { return lastPasswordResetAt; }
    public void setLastPasswordResetAt(Instant lastPasswordResetAt) { this.lastPasswordResetAt = lastPasswordResetAt; }
    public List<ModuleCode> getModules() { return modules; }
    public void setModules(List<ModuleCode> modules) { this.modules = modules; }
}
