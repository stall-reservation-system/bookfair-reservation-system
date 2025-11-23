package com.bookfair.user.model;

import com.constants.StallTypes;
import com.constants.StallStatuses;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "stalls")
public class Stall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stallId;

    @Column(nullable = false, unique = true)
    private String stallCode;

    private String category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Boolean isReserved;

    private String locationCoordinates;

    @OneToMany(mappedBy = "stall", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Reservation> reservations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private StallTypes size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private StallStatuses status;

    @Column(nullable = true)
    private String height;

    @Column(nullable = true)
    private String width;

    @Column(nullable = true)
    private String section;

    @Column(nullable = true)
    private Integer x;  // X-coordinate

    @Column(nullable = true)
    private Integer y;  // Y-coordinate

    @PrePersist
    public void prePersist() {
        if (stallCode == null || stallCode.isEmpty()) {
            stallCode = "ST-" + System.currentTimeMillis();
        }
        if (status == null) {
            status = StallStatuses.AVAILABLE;
        }
    }

    // Getters and Setters
    public Integer getStallId() {
        return stallId;
    }

    public void setStallId(Integer stallId) {
        this.stallId = stallId;
    }

    public String getStallCode() {
        return stallCode;
    }

    public void setStallCode(String stallCode) {
        this.stallCode = stallCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getReserved() {
        return isReserved;
    }

    public void setReserved(Boolean reserved) {
        isReserved = reserved;
    }

    public String getLocationCoordinates() {
        return locationCoordinates;
    }

    public void setLocationCoordinates(String locationCoordinates) {
        this.locationCoordinates = locationCoordinates;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public StallTypes getSize() {
        return size;
    }

    public void setSize(StallTypes size) {
        this.size = size;
    }

    public StallStatuses getStatus() {
        return status;
    }

    public void setStatus(StallStatuses status) {
        this.status = status;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
