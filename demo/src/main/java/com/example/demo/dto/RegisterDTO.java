package com.example.demo.dto;

import com.example.demo.entity.Role;
import com.example.demo.entity.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterDTO {
    String username;
    @Size(min = 6, max = 12, message = "Password must be between 6 and 12 characters")
    String password;
    String email;
    @Size(min = 10, max = 10, message = "Phone number must be 10 characters")
    String phoneNumber;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date Dob;
}
