package olihef.stratvis.controller;

import olihef.stratvis.service.TileStitchService;
import olihef.stratvis.sessions.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/tiles")
public class TileController {

	private final TileStitchService tileStitchService;

	public TileController(TileStitchService tileStitchService) {
		this.tileStitchService = tileStitchService;
	}

	@GetMapping(value = "/stitch", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> stitch(
		@RequestParam("minLng") double minLng,
		@RequestParam("minLat") double minLat,
		@RequestParam("maxLng") double maxLng,
		@RequestParam("maxLat") double maxLat,
		@RequestParam(value = "zoom", defaultValue = "15") int zoom
	) throws IOException {
		TileStitchService.StitchResult stitchResult = tileStitchService.stitchPngWithHigherZoomFallback(
			minLng,
			minLat,
			maxLng,
			maxLat,
			zoom
		);

		byte[] png = stitchResult.png();
		int id = SessionsManager.addNewSessionWithSnapshot(minLng, minLat, maxLng, maxLat, stitchResult.usedZoom());

		// put the id into the response header so the client can use it for future requests related to this session
		// in javascript the client can do something like this to extract the session id from the response header:
		// const sessionId = response.headers.get("X-Session-Id");
		return ResponseEntity.ok()
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Session-Id")
			.header("X-Used-Zoom", String.valueOf(stitchResult.usedZoom()))
			.header("X-Session-Id", String.valueOf(id))
			.contentType(MediaType.IMAGE_PNG)
			.body(png);
	}
}
