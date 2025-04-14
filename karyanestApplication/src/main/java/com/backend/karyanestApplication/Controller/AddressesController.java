package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.DTO.AddressResponseDTO;
import com.backend.karyanestApplication.Model.Addresses;
import com.backend.karyanestApplication.Service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing addresses.
 * Provides CRUD operations and filtering based on city, area, district, state, and pincode.
 */
@RestController
@RequestMapping("/v1/addresses")
@Tag(name = "Address Management", description = "APIs for managing addresses and location-based queries")
public class AddressesController {

    @Autowired
    private AddressesService addressesService;

    /**
     * Retrieves all addresses.
     *
     * @return List of all addresses.
     */
    @Operation(summary = "Get All Addresses", description = "Fetches a list of all addresses.")
    @GetMapping
    public List<Addresses> getAllAddresses() {
        return addressesService.getAllAddresses();
    }

    /**
     * Retrieves an address by its ID.
     *
     * @param id The unique ID of the address.
     * @return ResponseEntity containing the address or 404 if not found.
     */
    @Operation(summary = "Get Address by ID", description = "Fetches an address using its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Addresses> getAddressById(@PathVariable Long id) {
        Optional<Addresses> address = addressesService.getAddressById(id);
        return address.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves addresses by city name.
     *
     * @param city The city to filter addresses.
     * @return List of addresses in the given city.
     */
    @Operation(summary = "Get Addresses by City", description = "Fetches addresses based on city name.")
    @GetMapping("/city")
    public List<Addresses> getAddressesByCity(@RequestParam String city) {
        return addressesService.getAddressesByCity(city);
    }

    /**
     * Retrieves addresses by area.
     *
     * @param area The area to filter addresses.
     * @return List of addresses in the given area.
     */
    @Operation(summary = "Get Addresses by Area", description = "Fetches addresses based on area name.")
    @GetMapping("/area")
    public List<Addresses> getAddressesByArea(@RequestParam String area) {
        return addressesService.getAddressesByArea(area);
    }

    /**
     * Retrieves addresses by district.
     *
     * @param district The district to filter addresses.
     * @return List of addresses in the given district.
     */
    @Operation(summary = "Get Addresses by District", description = "Fetches addresses based on district name.")
    @GetMapping("/district")
    public List<Addresses> getAddressesByDistrict(@RequestParam String district) {
        return addressesService.getAddressesByDistrict(district);
    }

    /**
     * Retrieves addresses by state.
     *
     * @param state The state to filter addresses.
     * @return List of addresses in the given state.
     */
    @Operation(summary = "Get Addresses by State", description = "Fetches addresses based on state name.")
    @GetMapping("/state")
    public List<Addresses> getAddressesByState(@RequestParam String state) {
        return addressesService.getAddressesByState(state);
    }

    /**
     * Retrieves addresses by pincode.
     *
     * @param pincode The pincode to filter addresses.
     * @return List of addresses in the given pincode area.
     */
    @Operation(summary = "Get Addresses by Pincode", description = "Fetches addresses based on pincode.")
    @GetMapping("/pincode")
    public List<Addresses> getAddressesByPincode(@RequestParam String pincode) {
        return addressesService.getAddressesByPincode(pincode);
    }

    /**
     * Creates a new address entry.
     *
     * @param address The address details to create.
     * @return The newly created address.
     */
    @Operation(summary = "Create a New Address", description = "Creates and saves a new address.")
    @PostMapping
    public Addresses createAddress(@RequestBody Addresses address) {
        return addressesService.saveAddress(address);
    }

    /**
     * Updates an existing address by ID.
     *
     * @param id         The ID of the address to update.
     * @param newAddress The updated address details.
     * @return ResponseEntity containing the updated address or 404 if not found.
     */
    @Operation(summary = "Update Address", description = "Updates an existing address based on ID.")
    @PutMapping("/{id}")
    public ResponseEntity<Addresses> updateAddress(@PathVariable Long id, @RequestBody Addresses newAddress) {
        Optional<Addresses> updatedAddress = addressesService.updateAddress(id, newAddress);
        return updatedAddress.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes an address by ID.
     *
     * @param id The ID of the address to delete.
     * @return ResponseEntity with no content status.
     */
    @Operation(summary = "Delete Address", description = "Deletes an address based on ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressesService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Search Addresses by Keyword", description = "Fetches addresses based on a search keyword.")
    @GetMapping("/search")
    public ResponseEntity<List<AddressResponseDTO>> searchAddressesByKeyword(@RequestParam String keyword) {
        List<AddressResponseDTO> addresses = addressesService.getSearchAddressesByKeyword(keyword);
        return ResponseEntity.ok(addresses);
    }
}
