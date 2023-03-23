package com.danarim.monal.config.security.jwt;

import com.danarim.monal.user.persistence.model.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for storing JWT tokens in the database.
 */
@Entity
@Table(name = "jwt_token")
public class JwtTokenEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1800737016704034901L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    /**
     * Creates a new JwtTokenEntity that is not blocked.
     *
     * @param tokenType      JwtUtil.CLAIM_TOKEN_TYPE
     * @param expirationDate expiration date of the token
     * @param user           user that the token belongs to
     */
    public JwtTokenEntity(String tokenType, Date expirationDate, User user) {
        this.tokenType = tokenType;
        this.expirationDate = new Date(expirationDate.getTime());
        this.user = user;
    }

    public Long getId() {
        return id;
    }

}
