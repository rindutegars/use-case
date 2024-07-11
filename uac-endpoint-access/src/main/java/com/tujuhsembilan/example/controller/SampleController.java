package com.tujuhsembilan.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class SampleController {

  @GetMapping("/data-a")
  @PreAuthorize("hasAuthority('ROLE_A')")
  public ResponseEntity<?> getDataA() {
    return ResponseEntity.ok().build();
  }

  @GetMapping("/data-b")
  @PreAuthorize("hasAuthority('ROLE_B')")
  public ResponseEntity<?> getDataB() {
    return ResponseEntity.ok().build();
  }

  @GetMapping("/data-c")
  @PreAuthorize("hasAnyAuthority('ROLE_A', 'ROLE_B')")
  public ResponseEntity<?> getDataC() {
    return ResponseEntity.ok().build();
  }
}
