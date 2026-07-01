package com.lanely.api.controller;

import com.lanely.api.dto.client.ClientAddressResponse;
import com.lanely.api.dto.client.CreateClientAddressRequest;
import com.lanely.api.dto.client.UpdateClientAddressRequest;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/clients/{clientId}/addresses")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Client addresses", description = "Manage the addresses of a client (depots, billing, shipping, headquarters)")
public class ClientAddressController {

    private final ClientService clientService;

    public ClientAddressController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @Operation(
            summary = "List client addresses",
            description = "Returns all addresses of the client, oldest first. The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addresses returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientAddressResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ClientAddressResponse> listAddresses(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.listAddresses(principal.userId(), companyId, clientId);
    }

    @PostMapping
    @Operation(
            summary = "Add a client address",
            description = "Adds an address to the client. Requires the MANAGE_CLIENTS permission. When the country is omitted it defaults "
                    + "to the company's country. Setting isPrimary/isDefaultBilling/isDefaultShipping to true unsets the same flag on the "
                    + "client's other addresses."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address added",
                    content = @Content(schema = @Schema(implementation = ClientAddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientAddressResponse> addAddress(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @Valid @RequestBody CreateClientAddressRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        ClientAddressResponse response = clientService.addAddress(principal.userId(), companyId, clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{addressId}")
    @Operation(
            summary = "Update a client address",
            description = "Updates an address. Only non-null fields are applied. Requires the MANAGE_CLIENTS permission. "
                    + "Setting a default flag to true unsets the same flag on the client's other addresses."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated",
                    content = @Content(schema = @Schema(implementation = ClientAddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, client or address not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ClientAddressResponse updateAddress(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @Parameter(description = "Identifier of the address", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateClientAddressRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.updateAddress(principal.userId(), companyId, clientId, addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @Operation(
            summary = "Delete a client address",
            description = "Permanently deletes an address of the client. Requires the MANAGE_CLIENTS permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, client or address not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @Parameter(description = "Identifier of the address", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID addressId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        clientService.deleteAddress(principal.userId(), companyId, clientId, addressId);
        return ResponseEntity.noContent().build();
    }
}
