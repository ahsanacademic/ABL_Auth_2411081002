package com.asan.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

  @NotBlank
  @Size(min = 3, max = 50)
  @Pattern(regexp = "^[A-Za-z0-9._-]+$")
  private String username;

  @NotBlank
  @Size(min = 12, max = 72)
  private String password;

  @NotBlank
  @Email
  @Size(max = 254)
  private String email;

  @NotBlank
  @Size(max = 100)
  private String nama;

  @NotBlank
  @Size(max = 255)
  private String alamat;

  @Pattern(regexp = "LAKI_LAKI|PEREMPUAN")
  private String jenisKelamin;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getNama() {
    return nama;
  }

  public void setNama(String nama) {
    this.nama = nama;
  }

  public String getAlamat() {
    return alamat;
  }

  public void setAlamat(String alamat) {
    this.alamat = alamat;
  }

  public String getJenisKelamin() {
    return jenisKelamin;
  }

  public void setJenisKelamin(String jenisKelamin) {
    this.jenisKelamin = jenisKelamin;
  }
}
