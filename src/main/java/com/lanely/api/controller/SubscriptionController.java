package com.lanely.api.controller;

import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.subscription.ChangeSubscriptionRequest;
import com.lanely.api.dto.subscription.ChangeSubscriptionResponse;
import com.lanely.api.dto.subscription.MySubscriptionResponse;
import com.lanely.api.dto.subscription.SubscriptionPlanResponse;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Subscriptions", description = "Browse subscription plans and manage the current web user's subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/subscription-plans")
    @Operation(
            summary = "List available subscription plans",
            description = "Returns every subscription plan offered to web users, ordered from the smallest to the largest. Each plan exposes "
                    + "its stable technical code, a localized name and description (resolved from the Accept-Language header), its monthly "
                    + "price (in cents and as a decimal amount, tax-excluded), and its limits: how many companies the owner may create and "
                    + "how many seats (active profiles + members) each company may hold. Public endpoint: no authentication required, so it can "
                    + "back a pricing page."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plans returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubscriptionPlanResponse.class))))
    })
    public List<SubscriptionPlanResponse> listPlans() {
        return subscriptionService.listPlans();
    }

    @GetMapping("/me/subscription")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my current subscription and usage",
            description = "Returns the calling web user's current plan (or null if none) together with their live usage: number of companies "
                    + "owned versus the company limit, and, for every owned company, the seat consumption (active profiles + members) versus "
                    + "the per-company seat limit. Useful to render a 'manage subscription' screen and show remaining seats."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription and usage returned",
                    content = @Content(schema = @Schema(implementation = MySubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a web user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MySubscriptionResponse getMySubscription(@AuthenticationPrincipal AuthenticatedUser principal) {
        return subscriptionService.getMySubscription(principal.userId());
    }

    @PutMapping("/me/subscription")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change my subscription plan",
            description = "Switches the calling web user to another plan, identified by its technical code (STARTER, PRO or ENTERPRISE). "
                    + "Today the change is applied immediately and the response status is ACTIVATED with a null checkoutUrl. The contract is "
                    + "already shaped for a future external payment provider (e.g. Stripe): an upgrade may then return PENDING_PAYMENT with a "
                    + "checkoutUrl the client must redirect to, the plan becoming active only after payment is confirmed. A downgrade is "
                    + "rejected (403) when current usage exceeds the target plan's limits (too many owned companies, or a company with more "
                    + "seats than the target allows): free up the excess first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan-change request processed",
                    content = @Content(schema = @Schema(implementation = ChangeSubscriptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing plan code)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a web user, or the downgrade conflicts with current usage",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No plan exists for the provided code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ChangeSubscriptionResponse changeSubscription(@Valid @RequestBody ChangeSubscriptionRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        return subscriptionService.changePlan(principal.userId(), request);
    }
}
