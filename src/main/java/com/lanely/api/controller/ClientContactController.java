package com.lanely.api.controller;

import com.lanely.api.dto.client.ClientContactResponse;
import com.lanely.api.dto.client.CreateClientContactRequest;
import com.lanely.api.dto.client.UpdateClientContactRequest;
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
@RequestMapping("/companies/{companyId}/clients/{clientId}/contacts")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Client contacts", description = "Manage the contact people attached to a client")
public class ClientContactController {

    private final ClientService clientService;

    public ClientContactController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @Operation(
            summary = "List client contacts",
            description = "Returns all contacts of the client, oldest first. The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacts returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientContactResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ClientContactResponse> listContacts(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.listContacts(principal.userId(), companyId, clientId);
    }

    @PostMapping
    @Operation(
            summary = "Add a client contact",
            description = "Adds a contact person to the client. Requires the MANAGE_CLIENTS permission. Setting isPrimary to true unsets "
                    + "it on the client's other contacts. The receivesInvoices / receivesDeliveryNotifications flags drive who is e-mailed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contact added",
                    content = @Content(schema = @Schema(implementation = ClientContactResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientContactResponse> addContact(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @Valid @RequestBody CreateClientContactRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        ClientContactResponse response = clientService.addContact(principal.userId(), companyId, clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{contactId}")
    @Operation(
            summary = "Update a client contact",
            description = "Updates a contact. Only non-null fields are applied. Requires the MANAGE_CLIENTS permission. "
                    + "Setting isPrimary to true unsets it on the client's other contacts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact updated",
                    content = @Content(schema = @Schema(implementation = ClientContactResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, client or contact not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ClientContactResponse updateContact(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @Parameter(description = "Identifier of the contact", example = "2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e")
            @PathVariable UUID contactId,
            @Valid @RequestBody UpdateClientContactRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.updateContact(principal.userId(), companyId, clientId, contactId, request);
    }

    @DeleteMapping("/{contactId}")
    @Operation(
            summary = "Delete a client contact",
            description = "Permanently deletes a contact of the client. Requires the MANAGE_CLIENTS permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contact deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, client or contact not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteContact(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @Parameter(description = "Identifier of the contact", example = "2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e")
            @PathVariable UUID contactId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        clientService.deleteContact(principal.userId(), companyId, clientId, contactId);
        return ResponseEntity.noContent().build();
    }
}
