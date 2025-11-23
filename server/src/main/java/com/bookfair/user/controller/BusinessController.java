package com.bookfair.user.controller;

import com.bookfair.user.model.Business;
import com.bookfair.user.model.Stall;
import com.bookfair.user.repository.BusinessRepository;
import com.bookfair.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/business")
@CrossOrigin
public class BusinessController {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public BusinessController(BusinessRepository businessRepository, UserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

//    @GetMapping()
//    public Map<Integer, Boolean> getBusinesses(@RequestParam(required = false) Integer userId) {
//
//        List<Business> businesses = businessRepository.findAll();
//        Map<Integer, Boolean> result = new LinkedHashMap<>();
//
//        if (userId == null) {
//            for (Business b : businesses) {
//                result.put(b.getBusinessId(), false);
//            }
//
//            return result;
//        }
//
//        Optional<Integer> userBusinessId = userRepository.findById(userId)
//                .map(user -> user.getBusiness() != null ? user.getBusiness().getBusinessId() : null);
//
//        for (Business b : businesses) {
//            if (userBusinessId.isPresent() && Objects.equals(userBusinessId.get(), b.getBusinessId())) {
//                result.put(b.getBusinessId(), true);
//            } else {
//                result.put(b.getBusinessId(), false);
//            }
//        }
//
//        return result;
//    }

    /**
     * Get all businesses - returns list with id and name
     */
    @GetMapping()
    public ResponseEntity<List<Map<String, Object>>> getBusinesses() {
        List<Business> businesses = businessRepository.findAll();

        List<Map<String, Object>> result = businesses.stream()
                .map(b -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", b.getBusinessId());
                    map.put("name", b.getName());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<?> getBusinessById(@PathVariable Integer businessId) {
        return businessRepository.findById(businessId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Business not found")));
    }

    @PostMapping
    public ResponseEntity<Business> createBusiness(@RequestBody createBusinessDTO business) {       
        Business newBusiness = new Business();
        newBusiness.setName(business.getName());    
        newBusiness.setRegistrationNumber(business.getRegistrationNumber());
        newBusiness.setContactNumber(business.getContactNumber());
        newBusiness.setAddress(business.getAddress());
        Business createdBusiness = businessRepository.save(newBusiness);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBusiness);
    }


    //DTOs
    public static class createBusinessDTO {
         
            private String name;
            private String registrationNumber;
            private String contactNumber;
            private String address;
         
           public String getName() { return name; }
           public void setName(String name) { this.name = name; }

           public String getRegistrationNumber() { return registrationNumber; }
           public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

           public String getContactNumber() { return contactNumber; }
           public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

           public String getAddress() { return address; }
           public void setAddress(String address) { this.address = address; }
       }
}
