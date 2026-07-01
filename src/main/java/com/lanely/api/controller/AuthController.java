package com.lanely.api.controller;

import com.lanely.api.controller.support.DeviceMetaResolver;
import com.lanely.api.dto.auth.DriverGoogleLoginRequest;
import com.lanely.api.dto.auth.GoogleAuthResponse;
import com.lanely.api.dto.auth.GoogleLoginRequest;
import com.lanely.api.dto.auth.ForgotPasswordRequest;
import com.lanely.api.dto.auth.GoogleRegisterRequest;
import com.lanely.api.dto.auth.ProfileAuthResponse;
import com.lanely.api.dto.auth.ProfileLoginRequest;
import com.lanely.api.dto.auth.RefreshRequest;
import com.lanely.api.dto.auth.RegisterUserRequest;
import com.lanely.api.dto.auth.ResetPasswordRequest;
import com.lanely.api.dto.auth.TokenResponse;
import com.lanely.api.dto.auth.UserAuthResponse;
import com.lanely.api.dto.auth.UserLoginRequest;
import com.lanely.api.dto.auth.VerifyEmailRequest;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.security.AuthenticatedPrincipal;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.AuthService;
import com.lanely.api.service.EmailVerificationService;
import com.lanely.api.service.GoogleAuthService;
import com.lanely.api.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Sign up, log in (web users and mobile profiles), refresh tokens and log out")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final GoogleAuthService googleAuthService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, EmailVerificationService emailVerificationService,
                          GoogleAuthService googleAuthService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
        this.googleAuthService = googleAuthService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new web user and start a session",
            description = "Creates a new web user account (email + password + name) and immediately issues an access/refresh token pair. "
                    + "Email is unique and case-insensitive. The created session is bound to the calling device."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created and tokens issued",
                    content = @Content(schema = @Schema(implementation = UserAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "An account already exists for this email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserAuthResponse> register(@Valid @RequestBody RegisterUserRequest request,
                                                     HttpServletRequest httpRequest) {
        UserAuthResponse response = authService.registerAndLogin(request, DeviceMetaResolver.from(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in a web user",
            description = "Authenticates a web user with email and password and issues a new access/refresh token pair for the calling device. "
                    + "A new session is created on every login, so the same account can be logged in on several devices simultaneously."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded",
                    content = @Content(schema = @Schema(implementation = UserAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserAuthResponse> login(@Valid @RequestBody UserLoginRequest request,
                                                  HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.loginUser(request, DeviceMetaResolver.from(httpRequest)));
    }

    @PostMapping("/google")
    @Operation(
            summary = "Start a Google sign-in (log in, or request account creation)",
            description = "First step of \"Sign in with Google\" for web users. Takes a Google ID token obtained on the client (Google "
                    + "Identity Services). The backend verifies the token's signature, expiry and audience (the configured Google client id) "
                    + "against Google's public keys, requires the Google email to be verified, then resolves one of two outcomes:\n"
                    + "- AUTHENTICATED: a user is already linked to this Google account, OR a user already exists with the same email (in "
                    + "which case the Google identity is automatically linked). A session is issued (field 'session') just like a normal login.\n"
                    + "- REGISTRATION_REQUIRED: no account exists yet. NOTHING is created. The response carries 'registration' (pre-filled "
                    + "email/first name/last name from Google) so the client can show a sign-up confirmation form. The client then calls "
                    + "POST /auth/google/register with the same ID token to actually create the account.\n"
                    + "The HTTP status is 200 in both outcomes; branch on the 'status' field."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Either a session was issued (status=AUTHENTICATED) or account creation "
                    + "is required (status=REGISTRATION_REQUIRED)",
                    content = @Content(schema = @Schema(implementation = GoogleAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing idToken)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The Google ID token is invalid, expired, has the wrong audience, "
                    + "its email is not verified, or the matched account is disabled",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GoogleAuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request,
                                                              HttpServletRequest httpRequest) {
        return ResponseEntity.ok(googleAuthService.loginWithGoogle(request, DeviceMetaResolver.from(httpRequest)));
    }

    @PostMapping("/google/register")
    @Operation(
            summary = "Confirm Google account creation and start a session",
            description = "Second step of \"Sign in with Google\", called after POST /auth/google returned status=REGISTRATION_REQUIRED "
                    + "and the user confirmed the pre-filled sign-up form. Takes the same Google ID token plus the chosen first/last name. "
                    + "The token is verified again server-side; the email and Google identity are always taken from the token (never trusted "
                    + "from the client), only the names come from the request. Creates a new web user (email already verified, no usable "
                    + "password — login is via Google) and issues an access/refresh token pair. If, in the meantime, a matching account "
                    + "already exists (same Google id or email), it is logged in instead of duplicated (idempotent)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created (or matched) and tokens issued",
                    content = @Content(schema = @Schema(implementation = UserAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields (idToken, firstName, lastName)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The Google ID token is invalid, expired, has the wrong audience or its "
                    + "email is not verified",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserAuthResponse> registerWithGoogle(@Valid @RequestBody GoogleRegisterRequest request,
                                                               HttpServletRequest httpRequest) {
        UserAuthResponse response = googleAuthService.registerWithGoogle(request, DeviceMetaResolver.from(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/profile/login")
    @Operation(
            summary = "Log in a mobile delivery session (profile or web user acting as a driver)",
            description = "Opens a mobile delivery session scoped to a company, identified by the company public code (obtained by scanning "
                    + "the company code) plus an identifier and password. The identifier is resolved in two ways:\n"
                    + "- as a delivery profile username within the company (subjectType=PROFILE in the response), OR\n"
                    + "- if no profile matches, as the email of a web user who is a member of the company, i.e. a web user acting as a driver "
                    + "(subjectType=DRIVER in the response).\n"
                    + "In both cases the issued access token carries the company id and grants the same driver access. Web users who signed up "
                    + "with Google have no usable password and must use POST /auth/driver/google instead. A single generic error is returned "
                    + "for every failure (unknown company, unknown identifier, wrong password, or not a member) to avoid enumeration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded",
                    content = @Content(schema = @Schema(implementation = ProfileAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid company code, identifier or password (or the web user is not a member of the company)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProfileAuthResponse> loginProfile(@Valid @RequestBody ProfileLoginRequest request,
                                                            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.loginProfile(request, DeviceMetaResolver.from(httpRequest)));
    }

    @PostMapping("/driver/google")
    @Operation(
            summary = "Log in a mobile delivery session for a Google web user",
            description = "Opens a mobile delivery session for a web user who signs in with Google, scoped to a company. Takes the company "
                    + "public code (obtained by scanning the company code) plus a Google ID token obtained on the client (Google Identity "
                    + "Services). The token is verified server-side (signature, expiry, audience, verified email). The matching web user must "
                    + "already exist (this endpoint never creates an account) and must be a member of the company. On success, issues an "
                    + "access/refresh token pair whose access token carries the company id and grants driver access (subjectType=DRIVER)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded",
                    content = @Content(schema = @Schema(implementation = ProfileAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid company code, invalid/expired Google ID token, no matching web user, "
                    + "the account is disabled, or the user is not a member of the company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProfileAuthResponse> loginDriverWithGoogle(@Valid @RequestBody DriverGoogleLoginRequest request,
                                                                     HttpServletRequest httpRequest) {
        return ResponseEntity.ok(googleAuthService.loginDriverWithGoogle(request, DeviceMetaResolver.from(httpRequest)));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh tokens",
            description = "Exchanges a valid, non-revoked, non-expired refresh token for a brand new access/refresh token pair. "
                    + "The previous refresh token is rotated and can no longer be reused. Works for both user and profile sessions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New token pair issued",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid, revoked or expired",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request,
                                                 HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refresh(request, DeviceMetaResolver.from(httpRequest)));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Log out the current session",
            description = "Revokes the session bound to the access token used for this request. The associated refresh token can no longer "
                    + "be used; the access token remains technically valid until it expires (short-lived)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Current session revoked"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        authService.logout(principal.accountId(), principal.sessionId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify an email address and log in",
            description = "Confirms a user's email address using the token from the verification link sent by email "
                    + "(the website page behind the link calls this endpoint with the token). Public. On success the user is "
                    + "logged in directly: a new session is created and an access/refresh token pair is returned, with the access "
                    + "token already carrying emailVerified=true (no refresh needed to lift the verification gate)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified and tokens issued",
                    content = @Content(schema = @Schema(implementation = UserAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or the token is expired/already used",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Token not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserAuthResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request, HttpServletRequest httpRequest) {
        return emailVerificationService.verify(request.token(), DeviceMetaResolver.from(httpRequest));
    }

    @PostMapping("/verify-email/resend")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Resend my verification email",
            description = "Sends a fresh email-verification link to the currently authenticated user. Fails if the email is already verified."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Verification email sent"),
            @ApiResponse(responseCode = "400", description = "Email already verified, or unsupported account type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> resendVerification(@AuthenticationPrincipal Object principal) {
        if (principal instanceof AuthenticatedUser user) {
            emailVerificationService.resend(user.userId());
            return ResponseEntity.noContent().build();
        }
        throw new BadRequestException("error.me.web-only-email");
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request a password reset link",
            description = "Starts the \"forgot password\" flow for a web user. Public. If an active account exists for the given email, "
                    + "a reset link (containing a short-lived, single-use token, valid for 1 hour) is sent to that address; the website page "
                    + "behind the link then calls POST /auth/reset-password with the token. To prevent email enumeration, this endpoint always "
                    + "responds 204 whether or not an account exists for the email — the client must never reveal which case happened. "
                    + "Mobile delivery profiles are not concerned (they have no email)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Request accepted; a reset email was sent if a matching active account exists"),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing or malformed email)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Set a new password from a reset token",
            description = "Second step of the \"forgot password\" flow, called by the website page behind the reset link. Public. Takes the "
                    + "token from the link plus the chosen new password (min 8 chars). On success the account password is replaced, the email "
                    + "is marked as verified (receiving the link proves ownership of the address), the token is consumed (single-use), and ALL "
                    + "existing sessions of the account are revoked for security. No session is issued here: the user must log in again with the "
                    + "new password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password updated; all sessions revoked"),
            @ApiResponse(responseCode = "400", description = "Validation failed, or the token is expired or already used",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Token not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reset(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
