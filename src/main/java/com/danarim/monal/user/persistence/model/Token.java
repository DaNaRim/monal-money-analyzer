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

    private Date expiryDate;

    protected Token() {
    }

    public Token(User user, TokenType tokenType) {
        this.tokenValue = UUID.randomUUID().toString();
        this.user = user;
        this.tokenType = tokenType;
        this.expiryDate = calculateExpiryDate();
    }

    public boolean isExpired() {
        return expiryDate.before(Calendar.getInstance().getTime());
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

    public Date getExpiryDate() {
        return new Date(expiryDate.getTime());
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = new Date(expiryDate.getTime());
    }

    private static Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, DEFAULT_EXPIRY_TIME_IN_HOURS);
        return cal.getTime();
    }
}
