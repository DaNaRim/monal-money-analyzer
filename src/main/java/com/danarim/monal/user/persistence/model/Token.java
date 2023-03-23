package com.danarim.monal.user.persistence.model;

import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents a token that can be used to perform a specific action for a user.
 */
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
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    private boolean isUsed;

    protected Token() {
    }

    /**
     * Creates a new token with a random value and an expiration date.
     *
     * @param user      the user to whom the token belongs
     * @param tokenType the type of the token
     */
    public Token(User user, TokenType tokenType) {
        this.tokenValue = UUID.randomUUID().toString();
        this.user = user;
        this.tokenType = tokenType;
        this.expirationDate = calculateExpiryDate();
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

    public String getTokenValue() {
        return tokenValue;
    }

    public User getUser() {
        return user;
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
