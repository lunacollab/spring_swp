package com.example.demo.controller;

import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.Status;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StatusRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;

    private static final String UPLOAD_DIR = "D:/spring_swp/demo/uploads/";
    private final StatusRepository statusRepository;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(RegisterDTO registerDTO, Model model, @RequestParam("avatarFile") MultipartFile avatarFile) {
        return createUser(registerDTO, model, avatarFile, 1L, "register");
    }

    @GetMapping("/admin/create-account")
    public String showCreateAccount(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "create-account";
    }

    @GetMapping("/admin/getAllUser")
    public String showAllUser(Model model) {
        List<User> allUsers = userRepository.findAll();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getRole().getRoleID() != 2)
                .collect(Collectors.toList());
        model.addAttribute("users", filteredUsers);
        return "all-user";
    }

    @GetMapping("/admin/updateUser/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            model.addAttribute("error", "User not found.");
            return "redirect:/user/admin/getAllUser";
        }
        List<Role> role = roleRepository.findAll();
        List<Status> statuses = statusRepository.findAll();
        model.addAttribute("statuses", statuses);
        model.addAttribute("roles", role);
        model.addAttribute("existingUser", existingUser);
        return "update-user";
    }

    @PostMapping("/admin/updateUser/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute User updatedUser,
                             @RequestParam("avatarFile") MultipartFile avatarFile,
                             Model model) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            model.addAttribute("error", "User not found.");
            return "redirect:/user/admin/getAllUser";
        }

        // Kiểm tra trùng lặp username hoặc email
        if (isUsernameOrEmailTaken(updatedUser, model, id)) {
            model.addAttribute("existingUser", existingUser);
            model.addAttribute("roles", roleRepository.findAll());
            return "update-user";
        }

        // Cập nhật thông tin người dùng
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setDob(updatedUser.getDob());

        // Cập nhật roleID
        if (updatedUser.getRole() != null && updatedUser.getRole().getRoleID() != null) {
            Role role = roleRepository.findById(updatedUser.getRole().getRoleID()).orElse(null);
            if (role != null) {
                existingUser.setRole(role);
            }
        }

        // Cập nhật status
        if (updatedUser.getStatus() != null && updatedUser.getStatus().getStatusId() != null) {
            Status status = statusRepository.findById(updatedUser.getStatus().getStatusId()).orElse(null);
            if (status != null) {
                existingUser.setStatus(status);
            }
        }

        if (!avatarFile.isEmpty()) {
            try {
                existingUser.setAvatar(uploadAvatarFile(avatarFile));
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Avatar upload failed.");
                model.addAttribute("existingUser", existingUser);
                model.addAttribute("roles", roleRepository.findAll());
                return "update-user";
            }
        }

        userRepository.save(existingUser);
        return "redirect:/user/admin/getAllUser";
    }


    private boolean isUsernameOrEmailTaken(User updatedUser, Model model, Long userId) {
        User userWithSameUsername = userRepository.findByUsername(updatedUser.getUsername());
        if (userWithSameUsername != null && !userWithSameUsername.getUserID().equals(userId)) {
            model.addAttribute("error", "Username is already taken by another user.");
            return true;
        }

        User userWithSameEmail = userRepository.findByEmail(updatedUser.getEmail());
        if (userWithSameEmail != null && !userWithSameEmail.getUserID().equals(userId)) {
            model.addAttribute("error", "Email is already taken by another user.");
            return true;
        }
        return false;
    }

    @PostMapping("/admin/updatePassword/{id}")
    public String updatePassword(@PathVariable("id") Long id,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @ModelAttribute User existingUser,
                                 Model model) {

        if (existingUser == null) {
            model.addAttribute("error", "User not found.");
            return "redirect:/user/admin/getAllUser";
        }

        if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
            model.addAttribute("error", "Current password is incorrect.");
            return "update-user";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match.");
            return "update-user";
        }
        existingUser.setUserID(id);
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        userService.saveOrUpdate(existingUser);
        return "redirect:/user/admin/getAllUser";
    }

    @PostMapping("/admin/create-account")
    public String createAccount(RegisterDTO registerDTO, Model model, @RequestParam("avatarFile") MultipartFile avatarFile) {
        return createUser(registerDTO, model, avatarFile, null, "create-account");
    }

    private String createUser(RegisterDTO registerDTO, Model model, MultipartFile avatarFile, Long roleId, String returnPage) {
        if (userService.existsByUsername(registerDTO.getUsername())) {
            model.addAttribute("error", "Username already exists.");
            return returnPage;
        }

        if (userService.existsByEmail(registerDTO.getEmail())) {
            model.addAttribute("error", "Email already exists.");
            return returnPage;
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhoneNumber(registerDTO.getPhoneNumber());
        user.setDob(registerDTO.getDob());

        if (!avatarFile.isEmpty()) {
            try {
                user.setAvatar(uploadAvatarFile(avatarFile));
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Avatar upload failed.");
            }
        }

        Role role = new Role();
        role.setRoleID(roleId != null ? roleId : role.getRoleID());
        user.setRole(role);

        Status status = new Status();
        status.setStatusId(1L);
        user.setStatus(status);

        userService.saveOrUpdate(user);
        return returnPage;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    private String uploadAvatarFile(MultipartFile file) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.write(filePath, file.getBytes());
        return "/uploads/" + fileName;
    }
}
