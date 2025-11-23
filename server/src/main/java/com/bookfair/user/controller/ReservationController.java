package com.bookfair.user.controller;
import org.springframework.transaction.annotation.Transactional;
import com.bookfair.user.model.Reservation;
import com.bookfair.user.model.Stall;
import com.bookfair.user.model.User;
import com.bookfair.user.repository.ReservationRepository;
import com.bookfair.user.repository.StallRepository;
import com.bookfair.user.repository.UserRepository;
import com.bookfair.user.service.EmailService;
import com.bookfair.user.service.QRCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final StallRepository stallRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final EmailService emailService;

    public ReservationController(ReservationRepository reservationRepository, StallRepository stallRepository, UserRepository userRepository, QRCodeService qrCodeService, EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.stallRepository = stallRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {

        Optional<User> userOpt = userRepository.findById(request.getUserId());
        Optional<Stall> stallOpt = stallRepository.findById(request.getStallId());

        if (userOpt.isEmpty() || stallOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid user or stall ID"));
        }

        Stall stall = stallOpt.get();
        if (stall.getReserved()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Stall already reserved"));
        }

        Reservation reservation = new Reservation();
        reservation.setUser(userOpt.get());
        reservation.setStall(stall);
        reservation.setStatus("PENDING");

        stall.setReserved(true);
        stallRepository.save(stall);

        reservationRepository.save(reservation);

        return ResponseEntity.ok(Map.of(
                "message", "Reservation created successfully",
                "reservationId", reservation.getReservationId()
        ));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getReservationsByUser(@PathVariable Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);

         if (userOpt.isEmpty()) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
         }

    List<Reservation> reservations = reservationRepository.findByUser(userOpt.get());
    return ResponseEntity.ok(reservations);
}

    @GetMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Integer reservationId) {
        Optional<Reservation> resOpt = reservationRepository.findById(reservationId);

        if (resOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservation not found"));
        }

        Reservation reservation = resOpt.get();

        if (reservation.getStatus().equalsIgnoreCase("CANCELLED")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Reservation already cancelled"));
        }

        reservation.setStatus("CANCELLED");

        Stall stall = reservation.getStall();
        stall.setReserved(false);
        stallRepository.save(stall);

        reservationRepository.save(reservation);

        return ResponseEntity.ok(Map.of(
                "message", "Reservation cancelled successfully",
                "reservationId", reservationId
        ));
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<?> getReservationQr(@PathVariable Integer id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);

        if (reservationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reservation not found"));
        }

        Reservation reservation = reservationOpt.get();
        byte[] qrCodeBytes = reservation.getQrCodeImage();

        if (qrCodeBytes == null || qrCodeBytes.length == 0) {
            try {
                String qrData = "Reservation ID: " + reservation.getReservationId()
                        + " | User: " + reservation.getUser().getName()
                        + " | Stall: " + reservation.getStall().getStallCode()
                        + " | Status: " + reservation.getStatus();

                final int QR_WIDTH = 250;
                final int QR_HEIGHT = 250;

                qrCodeBytes = qrCodeService.generateQRCodeImage(qrData, QR_WIDTH, QR_HEIGHT);

                reservation.setQrCodeImage(qrCodeBytes);
                reservationRepository.save(reservation);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to generate QR code image"));
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeBytes.length);

        return new ResponseEntity<>(qrCodeBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/confirmation")
    public ResponseEntity<?> sendReservationConfirmation(@PathVariable Integer id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);

        if (reservationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reservation not found"));
        }

        Reservation reservation = reservationOpt.get();

        byte[] qrBytes = reservation.getQrCodeImage();
        if (qrBytes == null || qrBytes.length == 0) {
            try {
                String qrData = "Reservation ID: " + reservation.getReservationId()
                        + " | User: " + reservation.getUser().getName()
                        + " | Stall: " + reservation.getStall().getStallCode()
                        + " | Status: " + reservation.getStatus();

                final int QR_WIDTH = 250;
                final int QR_HEIGHT = 250;

                qrBytes = qrCodeService.generateQRCodeImage(qrData, QR_WIDTH, QR_HEIGHT);
                reservation.setQrCodeImage(qrBytes);
                reservationRepository.save(reservation);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to generate QR code"));
            }
        }

        String emailBody = """
            <h2>Bookfair Reservation Confirmation</h2>
            <p>Dear %s,</p>
            <p>Your reservation has been confirmed successfully.</p>
            <p><b>Reservation ID:</b> %d<br>
               <b>Stall:</b> %s<br>
               <b>Status:</b> %s</p>
            <p>Your QR code for entry is attached below.</p>
            <p>Thank you for being part of the Colombo International Bookfair!</p>
            """.formatted(
                reservation.getUser().getName(),
                reservation.getReservationId(),
                reservation.getStall().getStallCode(),
                reservation.getStatus()
        );

        try {
            emailService.sendReservationConfirmation(
                    reservation.getUser().getEmail(),
                    "Bookfair Reservation Confirmation",
                    emailBody,
                    qrBytes
            );

            return ResponseEntity.ok(Map.of("message", "Confirmation email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }
    }

    public static class ReservationRequest {
        private Integer userId;
        private Integer stallId;

        public Integer getUserId() {
            return userId;
        }
        public void setUserId(Integer userId) {
            this.userId = userId;
        }
        public Integer getStallId() {
            return stallId;
        }
        public void setStallId(Integer stallId) {
            this.stallId = stallId;
        }
    }
}
