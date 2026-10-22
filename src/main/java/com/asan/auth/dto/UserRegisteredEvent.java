package com.asan.auth.dto;

public class UserRegisteredEvent {

  private Long userId;
  private String username;
  private String email;
  private String nama;
  private String alamat;
  private String jenisKelamin;

  public UserRegisteredEvent() {
  }

  public UserRegisteredEvent(Long userId, String username, String email, String nama, String alamat,
      String jenisKelamin) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.nama = nama;
    this.alamat = alamat;
    this.jenisKelamin = jenisKelamin;
  }

  public Long getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getNama() {
    return nama;
  }

  public String getAlamat() {
    return alamat;
  }

  public String getJenisKelamin() {
    return jenisKelamin;
  }
}
