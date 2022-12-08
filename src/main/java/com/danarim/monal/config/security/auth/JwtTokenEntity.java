package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.model.User;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "jwt_token")
@Inheritance(strategy = InheritanceType.JOINED)
public class JwtTokenEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1800737016704034901L;

    @Id
    @GeneratedValue
    private Long id; //jti

    @Column(nullable = false, updatable = false)
    private String tokenType; //JwtUtil.CLAIM_TOKEN_TYPE

    @Column(nullable = false, updatable = false)
    private Date expirationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    private Boolean isBlocked = false; // Blocked tokens can't be used for authentication

    protected JwtTokenEntity() {
    }

    public JwtTokenEntity(String tokenType, Date expirationDate, User user) {
        this.tokenType = tokenType;
        this.expirationDate = new Date(expirationDate.getTime());
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Date getExpirationDate() {
        return new Date(expirationDate.getTime());
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = new Date(expirationDate.getTime());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
    }
}
