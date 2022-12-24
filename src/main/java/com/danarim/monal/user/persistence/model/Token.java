package com.danarim.monal.user.persistence.model;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
public class Token implements Serializable {

    @Serial
    private static final long serialVersionUID = -4504331784456739668L;

    private static final int DEFAULT_EXPIRY_TIME_IN_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String tokenValue;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private Date expirationDate;

    @Column(nullable = false, updatable = false)
    private Date createdDate;

    private boolean isUsed;

    protected Token() {
    }

    public Token(User user, TokenType tokenType) {
        this.tokenValue = UUID.randomUUID().toString();
        this.user = user;
        this.tokenType = tokenType;
        this.expirationDate = calculateExpiryDate();
        this.createdDate = new Date();
        this.isUsed = false;
    }

    public boolean isExpired() {
        return expirationDate.before(Calendar.getInstance().getTime());
    }


    public TokenType getTokenType() {
        return tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpirationDate() {
        return new Date(expirationDate.getTime());
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = new Date(expirationDate.getTime());
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed() {
        isUsed = true;
    }

    private static Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, DEFAULT_EXPIRY_TIME_IN_HOURS);
        return cal.getTime();
    }
}
