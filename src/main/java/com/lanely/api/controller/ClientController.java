package com.lanely.api.controller;

import com.lanely.api.dto.client.ClientResponse;
import com.lanely.api.dto.client.ClientSummaryResponse;
import com.lanely.api.dto.client.CreateClientRequest;
import com.lanely.api.dto.client.UpdateClientRequest;
import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.entity.enums.ClientStatus;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/clients")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clients", description = "Manage the clients (customers) of a transport company, including their addresses and contacts")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @Operation(
            summary = "Create a client",
            description = "Creates a client for the company. Requires the MANAGE_CLIENTS permission (the OWNER always has it). "
                    + "The reference is unique within the company; when omitted it is auto-generated (e.g. CLI-0001). "
                    + "Addresses and contacts are added through their own sub-resources."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created",
                    content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found, or the referenced account manager is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A client with the same reference already exists in this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientResponse> createClient(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateClientRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        ClientResponse response = clientService.createClient(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List clients",
            description = "Returns a paginated list of the company's clients. The caller must be a member of the company. "
                    + "Optionally filter by status and by a free-text query matching the name, reference or e-mail. "
                    + "Pagination uses the standard page, size and sort query parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of clients returned",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<ClientSummaryResponse> listClients(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Filter by lifecycle status. Omit to return clients of any status.", example = "ACTIVE")
            @RequestParam(required = false) ClientStatus status,
            @Parameter(description = "Free-text search on name, reference or e-mail (case-insensitive)", example = "acme")
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.listClients(principal.userId(), companyId, status, q, pageable);
    }

    @GetMapping("/{clientId}")
    @Operation(
            summary = "Get a client",
            description = "Returns the full detail of a client, including its addresses and contacts. The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client returned",
                    content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ClientResponse getClient(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.getClient(principal.userId(), companyId, clientId);
    }

    @PatchMapping("/{clientId}")
    @Operation(
            summary = "Update a client",
            description = "Updates a client's editable information. Only non-null fields are applied. Requires the MANAGE_CLIENTS permission. "
                    + "A changed reference must remain unique within the company. The settings block is merged field by field."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated",
                    content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, client or referenced account manager not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Another client already uses the new reference",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ClientResponse updateClient(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @Valid @RequestBody UpdateClientRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.updateClient(principal.userId(), companyId, clientId, request);
    }

    @PostMapping("/{clientId}/archive")
    @Operation(
            summary = "Archive a client",
            description = "Soft-deletes a client by moving it to the ARCHIVED status. It is kept for history and existing orders/invoices, "
                    + "but hidden from the default active list. Requires the MANAGE_CLIENTS permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client archived",
                    content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ClientResponse archiveClient(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.setClientStatus(principal.userId(), companyId, clientId, ClientStatus.ARCHIVED);
    }

    @DeleteMapping("/{clientId}")
    @Operation(
            summary = "Permanently delete an archived client",
            description = "Hard-deletes an archived client and everything it owns: its addresses and contacts are removed for good. "
                    + "The client must already be in the ARCHIVED status (archive it first); deleting an ACTIVE client returns 400. "
                    + "Waybills are NEVER deleted (they are kept for legal/history): instead every reference to this client is cleared "
                    + "(the waybill client link, the denormalized pickup/delivery client and address identifiers, and the same identifiers "
                    + "on waybill parties are all set to null). This operation is irreversible. Requires the MANAGE_CLIENTS permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client permanently deleted"),
            @ApiResponse(responseCode = "400", description = "The client is not archived and therefore cannot be deleted",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        clientService.deleteArchivedClient(principal.userId(), companyId, clientId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clientId}/restore")
    @Operation(
            summary = "Restore a client",
            description = "Moves an archived client back to the ACTIVE status. Requires the MANAGE_CLIENTS permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client restored",
                    content = @Content(schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_CLIENTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or client not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ClientResponse restoreClient(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the client", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID clientId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return clientService.setClientStatus(principal.userId(), companyId, clientId, ClientStatus.ACTIVE);
    }
}
