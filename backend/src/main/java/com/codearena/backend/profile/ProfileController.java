package com.codearena.backend.profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.backend.profile.dto.ProfileSummaryResponse;
import com.codearena.backend.security.AuthenticatedUser;

@RestController
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/profile/summary")
    public ResponseEntity<ProfileSummaryResponse> summary(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(profileService.getSummary(principal.id()));
    }
}
