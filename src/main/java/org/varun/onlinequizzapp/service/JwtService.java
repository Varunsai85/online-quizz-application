package org.varun.onlinequizzapp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.varun.onlinequizzapp.model.User;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private final String secret;

    public JwtService() {
        this.secret = generateSecret();
    }

    public String generateJwtToken(String subject) {
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (jwtExpirationMs)))
                .signWith(getKey())
                .compact();
    }

    private String generateSecret() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey key = keyGen.generateKey();
            return Encoders.BASE64.encode(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        Claims claims=extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(UserDetails userDetails, String token) {
        String subject=extractSubject(token);
        return ((subject.equals(((User) userDetails).getEmail()) || subject.equals(userDetails.getUsername())) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        return extractClaim(token,Claims::getExpiration).before(new Date());
    }
}
