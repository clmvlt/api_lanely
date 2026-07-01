package com.lanely.api.service;

import com.lanely.api.dto.me.ChangePasswordRequest;
import com.lanely.api.dto.me.UpdateMeRequest;
import com.lanely.api.entity.Account;
import com.lanely.api.entity.Image;
import com.lanely.api.entity.User;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.repository.AccountRepository;
import com.lanely.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final ImageService imageService;

    public AccountService(UserRepository userRepository, AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder, SessionService sessionService, ImageService imageService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.imageService = imageService;
    }

    @Transactional
    public void updateUserInfo(UUID userId, UpdateMeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));
        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }
    }

    @Transactional
    public void changePassword(UUID accountId, UUID currentSessionId, ChangePasswordRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("error.account.not-found"));
        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new BadRequestException("error.password.incorrect");
        }
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        sessionService.revokeOthers(accountId, currentSessionId);
    }

    @Transactional
    public void setUserPicture(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));
        Image previous = user.getProfileImage();
        Image image = imageService.upload(file);
        user.setProfileImage(image);
        if (previous != null) {
            imageService.delete(previous);
        }
    }

    @Transactional
    public void removeUserPicture(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));
        Image previous = user.getProfileImage();
        if (previous != null) {
            user.setProfileImage(null);
            imageService.delete(previous);
        }
    }
}
