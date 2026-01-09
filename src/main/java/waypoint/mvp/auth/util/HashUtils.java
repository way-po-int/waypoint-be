package waypoint.mvp.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashUtils {

	private static final String ALGORITHM = "SHA-256";

	public static String generateHash(String input) {
		Objects.requireNonNull(input);
		try {
			MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
			byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(encodedHash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("해싱 알고리즘을 찾을 수 없습니다.", e);
		}
	}
}
