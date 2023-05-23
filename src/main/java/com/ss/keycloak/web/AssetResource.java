package com.ss.keycloak.web;

import com.ss.keycloak.model.Asset;
import com.ss.keycloak.utils.constant.AppConstants;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(AppConstants.APP_CONTEXT_PATH + "/asset")
public class AssetResource {

    public Map<String, Asset> assetMap = new HashMap<>();


    @GetMapping()
    //@PreAuthorize("hasAuthority('ROLE_get_asset')")
    //@PreAuthorize("hasPermission(null, 'get_asset')")
    public Map<String, Asset> getAssets() {
        return this.assetMap;
    }

    @PostMapping()
    //@PostAuthorize("hasPermission(null, 'create_asset')")
    public void createAsset(@RequestBody Asset asset) {
        this.assetMap.put(asset.id(), asset);
    }

    @PutMapping()
    //@PreAuthorize("hasAuthority('ROLE_update_asset')")
    public void UpdateAsset(@RequestBody Asset asset) {
        this.assetMap.put(asset.id(), asset);
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasAuthority('ROLE_delete_asset')")
    public void UpdateAsset(@PathVariable("id") String id) {
        this.assetMap.remove(id);
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAuthority('ROLE_get_asset')")
    public Asset getAssetById(@PathVariable("id") String id) {
        return this.assetMap.get(id);
    }


}
