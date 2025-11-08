package ir.netpick.mailmine.auth.controller;

import ir.netpick.mailmine.auth.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.jwt.JWTUtil;
import ir.netpick.mailmine.auth.service.UserService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    @GetMapping()
    public ResponseEntity<UserDTO> getProfile(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        String email = jwtUtil.getSubject(token.substring(7));
        return ResponseEntity.ok()
                .body(userService.getUser(email));
    }

    @PutMapping()
    public ResponseEntity<?> updateProfile(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
                                                @RequestBody UserUpdateRequest request) {
        String email = jwtUtil.getSubject(token.substring(7));
        userService.updateUser(email, request);
        return ResponseEntity.ok()
                .build();
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteProfile(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        String email = jwtUtil.getSubject(token.substring(7));
        userService.deleteUser(email);
        return ResponseEntity.ok()
                .build();
    }
//
//    @GetMapping("image")
//    public ResponseEntity<String> getProfileImage(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
//        String email = jwtUtil.getSubject(token.substring(7));
//        return ResponseEntity.ok()
//                .body(userService.getUserImage(email));
//    }
//
//    @PutMapping("image")
//    public ResponseEntity<?> updateProfileImage(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
//                                                @RequestBody String imageId) {
//        String email = jwtUtil.getSubject(token.substring(7));
//        userService.updateUserImage(email, imageId);
//        return ResponseEntity.ok()
//                .build();
//    }
//
//    @DeleteMapping("image")
//    public ResponseEntity<?> deleteProfileImage(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
//        String email = jwtUtil.getSubject(token.substring(7));
//        userService.deleteUserImage(email);
//        return ResponseEntity.ok()
//                .build();
//    }





}
