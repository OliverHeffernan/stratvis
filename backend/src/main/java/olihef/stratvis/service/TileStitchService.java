package olihef.stratvis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class TileStitchService {

	private static final int TILE_SIZE = 256;
	private static final int MAX_TILE_COUNT = 900;
	private static final String TOO_MANY_TILES_MESSAGE = "Requested area is too large at this zoom level.";
	private static final String TILE_URL_TEMPLATE =
		"https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/%d/%d/%d";

	private final RestTemplate restTemplate;

	public TileStitchService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public record StitchResult(byte[] png, int usedZoom) {
	}

	public StitchResult stitchPngWithHigherZoomFallback(double minLng, double minLat, double maxLng, double maxLat, int baseZoom) throws IOException {
		int preferredZoom = Math.min(19, Math.max(1, baseZoom + 1));
		int fallbackZoom = Math.max(1, Math.min(19, baseZoom));

		try {
			return new StitchResult(stitchPng(minLng, minLat, maxLng, maxLat, preferredZoom), preferredZoom);
		} catch (IllegalArgumentException ex) {
			if (!isTooManyTilesError(ex) || preferredZoom == fallbackZoom) {
				throw ex;
			}
		}

		return new StitchResult(stitchPng(minLng, minLat, maxLng, maxLat, fallbackZoom), fallbackZoom);
	}

	public byte[] stitchPng(double minLng, double minLat, double maxLng, double maxLat, int zoom) throws IOException {
		validateRequest(minLng, minLat, maxLng, maxLat, zoom);

		int n = 1 << zoom;
		int xMin = lngToTileX(minLng, zoom);
		int xMax = lngToTileX(maxLng, zoom);
		int yMin = latToTileY(maxLat, zoom);
		int yMax = latToTileY(minLat, zoom);

		int tileCols = xMax - xMin + 1;
		int tileRows = yMax - yMin + 1;
		int tileCount = tileCols * tileRows;
		if (tileCount <= 0 || tileCount > MAX_TILE_COUNT) {
			throw new IllegalArgumentException(TOO_MANY_TILES_MESSAGE);
		}

		BufferedImage stitched = new BufferedImage(tileCols * TILE_SIZE, tileRows * TILE_SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = stitched.createGraphics();
		try {
			for (int tileY = yMin; tileY <= yMax; tileY++) {
				for (int tileX = xMin; tileX <= xMax; tileX++) {
					BufferedImage tile = fetchTile(zoom, tileY, tileX);
					if (tile == null) {
						continue;
					}
					int dx = (tileX - xMin) * TILE_SIZE;
					int dy = (tileY - yMin) * TILE_SIZE;
					graphics.drawImage(tile, dx, dy, null);
				}
			}
		} finally {
			graphics.dispose();
		}

		double worldSize = TILE_SIZE * (double) n;
		double leftGlobal = lngToPixelX(minLng, worldSize);
		double rightGlobal = lngToPixelX(maxLng, worldSize);
		double topGlobal = latToPixelY(maxLat, worldSize);
		double bottomGlobal = latToPixelY(minLat, worldSize);

		int originPixelX = xMin * TILE_SIZE;
		int originPixelY = yMin * TILE_SIZE;

		int cropX = (int) Math.floor(leftGlobal - originPixelX);
		int cropY = (int) Math.floor(topGlobal - originPixelY);
		int cropW = (int) Math.ceil(rightGlobal - leftGlobal);
		int cropH = (int) Math.ceil(bottomGlobal - topGlobal);

		cropX = Math.max(0, Math.min(cropX, stitched.getWidth() - 1));
		cropY = Math.max(0, Math.min(cropY, stitched.getHeight() - 1));
		cropW = Math.max(1, Math.min(cropW, stitched.getWidth() - cropX));
		cropH = Math.max(1, Math.min(cropH, stitched.getHeight() - cropY));

		BufferedImage cropped = stitched.getSubimage(cropX, cropY, cropW, cropH);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(cropped, "png", output);
		return output.toByteArray();
	}

	private BufferedImage fetchTile(int zoom, int tileY, int tileX) throws IOException {
		String url = TILE_URL_TEMPLATE.formatted(zoom, tileY, tileX);
		byte[] body = restTemplate.getForObject(url, byte[].class);
		if (body == null || body.length == 0) {
			return null;
		}
		return ImageIO.read(new ByteArrayInputStream(body));
	}

	private void validateRequest(double minLng, double minLat, double maxLng, double maxLat, int zoom) {
		if (Double.isNaN(minLng) || Double.isNaN(minLat) || Double.isNaN(maxLng) || Double.isNaN(maxLat)) {
			throw new IllegalArgumentException("Bounds must be numeric values.");
		}
		if (minLng < -180 || maxLng > 180 || minLat < -85.0511 || maxLat > 85.0511) {
			throw new IllegalArgumentException("Bounds are outside Web Mercator limits.");
		}
		if (minLng >= maxLng || minLat >= maxLat) {
			throw new IllegalArgumentException("Invalid bounds ordering.");
		}
		if (zoom < 1 || zoom > 19) {
			throw new IllegalArgumentException("Zoom must be between 1 and 19.");
		}
	}

	private int lngToTileX(double lng, int zoom) {
		double n = Math.pow(2, zoom);
		int x = (int) Math.floor((lng + 180.0) / 360.0 * n);
		return clamp(x, 0, (int) n - 1);
	}

	private int latToTileY(double lat, int zoom) {
		double latRad = Math.toRadians(lat);
		double n = Math.pow(2, zoom);
		int y = (int) Math.floor((1.0 - Math.log(Math.tan(latRad) + (1 / Math.cos(latRad))) / Math.PI) / 2.0 * n);
		return clamp(y, 0, (int) n - 1);
	}

	private double lngToPixelX(double lng, double worldSize) {
		return ((lng + 180.0) / 360.0) * worldSize;
	}

	private double latToPixelY(double lat, double worldSize) {
		double latRad = Math.toRadians(lat);
		return (1.0 - Math.log(Math.tan(latRad) + (1 / Math.cos(latRad))) / Math.PI) / 2.0 * worldSize;
	}

	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private boolean isTooManyTilesError(IllegalArgumentException exception) {
		return TOO_MANY_TILES_MESSAGE.equals(exception.getMessage());
	}
}
