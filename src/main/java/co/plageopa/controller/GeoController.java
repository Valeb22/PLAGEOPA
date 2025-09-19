package co.plageopa.controller;

import co.plageopa.repository.GeoDao;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    private final GeoDao geo;

    public GeoController(GeoDao geo) { this.geo = geo; }

    @GetMapping(value = "/veredas", produces = MediaType.APPLICATION_JSON_VALUE)
    public String veredas() {
        return geo.veredasFeatureCollection();
    }

    // /api/geo/veredas/bbox?bbox=minX,minY,maxX,maxY
    @GetMapping(value = "/veredas/bbox", produces = MediaType.APPLICATION_JSON_VALUE)
    public String veredasBbox(@RequestParam String bbox) {
        String[] p = bbox.split(",", -1);
        double minX = Double.parseDouble(p[0]);
        double minY = Double.parseDouble(p[1]);
        double maxX = Double.parseDouble(p[2]);
        double maxY = Double.parseDouble(p[3]);
        return geo.veredasFeatureCollectionByBbox(minX, minY, maxX, maxY);
    }
}
