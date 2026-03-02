package waypoint.mvp.auth.security.jwt;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.domain.Role;

@Getter
@RequiredArgsConstructor
public enum JwtType {
	ACCESS("at", Role.USER),
	PRE_TERMS_ACCESS("ptat", Role.PRE_TERMS),
	REFRESH("rt", null);

	private static final Map<String, JwtType> TYPE_MAP = Arrays.stream(values())
		.collect(Collectors.toUnmodifiableMap(JwtType::getValue, Function.identity()));

	private final String value;
	private final Role role;

	public static JwtType from(String value) {
		JwtType type = TYPE_MAP.get(value);
		if (type == null) {
			throw new IllegalArgumentException("잘못된 토큰 타입입니다: " + value);
		}
		return type;
	}
}
